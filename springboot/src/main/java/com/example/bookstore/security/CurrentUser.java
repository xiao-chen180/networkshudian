package com.example.bookstore.security;

import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 统一的当前用户解析入口。
 * JwtAuthenticationFilter 已将认证信息写入 SecurityContext，
 * 各 Controller 通过本组件获取当前用户 ID / 角色，禁止再自行解析 Token。
 */
@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 当前登录用户的 ID；未登录返回 null，由调用方决定返回 401。 */
    public Long getId() {
        User user = getOrNull();
        return user != null ? user.getId() : null;
    }

    /** 当前登录用户实体；未登录返回 null。 */
    public User getOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByAccount(auth.getName()).orElse(null);
    }

    /** 当前请求是否携带管理员角色（由 JwtAuthenticationFilter 写入 ROLE_ADMIN）。 */
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
