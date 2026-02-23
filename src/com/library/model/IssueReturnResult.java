package com.library.model;

import java.math.BigDecimal;
import java.sql.Date;

public class IssueReturnResult {
    private final String issueId;
    private final String accessionNo;
    private final Date returnDate;
    private final BigDecimal fine;

    public IssueReturnResult(String issueId, String accessionNo, Date returnDate, BigDecimal fine) {
        this.issueId = issueId;
        this.accessionNo = accessionNo;
        this.returnDate = returnDate;
        this.fine = fine;
    }

    public String getIssueId() { return issueId; }
    public String getAccessionNo() { return accessionNo; }
    public Date getReturnDate() { return returnDate; }
    public BigDecimal getFine() { return fine; }
}
