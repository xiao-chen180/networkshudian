package com.example.bookstore.controller;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.dto.PriceUpdateRequest;
import com.example.bookstore.dto.RoleUpdateRequest;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.AdminService;
import com.example.bookstore.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final BookService bookService;
    private final CurrentUser currentUser;

    public AdminController(AdminService adminService, BookService bookService,
                           CurrentUser currentUser) {
        this.adminService = adminService;
        this.bookService = bookService;
        this.currentUser = currentUser;
    }

    private boolean isAdmin(HttpServletRequest request) {
        return currentUser.isAdmin();
    }

    private ResponseEntity<?> forbiddenIfNotAdmin(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "无管理员权限"));
        }
        return null;
    }

    // ========== Dashboard ==========
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ========== 用户管理 ==========
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(HttpServletRequest request,
                                            @PathVariable Long userId,
                                            @jakarta.validation.Valid @RequestBody RoleUpdateRequest body) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        adminService.updateUserRole(userId, body.role());
        return ResponseEntity.ok(Map.of("message", "角色更新成功"));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(HttpServletRequest request, @PathVariable Long userId) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        adminService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "用户删除成功"));
    }

    // ========== 订单管理 ==========
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders(HttpServletRequest request) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    @PutMapping("/orders/{orderId}/ship")
    public ResponseEntity<?> shipOrder(HttpServletRequest request, @PathVariable Long orderId) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        adminService.shipOrder(orderId);
        return ResponseEntity.ok(Map.of("message", "发货成功"));
    }

    @PutMapping("/orders/{orderId}/complete")
    public ResponseEntity<?> completeOrder(HttpServletRequest request, @PathVariable Long orderId) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        adminService.completeOrder(orderId);
        return ResponseEntity.ok(Map.of("message", "订单已完成"));
    }

    @PutMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(HttpServletRequest request, @PathVariable Long orderId) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        adminService.adminCancelOrder(orderId);
        return ResponseEntity.ok(Map.of("message", "订单已取消"));
    }

    // ========== 书籍管理 ==========
    @GetMapping("/books")
    public ResponseEntity<?> getAllBooks(HttpServletRequest request,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @PostMapping("/books")
    public ResponseEntity<?> createBook(HttpServletRequest request, @RequestBody BookDTO bookDTO) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        BookDTO created = bookService.createBook(bookDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<?> updateBook(HttpServletRequest request,
                                        @PathVariable Long id,
                                        @RequestBody BookDTO bookDTO) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        BookDTO updated = bookService.updateBook(id, bookDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(HttpServletRequest request, @PathVariable Long id) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        bookService.deleteBook(id);
        return ResponseEntity.ok(Map.of("message", "书籍删除成功"));
    }

    @PutMapping("/books/price")
    public ResponseEntity<?> batchUpdatePrice(HttpServletRequest request,
                                              @jakarta.validation.Valid @RequestBody PriceUpdateRequest body) {
        ResponseEntity<?> check = forbiddenIfNotAdmin(request);
        if (check != null) return check;
        String type = body.type() != null ? body.type() : "fixed";
        int count = adminService.batchUpdatePrice(body.categoryId(), body.adjustment(), type);
        return ResponseEntity.ok(Map.of("message", "成功调整 " + count + " 本书籍价格"));
    }
}
