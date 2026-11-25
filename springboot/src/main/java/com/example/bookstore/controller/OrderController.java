
package com.example.bookstore.controller;

import com.example.bookstore.dto.OrderDTO;
import com.example.bookstore.dto.OrderItemDTO;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<OrderDTO> createOrder(HttpServletRequest request, @RequestBody Map<String, Object> requestBody) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long addressId = requestBody.get("addressId") != null ?
            Long.parseLong(requestBody.get("addressId").toString()) : null;

        List<Map<String, Object>> items = (List<Map<String, Object>>) requestBody.get("items");
        List<OrderItemDTO> orderItems = items.stream()
            .map(item -> new OrderItemDTO(
                Long.parseLong(item.get("bookId").toString()),
                "",
                "",
                Integer.parseInt(item.get("quantity").toString()),
                new BigDecimal(item.get("price").toString())
            ))
            .toList();

        BigDecimal totalPrice = new BigDecimal(requestBody.get("totalPrice").toString());
        String paymentMethod = (String) requestBody.get("paymentMethod");

        OrderDTO order = orderService.createOrderWithBalance(userId, addressId, orderItems, totalPrice, paymentMethod);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/pending")
    public ResponseEntity<OrderDTO> createPendingOrder(HttpServletRequest request, @RequestBody Map<String, Object> requestBody) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String shippingAddress = (String) requestBody.get("shippingAddress");
        String phone = (String) requestBody.get("phone");

        List<Map<String, Object>> items = (List<Map<String, Object>>) requestBody.get("items");

        if (items != null && !items.isEmpty()) {
            List<OrderItemDTO> orderItems = items.stream()
                .map(item -> new OrderItemDTO(
                    Long.parseLong(item.get("bookId").toString()),
                    "",
                    "",
                    Integer.parseInt(item.get("quantity").toString()),
                    new BigDecimal(item.get("price").toString())
                ))
                .toList();

            BigDecimal totalPrice = requestBody.get("totalPrice") != null ?
                new BigDecimal(requestBody.get("totalPrice").toString()) : BigDecimal.ZERO;

            OrderDTO order = orderService.createPendingOrderWithItems(userId, shippingAddress, phone, orderItems, totalPrice);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        }

        OrderDTO order = orderService.createPendingOrder(userId, shippingAddress, phone);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<OrderDTO> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(
            HttpServletRequest request,
            @PathVariable Long orderId) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        OrderDTO order = orderService.getOrderById(orderId, userId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            HttpServletRequest request,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> statusRequest) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String status = statusRequest.get("status");
        OrderDTO order = orderService.updateOrderStatus(orderId, userId, status);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            HttpServletRequest request,
            @PathVariable Long orderId) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        orderService.deleteOrder(orderId, userId);
        return ResponseEntity.noContent().build();
    }
}
