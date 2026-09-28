package com.example.platform.error;

import org.springframework.http.HttpStatus;

/** Example domain enum. Replace with your real domains. */
public enum OrderErrorCode implements ErrorCode {
    ORDER_ALREADY_SHIPPED("ORD-B-1001", "Order already shipped", HttpStatus.CONFLICT),
    ORDER_LIMIT_EXCEEDED("ORD-B-1002", "Order quantity limit exceeded", HttpStatus.UNPROCESSABLE_ENTITY),
    INVENTORY_SERVICE_TIMEOUT("ORD-S-5001", "Inventory service timed out", HttpStatus.GATEWAY_TIMEOUT),
    PRICING_RULE_MISSING("ORD-P-2001", "Pricing configuration missing", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String title;
    private final HttpStatus status;

    OrderErrorCode(String code, String title, HttpStatus status) {
        this.code = code;
        this.title = title;
        this.status = status;
    }

    @Override public String code() { return code; }
    @Override public String title() { return title; }
    @Override public HttpStatus status() { return status; }
}
