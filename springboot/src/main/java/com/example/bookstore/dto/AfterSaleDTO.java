package com.example.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 售后工单，用户端与后台共用 */
public class AfterSaleDTO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private String username;
    private String bookName;
    private Integer quantity;
    private BigDecimal refundAmount;
    private String type;
    private String typeText;
    private String reason;
    private String description;
    private String status;
    private String statusText;
    private String adminRemark;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTypeText() { return typeText; }
    public void setTypeText(String typeText) { this.typeText = typeText; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }

    public String getAdminRemark() { return adminRemark; }
    public void setAdminRemark(String adminRemark) { this.adminRemark = adminRemark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
}