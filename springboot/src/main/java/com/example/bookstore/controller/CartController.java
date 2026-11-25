package com.example.bookstore.controller;

import com.example.bookstore.dto.CartItemDTO;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final CurrentUser currentUser;

    public CartController(CartService cartService, CurrentUser currentUser) {
        this.cartService = cartService;
        this.currentUser = currentUser;
    }

    @PostMapping("/add")
    public ResponseEntity<CartItemDTO> addToCart(
            HttpServletRequest request,
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        logger.info("addToCart called with userId: {}, bookId: {}, quantity: {}", userId, bookId, quantity);

        try {
            CartItemDTO cartItem = cartService.addToCart(userId, bookId, quantity);
            logger.info("Successfully added to cart: userId={}, bookId={}", userId, bookId);
            return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
        } catch (Exception e) {
            logger.error("Failed to add to cart: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCartItems(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<CartItemDTO> cartItems = cartService.getCartItems(userId);
        return ResponseEntity.ok(cartItems);
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItemDTO> updateCartItem(
            HttpServletRequest request,
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> quantityRequest) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Integer quantity = quantityRequest.get("quantity");
        CartItemDTO cartItem = cartService.updateCartItem(userId, cartItemId, quantity);
        return ResponseEntity.ok(cartItem);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(
            HttpServletRequest request,
            @PathVariable Long cartItemId) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
