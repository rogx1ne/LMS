package com.library.model;

public class Seller {
    private final String sellerId;
    private final String companyName;
    private final String companyContactNo;
    private final String companyMail;
    private final String contactPerson;
    private final String contactPersonNo;
    private final String contactPersonMail;
    private final String address;

    public Seller(
        String sellerId,
        String companyName,
        String companyContactNo,
        String companyMail,
        String contactPerson,
        String contactPersonNo,
        String contactPersonMail,
        String address
    ) {
        this.sellerId = sellerId;
        this.companyName = companyName;
        this.companyContactNo = companyContactNo;
        this.companyMail = companyMail;
        this.contactPerson = contactPerson;
        this.contactPersonNo = contactPersonNo;
        this.contactPersonMail = contactPersonMail;
        this.address = address;
    }

    public String getSellerId() { return sellerId; }
    public String getCompanyName() { return companyName; }
    public String getCompanyContactNo() { return companyContactNo; }
    public String getCompanyMail() { return companyMail; }
    public String getContactPerson() { return contactPerson; }
    public String getContactPersonNo() { return contactPersonNo; }
    public String getContactPersonMail() { return contactPersonMail; }
    public String getAddress() { return address; }
}
