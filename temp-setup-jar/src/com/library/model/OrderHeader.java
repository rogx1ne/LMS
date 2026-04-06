package com.library.model;

import java.sql.Date;

public class OrderHeader {
    private final String orderId;
    private final String sellerId;
    private final Date orderDate;

    public OrderHeader(String orderId, String sellerId, Date orderDate) {
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.orderDate = orderDate;
    }

    public String getOrderId() { return orderId; }
    public String getSellerId() { return sellerId; }
    public Date getOrderDate() { return orderDate; }
}
