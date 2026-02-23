package com.library.model;

import java.sql.Date;

public class IssueTransaction {
    private final String issueId;
    private final String cardId;
    private final String accessionNo;
    private final String bookTitle;
    private final String authorName;
    private final Date issueDate;
    private final Date dueDate;
    private final Date returnDate;
    private final Double fine;
    private final String issuedBy;
    private final String status;

    public IssueTransaction(
        String issueId,
        String cardId,
        String accessionNo,
        String bookTitle,
        String authorName,
        Date issueDate,
        Date dueDate,
        Date returnDate,
        Double fine,
        String issuedBy,
        String status
    ) {
        this.issueId = issueId;
        this.cardId = cardId;
        this.accessionNo = accessionNo;
        this.bookTitle = bookTitle;
        this.authorName = authorName;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fine = fine;
        this.issuedBy = issuedBy;
        this.status = status;
    }

    public String getIssueId() { return issueId; }
    public String getCardId() { return cardId; }
    public String getAccessionNo() { return accessionNo; }
    public String getBookTitle() { return bookTitle; }
    public String getAuthorName() { return authorName; }
    public Date getIssueDate() { return issueDate; }
    public Date getDueDate() { return dueDate; }
    public Date getReturnDate() { return returnDate; }
    public Double getFine() { return fine; }
    public String getIssuedBy() { return issuedBy; }
    public String getStatus() { return status; }
}
