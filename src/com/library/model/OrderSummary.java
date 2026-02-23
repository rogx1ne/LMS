package com.library.model;

import java.sql.Date;

public class OrderSummary {
    private final String orderId;
    private final Date orderDate;

    public OrderSummary(String orderId, Date orderDate) {
        this.orderId = orderId;
        this.orderDate = orderDate;
    }

    public String getOrderId() { return orderId; }
    public Date getOrderDate() { return orderDate; }
}
