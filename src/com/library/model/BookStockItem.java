package com.library.model;

public class BookStockItem {
    private final String title;
    private final String authorName;
    private final int edition;
    private final String publication;
    private final int quantity;

    public BookStockItem(String title, String authorName, int edition, String publication, int quantity) {
        this.title = title;
        this.authorName = authorName;
        this.edition = edition;
        this.publication = publication;
        this.quantity = quantity;
    }

    public String getTitle() { return title; }
    public String getAuthorName() { return authorName; }
    public int getEdition() { return edition; }
    public String getPublication() { return publication; }
    public int getQuantity() { return quantity; }
}
