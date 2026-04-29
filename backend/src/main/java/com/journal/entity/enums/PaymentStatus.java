package com.journal.entity.enums;

public enum PaymentStatus {
    PENDING("待支付"),
    PAID("已支付"),
    REFUNDED("已退款");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
