package com.example.bookstore.controller;

import com.example.bookstore.dto.FavoriteDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtUtil;
import com.example.bookstore.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public FavoriteController(FavoriteService favoriteService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.favoriteService = favoriteService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                String account = jwtUtil.extractUsername(token);
                User user = userRepository.findByAccount(account).orElse(null);
                if (user != null) return user.getId();
            } catch (Exception e) {}
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<FavoriteDTO>> getMyFavorites(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<FavoriteDTO> favorites = favoriteService.getFavoritesByUserId(userId);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/check/{bookId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            HttpServletRequest request,
            @PathVariable Long bookId) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isFavorited = favoriteService.isFavorited(userId, bookId);
        return ResponseEntity.ok(Map.of("isFavorited", isFavorited));
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<FavoriteDTO> addFavorite(
            HttpServletRequest request,
            @PathVariable Long bookId) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        FavoriteDTO favorite = favoriteService.addFavorite(userId, bookId);
        return ResponseEntity.status(HttpStatus.CREATED).body(favorite);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> removeFavorite(
            HttpServletRequest request,
            @PathVariable Long bookId) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        favoriteService.removeFavorite(userId, bookId);
        return ResponseEntity.noContent().build();
    }
}
