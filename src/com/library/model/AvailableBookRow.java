package com.library.model;

public class AvailableBookRow {
    private final String accessionNo;
    private final String title;
    private final String authorName;

    public AvailableBookRow(String accessionNo, String title, String authorName) {
        this.accessionNo = accessionNo;
        this.title = title;
        this.authorName = authorName;
    }

    public String getAccessionNo() { return accessionNo; }
    public String getTitle() { return title; }
    public String getAuthorName() { return authorName; }
}
