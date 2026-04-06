package com.library.model;

import java.math.BigDecimal;
import java.sql.Date;

public class BillItem {
    private final String billId;
    private final String sellerId;
    private final String title;
    private final String author;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final Date billDate;
    private final int tax;
    private final BigDecimal totalAmount;
    private final BigDecimal grandTotal;

    public BillItem(String billId, String sellerId, String title, String author, int quantity, 
                    BigDecimal unitPrice, Date billDate, int tax, BigDecimal totalAmount, BigDecimal grandTotal) {
        this.billId = billId;
        this.sellerId = sellerId;
        this.title = title;
        this.author = author;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.billDate = billDate;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.grandTotal = grandTotal;
    }

    public String getBillId() { return billId; }
    public String getSellerId() { return sellerId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Date getBillDate() { return billDate; }
    public int getTax() { return tax; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getGrandTotal() { return grandTotal; }
}
