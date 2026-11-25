package com.example.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CouponDTO {

    private Long id;
    private String name;
    private String type;
    private BigDecimal value;
    private BigDecimal minAmount;
    private Integer totalNum;
    private Integer usedNum;
    private Integer remainNum;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public CouponDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public Integer getTotalNum() { return totalNum; }
    public void setTotalNum(Integer totalNum) { this.totalNum = totalNum; }

    public Integer getUsedNum() { return usedNum; }
    public void setUsedNum(Integer usedNum) { this.usedNum = usedNum; }

    public Integer getRemainNum() { return remainNum; }
    public void setRemainNum(Integer remainNum) { this.remainNum = remainNum; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
