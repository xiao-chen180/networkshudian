package com.example.bookstore.service;

import com.example.bookstore.dto.ChangePasswordRequest;
import com.example.bookstore.dto.JwtResponse;
import com.example.bookstore.dto.LoginRequest;
import com.example.bookstore.dto.RegisterRequest;
import com.example.bookstore.dto.UpdateUserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {

    // 管理员注册密钥：仅当通过环境变量 BOOKSTORE_ADMIN_SECRET 配置后才生效，默认关闭
    @Value("${bookstore.admin-secret:}")
    private String adminSecretKey;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByAccount(request.getAccount())) {
            throw new CustomException("账号已存在", HttpStatus.BAD_REQUEST.value());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException("用户名已存在", HttpStatus.BAD_REQUEST.value());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("邮箱已存在", HttpStatus.BAD_REQUEST.value());
        }

        User user = new User();
        user.setAccount(request.getAccount());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // 仅在服务端配置了管理员密钥时才允许通过注册创建管理员
        String adminSecret = request.getAdminSecret();
        if (adminSecret != null && !adminSecretKey.isBlank() && adminSecretKey.equals(adminSecret)) {
            user.setRole("ADMIN");
            user.setDiscount(BigDecimal.valueOf(1.0));
        } else {
            user.setRole("USER");
            user.setDiscount(BigDecimal.valueOf(0.95));
        }

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getAccount(), savedUser.getId());

        return new JwtResponse(token, savedUser.getId(), savedUser.getAccount(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getDiscount(), savedUser.getRole());
    }

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getAccount(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByAccount(request.getAccount())
                .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND.value()));

        String token = jwtUtil.generateToken(request.getAccount(), user.getId());

        return new JwtResponse(token, user.getId(), user.getAccount(), user.getUsername(), user.getEmail(), user.getDiscount(), user.getRole());
    }

    public User getCurrentUser() {
        String account = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND.value()));
    }

    public UserResponse getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new CustomException("请先登录", HttpStatus.UNAUTHORIZED.value());
        }

        User user = getCurrentUser();
        return new UserResponse(user.getId(), user.getAccount(), user.getUsername(), user.getEmail(), user.getPhone(), user.getCreatedAt());
    }

    public UserResponse updateUser(UpdateUserRequest request) {
        User user = getCurrentUser();

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            if (!request.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
                throw new CustomException("用户名已被使用", HttpStatus.BAD_REQUEST.value());
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new CustomException("邮箱已被使用", HttpStatus.BAD_REQUEST.value());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser.getId(), savedUser.getAccount(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getPhone(), savedUser.getCreatedAt());
    }

    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new CustomException("原密码不正确", HttpStatus.BAD_REQUEST.value());
        }
        
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new CustomException("新密码长度至少为6位", HttpStatus.BAD_REQUEST.value());
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}