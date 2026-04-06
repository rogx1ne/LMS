package com.library.model;

import java.sql.Date;

public class BorrowRecord {
    private final String accessNo;
    private final String bookTitle;
    private final String authorName;
    private final Date issueDate;
    private final Date dueDate;
    private final Date returnDate;
    private final Double fineAmount;

    public BorrowRecord(
        String accessNo,
        String bookTitle,
        String authorName,
        Date issueDate,
        Date dueDate,
        Date returnDate,
        Double fineAmount
    ) {
        this.accessNo = accessNo;
        this.bookTitle = bookTitle;
        this.authorName = authorName;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
    }

    public String getAccessNo() { return accessNo; }
    public String getBookTitle() { return bookTitle; }
    public String getAuthorName() { return authorName; }
    public Date getIssueDate() { return issueDate; }
    public Date getDueDate() { return dueDate; }
    public Date getReturnDate() { return returnDate; }
    public Double getFineAmount() { return fineAmount; }
}
