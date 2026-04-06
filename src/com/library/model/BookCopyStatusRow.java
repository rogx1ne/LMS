package com.library.model;

import java.sql.Date;

public class BookCopyStatusRow {
    private final String accessionNo;
    private final String status;
    private final Date withdrawnDate;

    public BookCopyStatusRow(String accessionNo, String status, Date withdrawnDate) {
        this.accessionNo = accessionNo;
        this.status = status;
        this.withdrawnDate = withdrawnDate;
    }

    public String getAccessionNo() { return accessionNo; }
    public String getStatus() { return status; }
    public Date getWithdrawnDate() { return withdrawnDate; }
}
