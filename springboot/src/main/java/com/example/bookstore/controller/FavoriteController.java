package com.example.bookstore.controller;

import com.example.bookstore.dto.FavoriteDTO;
import com.example.bookstore.security.CurrentUser;
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
    private final CurrentUser currentUser;

    public FavoriteController(FavoriteService favoriteService, CurrentUser currentUser) {
        this.favoriteService = favoriteService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<FavoriteDTO>> getMyFavorites(HttpServletRequest request) {
        Long userId = currentUser.getId();
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
        Long userId = currentUser.getId();
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
        Long userId = currentUser.getId();
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
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        favoriteService.removeFavorite(userId, bookId);
        return ResponseEntity.noContent().build();
    }
}
