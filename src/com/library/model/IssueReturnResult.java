package com.library.model;

import java.math.BigDecimal;
import java.sql.Date;

public class IssueReturnResult {
    private final String issueId;
    private final String accessionNo;
    private final Date returnDate;
    private final String returnCondition;
    private final BigDecimal lateFine;
    private final BigDecimal inspectionFine;
    private final BigDecimal fine;

    public IssueReturnResult(
        String issueId,
        String accessionNo,
        Date returnDate,
        String returnCondition,
        BigDecimal lateFine,
        BigDecimal inspectionFine,
        BigDecimal fine
    ) {
        this.issueId = issueId;
        this.accessionNo = accessionNo;
        this.returnDate = returnDate;
        this.returnCondition = returnCondition;
        this.lateFine = lateFine;
        this.inspectionFine = inspectionFine;
        this.fine = fine;
    }

    public String getIssueId() { return issueId; }
    public String getAccessionNo() { return accessionNo; }
    public Date getReturnDate() { return returnDate; }
    public String getReturnCondition() { return returnCondition; }
    public BigDecimal getLateFine() { return lateFine; }
    public BigDecimal getInspectionFine() { return inspectionFine; }
    public BigDecimal getFine() { return fine; }
}
