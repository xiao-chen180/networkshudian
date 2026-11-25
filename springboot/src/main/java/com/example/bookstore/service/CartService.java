
package com.example.bookstore.service;

import com.example.bookstore.dto.CartItemDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.CartItem;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartItemRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository, BookRepository bookRepository,
                      UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartItemDTO addToCart(Long userId, Long bookId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND.value()));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException("书籍不存在", HttpStatus.NOT_FOUND.value()));

        if (book.getStock() < quantity) {
            throw new CustomException("库存不足", HttpStatus.BAD_REQUEST.value());
        }

        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndBookId(userId, bookId);
        CartItem cartItem;

        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            if (book.getStock() < newQuantity) {
                throw new CustomException("库存不足", HttpStatus.BAD_REQUEST.value());
            }
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setBook(book);
            cartItem.setQuantity(quantity);
        }

        CartItem savedItem = cartItemRepository.save(cartItem);
        return convertToDTO(savedItem);
    }

    public List<CartItemDTO> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartItemDTO updateCartItem(Long userId, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException("购物车项不存在", HttpStatus.NOT_FOUND.value()));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new CustomException("无权修改此购物车项", HttpStatus.FORBIDDEN.value());
        }

        if (cartItem.getBook().getStock() < quantity) {
            throw new CustomException("库存不足", HttpStatus.BAD_REQUEST.value());
        }

        cartItem.setQuantity(quantity);
        CartItem updatedItem = cartItemRepository.save(cartItem);
        return convertToDTO(updatedItem);
    }

    @Transactional
    public void removeFromCart(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException("购物车项不存在", HttpStatus.NOT_FOUND.value()));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new CustomException("无权删除此购物车项", HttpStatus.FORBIDDEN.value());
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemDTO convertToDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setBookId(cartItem.getBook().getId());
        dto.setBookName(cartItem.getBook().getName());
        dto.setAuthor(cartItem.getBook().getAuthor());
        dto.setPrice(cartItem.getBook().getPrice());
        dto.setQuantity(cartItem.getQuantity());
        dto.setStock(cartItem.getBook().getStock());
        return dto;
    }
}
