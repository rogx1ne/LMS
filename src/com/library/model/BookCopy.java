package com.library.model;

import java.math.BigDecimal;
import java.sql.Date;

public class BookCopy {
    private final String accessionNo;
    private final String authorName;
    private final String title;
    private final Integer volume;
    private final int edition;
    private final String publisher;
    private final String publicationPlace;
    private final int publicationYear;
    private final int pages;
    private final String source;
    private final String classNo;
    private final String bookNo;
    private final BigDecimal cost;
    private final String billNo;
    private final Date billDate;
    private final Date withdrawnDate;
    private final String remarks;
    private final String status;

    public BookCopy(
        String accessionNo,
        String authorName,
        String title,
        Integer volume,
        int edition,
        String publisher,
        String publicationPlace,
        int publicationYear,
        int pages,
        String source,
        String classNo,
        String bookNo,
        BigDecimal cost,
        String billNo,
        Date billDate,
        Date withdrawnDate,
        String remarks,
        String status
    ) {
        this.accessionNo = accessionNo;
        this.authorName = authorName;
        this.title = title;
        this.volume = volume;
        this.edition = edition;
        this.publisher = publisher;
        this.publicationPlace = publicationPlace;
        this.publicationYear = publicationYear;
        this.pages = pages;
        this.source = source;
        this.classNo = classNo;
        this.bookNo = bookNo;
        this.cost = cost;
        this.billNo = billNo;
        this.billDate = billDate;
        this.withdrawnDate = withdrawnDate;
        this.remarks = remarks;
        this.status = status;
    }

    public String getAccessionNo() { return accessionNo; }
    public String getAuthorName() { return authorName; }
    public String getTitle() { return title; }
    public Integer getVolume() { return volume; }
    public int getEdition() { return edition; }
    public String getPublisher() { return publisher; }
    public String getPublicationPlace() { return publicationPlace; }
    public int getPublicationYear() { return publicationYear; }
    public int getPages() { return pages; }
    public String getSource() { return source; }
    public String getClassNo() { return classNo; }
    public String getBookNo() { return bookNo; }
    public BigDecimal getCost() { return cost; }
    public String getBillNo() { return billNo; }
    public Date getBillDate() { return billDate; }
    public Date getWithdrawnDate() { return withdrawnDate; }
    public String getRemarks() { return remarks; }
    public String getStatus() { return status; }
}
