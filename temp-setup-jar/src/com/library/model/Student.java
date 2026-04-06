package com.library.model;

import java.sql.Date;

public class Student {
    private String cardId;
    private int roll;
    private String name;
    private long phone;
    private String address;
    private String course;
    private String session;
    private String receiptNo; // Changed to String to handle "LRYYNNN" format logic easier, though DB is Number, we can parse.
    // Actually DB says NUMBER(7), but format is LRYYNNN. LR is text. 
    // Correction: If DB is NUMBER, it cannot store "LR". 
    // Assumption: The DB schema provided has RECEIPT_NO NUMBER(7). 
    // However, the requirement says format "LRYYNNN". "LR" are letters. 
    // I will assume the DB column should be VARCHAR2 to store "LR". 
    // If it MUST be NUMBER, we can only store the YYNNN part. 
    // *Decision*: I will use String in Java. In DAO, I will assume the column was updated to VARCHAR2 or I will strip "LR" if forced to NUMBER.
    // Given the prompt "RECEIPT_NO NUMBER(7)", it's likely the "LR" is a visual prefix or the DB type description in prompt had a typo vs the format requirement.
    // I will use String here and assume the table supports the format.
    
    private String issuedBy;
    private Date issueDate;
    private int bookLimit;
    private double fee;
    private String status;

    public Student(String cardId, int roll, String name, long phone, String address, 
                   String course, String session, String receiptNo, String issuedBy, 
                   Date issueDate, int bookLimit, double fee, String status) {
        this.cardId = cardId;
        this.roll = roll;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.course = course;
        this.session = session;
        this.receiptNo = receiptNo;
        this.issuedBy = issuedBy;
        this.issueDate = issueDate;
        this.bookLimit = bookLimit;
        this.fee = fee;
        this.status = status;
    }

    // Getters
    public String getCardId() { return cardId; }
    public int getRoll() { return roll; }
    public String getName() { return name; }
    public long getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCourse() { return course; }
    public String getSession() { return session; }
    public String getReceiptNo() { return receiptNo; }
    public String getIssuedBy() { return issuedBy; }
    public Date getIssueDate() { return issueDate; }
    public int getBookLimit() { return bookLimit; }
    public double getFee() { return fee; }
    public String getStatus() { return status; }
}