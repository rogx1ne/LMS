package com.library.service;

import com.library.model.OrderDetail;
import com.library.model.Seller;

import java.util.List;

public class ProcurementValidationService {

    public Seller validateSeller(Seller seller) throws ValidationException {
        if (seller == null) throw new ValidationException("Seller data is required.");

        String companyName = required(seller.getCompanyName(), "Company Name");
        String companyContact = validatePhone(seller.getCompanyContactNo(), "Company Contact No");
        String companyMail = validateEmail(seller.getCompanyMail(), "Company Mail");
        String contactPerson = required(seller.getContactPerson(), "Contact Person");
        String contactNo = validatePhone(seller.getContactPersonNo(), "Contact Person No");
        String contactMail = validateEmail(seller.getContactPersonMail(), "Contact Person Mail");
        String address = required(seller.getAddress(), "Address");

        return new Seller(
            seller.getSellerId(),
            companyName,
            companyContact,
            companyMail,
            contactPerson,
            contactNo,
            contactMail,
            address
        );
    }

    public void validateOrder(String sellerId, List<OrderDetail> details) throws ValidationException {
        required(sellerId, "Seller ID");
        if (details == null || details.isEmpty()) {
            throw new ValidationException("Add at least one book detail before placing order.");
        }
        for (int i = 0; i < details.size(); i++) {
            OrderDetail d = details.get(i);
            if (d == null) throw new ValidationException("Order detail row " + (i + 1) + " is empty.");
            required(d.getBookTitle(), "Book Title (row " + (i + 1) + ")");
            required(d.getAuthor(), "Author (row " + (i + 1) + ")");
            required(d.getPublication(), "Publication (row " + (i + 1) + ")");
            if (d.getQuantity() <= 0) throw new ValidationException("Quantity must be > 0 (row " + (i + 1) + ").");
        }
    }

    public OrderDetail validateOrderDetailInput(String title, String author, String publication, String qty) throws ValidationException {
        String t = required(title, "Book Title");
        String a = required(author, "Author");
        String p = required(publication, "Publication");
        if (qty == null || !qty.trim().matches("\\d+")) {
            throw new ValidationException("Quantity must be numeric.");
        }
        int quantity = Integer.parseInt(qty.trim());
        if (quantity <= 0) throw new ValidationException("Quantity must be greater than 0.");
        return new OrderDetail(null, t, a, p, quantity);
    }

    private String required(String raw, String field) throws ValidationException {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) throw new ValidationException(field + " is required.");
        return value;
    }

    private String validatePhone(String raw, String field) throws ValidationException {
        String value = required(raw, field);
        if (!value.matches("\\d{10}")) throw new ValidationException(field + " must be exactly 10 digits.");
        return value;
    }

    private String validateEmail(String raw, String field) throws ValidationException {
        String value = required(raw, field);
        if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidationException(field + " is invalid.");
        }
        return value;
    }
}
