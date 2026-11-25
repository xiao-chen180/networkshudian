package com.example.bookstore.controller;

import com.example.bookstore.dto.OrderCreateRequest;
import com.example.bookstore.dto.OrderDTO;
import com.example.bookstore.dto.OrderItemDTO;
import com.example.bookstore.dto.OrderItemRequest;
import com.example.bookstore.dto.OrderPendingRequest;
import com.example.bookstore.dto.OrderStatusRequest;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUser currentUser;

    public OrderController(OrderService orderService, CurrentUser currentUser) {
        this.orderService = orderService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        OrderDTO order = orderService.createOrderWithBalance(
                userId, request.addressId(), toOrderItems(request.items()), request.paymentMethod());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/pending")
    public ResponseEntity<OrderDTO> createPendingOrder(@Valid @RequestBody OrderPendingRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        OrderDTO order;
        if (request.items() != null && !request.items().isEmpty()) {
            order = orderService.createPendingOrderWithItems(
                    userId, request.shippingAddress(), request.phone(), toOrderItems(request.items()));
        } else {
            order = orderService.createPendingOrder(userId, request.shippingAddress(), request.phone());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrdersByUser() {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(orderService.getOrderById(orderId, userId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long orderId,
                                                      @Valid @RequestBody OrderStatusRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, userId, request.status()));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        orderService.deleteOrder(orderId, userId);
        return ResponseEntity.noContent().build();
    }

    /** 价格由后端按书籍原价与用户折扣计算，前端价格字段不再传入。 */
    private List<OrderItemDTO> toOrderItems(List<OrderItemRequest> items) {
        return items.stream()
                .map(item -> new OrderItemDTO(
                        item.bookId(), "", "", item.quantity(), BigDecimal.ZERO))
                .toList();
    }
}