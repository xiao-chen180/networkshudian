
package com.example.bookstore.security;

import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        User user = userRepository.findByAccount(account)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + account));

        return new org.springframework.security.core.userdetails.User(
                user.getAccount(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
