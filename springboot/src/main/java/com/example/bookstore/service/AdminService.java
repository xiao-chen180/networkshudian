package com.example.bookstore.service;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.OrderItem;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    public AdminService(UserRepository userRepository, OrderRepository orderRepository,
                        BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    // ========== Dashboard 统计 ==========
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        List<User> users = userRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        List<Book> books = bookRepository.findAll();

        long totalUsers = users.stream().filter(u -> "USER".equals(u.getRole())).count();
        long totalAdmins = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long totalOrders = orders.size();
        long pendingOrders = orders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        long paidOrders = orders.stream().filter(o -> "PAID".equals(o.getStatus())).count();
        long shippedOrders = orders.stream().filter(o -> "SHIPPED".equals(o.getStatus())).count();
        long completedOrders = orders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();
        long cancelledOrders = orders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();

        BigDecimal totalRevenue = orders.stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalBooks = books.size();
        long lowStockBooks = books.stream().filter(b -> b.getStock() < 10).count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalAdmins", totalAdmins);
        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("paidOrders", paidOrders);
        stats.put("shippedOrders", shippedOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("cancelledOrders", cancelledOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalBooks", totalBooks);
        stats.put("lowStockBooks", lowStockBooks);

        return stats;
    }

    // ========== 用户管理 ==========
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("account", user.getAccount());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            map.put("phone", user.getPhone());
            map.put("role", user.getRole());
            map.put("discount", user.getDiscount());
            map.put("createdAt", user.getCreatedAt());
            long orderCount = orderRepository.findByUserId(user.getId()).size();
            map.put("orderCount", orderCount);
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND.value()));
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND.value()));
        List<Order> orders = orderRepository.findByUserId(userId);
        if (!orders.isEmpty()) {
            throw new CustomException("该用户还有订单，无法删除", HttpStatus.BAD_REQUEST.value());
        }
        userRepository.delete(user);
    }

    // ========== 订单管理 ==========
    public List<Map<String, Object>> getAllOrders() {
        return orderRepository.findAll().stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("userId", order.getUser().getId());
            map.put("username", order.getUser().getUsername());
            map.put("account", order.getUser().getAccount());
            map.put("totalPrice", order.getTotalPrice());
            map.put("status", order.getStatus());
            map.put("shippingAddress", order.getShippingAddress());
            map.put("phone", order.getPhone());
            map.put("createdAt", order.getCreatedAt());

            List<Map<String, Object>> items = order.getItems().stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("bookId", item.getBook().getId());
                itemMap.put("bookName", item.getBook().getName());
                itemMap.put("author", item.getBook().getAuthor());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("price", item.getPrice());
                return itemMap;
            }).collect(Collectors.toList());
            map.put("items", items);

            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("订单不存在", HttpStatus.NOT_FOUND.value()));
        if (!"PAID".equals(order.getStatus())) {
            throw new CustomException("只有已付款的订单才能发货", HttpStatus.BAD_REQUEST.value());
        }
        order.setStatus("SHIPPED");
        orderRepository.save(order);
    }

    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("订单不存在", HttpStatus.NOT_FOUND.value()));
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new CustomException("只有已发货的订单才能确认完成", HttpStatus.BAD_REQUEST.value());
        }
        order.setStatus("COMPLETED");
        orderRepository.save(order);
    }

    @Transactional
    public void adminCancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("订单不存在", HttpStatus.NOT_FOUND.value()));
        if ("CANCELLED".equals(order.getStatus())) {
            throw new CustomException("订单已被取消", HttpStatus.BAD_REQUEST.value());
        }
        if ("COMPLETED".equals(order.getStatus())) {
            throw new CustomException("已完成的订单无法取消", HttpStatus.BAD_REQUEST.value());
        }
        // 恢复库存；已支付订单同时回退销量
        boolean wasPaid = "PAID".equals(order.getStatus());
        for (OrderItem item : order.getItems()) {
            Book book = item.getBook();
            book.setStock(book.getStock() + item.getQuantity());
            if (wasPaid) {
                int current = book.getSalesCount() != null ? book.getSalesCount() : 0;
                book.setSalesCount(Math.max(0, current - item.getQuantity()));
            }
            bookRepository.save(book);
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    // ========== 书籍批量调价 ==========
    @Transactional
    public int batchUpdatePrice(Long categoryId, BigDecimal adjustment, String type) {
        List<Book> books;
        if (categoryId != null) {
            books = bookRepository.findByCategoryId(categoryId);
        } else {
            books = bookRepository.findAll();
        }

        int count = 0;
        for (Book book : books) {
            BigDecimal newPrice;
            if ("percent".equals(type)) {
                newPrice = book.getPrice().multiply(BigDecimal.ONE.add(adjustment.divide(BigDecimal.valueOf(100))));
            } else {
                newPrice = book.getPrice().add(adjustment);
            }
            if (newPrice.compareTo(BigDecimal.ZERO) > 0) {
                book.setPrice(newPrice.setScale(2, java.math.RoundingMode.HALF_UP));
                bookRepository.save(book);
                count++;
            }
        }
        return count;
    }
}
