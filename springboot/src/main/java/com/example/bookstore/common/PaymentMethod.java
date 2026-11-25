package com.example.bookstore.common;

/** 支付方式。前端传入小写字符串，此处统一解析。 */
public enum PaymentMethod {
    BALANCE, ALIPAY, WECHAT;

    /** 兼容前端传入的 "balance"/"alipay"/"wechat"，未知值返回 null。 */
    public static PaymentMethod from(String value) {
        if (value == null) return null;
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
