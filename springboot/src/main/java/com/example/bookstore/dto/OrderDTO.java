package com.example.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {

    private Long id;
    private Long userId;
    private BigDecimal totalPrice;
    private String status;
    private String shippingAddress;
    private String phone;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;

    public OrderDTO() {}

    public OrderDTO(Long id, Long userId, BigDecimal totalPrice, String status, 
                   String shippingAddress, String phone, LocalDateTime createdAt, List<OrderItemDTO> items) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.createdAt = createdAt;
        this.items = items;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}