package com.library.model;

import java.math.BigDecimal;
import java.sql.Date;

public class CirculationReportRow {
    private final String issueId;
    private final String borrowerType;
    private final String cardId;
    private final String borrowerName;
    private final String borrowerContact;
    private final String accessionNo;
    private final String bookTitle;
    private final String authorName;
    private final Date issueDate;
    private final Date dueDate;
    private final Date returnDate;
    private final String returnCondition;
    private final BigDecimal fine;
    private final String issuedBy;
    private final String status;

    public CirculationReportRow(
        String issueId,
        String borrowerType,
        String cardId,
        String borrowerName,
        String borrowerContact,
        String accessionNo,
        String bookTitle,
        String authorName,
        Date issueDate,
        Date dueDate,
        Date returnDate,
        String returnCondition,
        BigDecimal fine,
        String issuedBy,
        String status
    ) {
        this.issueId = issueId;
        this.borrowerType = borrowerType;
        this.cardId = cardId;
        this.borrowerName = borrowerName;
        this.borrowerContact = borrowerContact;
        this.accessionNo = accessionNo;
        this.bookTitle = bookTitle;
        this.authorName = authorName;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.returnCondition = returnCondition;
        this.fine = fine;
        this.issuedBy = issuedBy;
        this.status = status;
    }

    public String getIssueId() { return issueId; }
    public String getBorrowerType() { return borrowerType; }
    public String getCardId() { return cardId; }
    public String getBorrowerName() { return borrowerName; }
    public String getBorrowerContact() { return borrowerContact; }
    public String getAccessionNo() { return accessionNo; }
    public String getBookTitle() { return bookTitle; }
    public String getAuthorName() { return authorName; }
    public Date getIssueDate() { return issueDate; }
    public Date getDueDate() { return dueDate; }
    public Date getReturnDate() { return returnDate; }
    public String getReturnCondition() { return returnCondition; }
    public BigDecimal getFine() { return fine; }
    public String getIssuedBy() { return issuedBy; }
    public String getStatus() { return status; }
}
