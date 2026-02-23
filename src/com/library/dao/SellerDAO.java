package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.Seller;
import com.library.service.ProcurementIdGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SellerDAO {
    private final ProcurementIdGenerator idGenerator = new ProcurementIdGenerator();
    private static final String[] REQUIRED_COLUMNS = {
        "S_ID", "COMPANY_NAME", "COMPANY_CONTACT_NO", "COMPANY_MAIL",
        "CONTACT_PERSON", "CONTACT_PERSON_NO", "CONTACT_PERSON_MAIL", "ADDR"
    };

    public boolean isSellerSchemaAvailable() {
        String sql = "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'TBL_SELLER' AND COLUMN_NAME = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            int found = 0;
            for (String col : REQUIRED_COLUMNS) {
                ps.setString(1, col);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) found++;
                }
            }
            return found == REQUIRED_COLUMNS.length;
        } catch (SQLException e) {
            return false;
        }
    }

    public String peekNextSellerId() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return "SID0000001";
            return idGenerator.nextSellerId(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return "SID0000001";
        }
    }

    public boolean addSeller(Seller seller) {
        String sql =
            "INSERT INTO TBL_SELLER (S_ID, COMPANY_NAME, COMPANY_CONTACT_NO, COMPANY_MAIL, CONTACT_PERSON, CONTACT_PERSON_NO, CONTACT_PERSON_MAIL, ADDR) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, seller.getSellerId());
            ps.setString(2, seller.getCompanyName());
            ps.setString(3, seller.getCompanyContactNo());
            ps.setString(4, seller.getCompanyMail());
            ps.setString(5, seller.getContactPerson());
            ps.setString(6, seller.getContactPersonNo());
            ps.setString(7, seller.getContactPersonMail());
            ps.setString(8, seller.getAddress());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSeller(Seller seller) {
        String sql =
            "UPDATE TBL_SELLER SET COMPANY_NAME=?, COMPANY_CONTACT_NO=?, COMPANY_MAIL=?, CONTACT_PERSON=?, CONTACT_PERSON_NO=?, CONTACT_PERSON_MAIL=?, ADDR=? " +
            "WHERE S_ID=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, seller.getCompanyName());
            ps.setString(2, seller.getCompanyContactNo());
            ps.setString(3, seller.getCompanyMail());
            ps.setString(4, seller.getContactPerson());
            ps.setString(5, seller.getContactPersonNo());
            ps.setString(6, seller.getContactPersonMail());
            ps.setString(7, seller.getAddress());
            ps.setString(8, seller.getSellerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Seller getSellerById(String sellerId) {
        String sql = "SELECT * FROM TBL_SELLER WHERE S_ID=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSeller(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Seller> getAllSellers() {
        List<Seller> out = new ArrayList<>();
        String sql = "SELECT * FROM TBL_SELLER ORDER BY S_ID DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.add(mapSeller(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public List<String> getAllSellerIds() {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT S_ID FROM TBL_SELLER ORDER BY S_ID";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) ids.add(rs.getString("S_ID"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    private Seller mapSeller(ResultSet rs) throws SQLException {
        return new Seller(
            rs.getString("S_ID"),
            rs.getString("COMPANY_NAME"),
            rs.getString("COMPANY_CONTACT_NO"),
            rs.getString("COMPANY_MAIL"),
            rs.getString("CONTACT_PERSON"),
            rs.getString("CONTACT_PERSON_NO"),
            rs.getString("CONTACT_PERSON_MAIL"),
            rs.getString("ADDR")
        );
    }
}
