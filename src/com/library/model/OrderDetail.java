package com.library.model;

public class OrderDetail {
    private final String orderId;
    private final String bookTitle;
    private final String author;
    private final String publication;
    private final int quantity;

    public OrderDetail(String orderId, String bookTitle, String author, String publication, int quantity) {
        this.orderId = orderId;
        this.bookTitle = bookTitle;
        this.author = author;
        this.publication = publication;
        this.quantity = quantity;
    }

    public String getOrderId() { return orderId; }
    public String getBookTitle() { return bookTitle; }
    public String getAuthor() { return author; }
    public String getPublication() { return publication; }
    public int getQuantity() { return quantity; }
}
