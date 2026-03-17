package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.BillItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public void createBill(List<BillItem> items) throws SQLException {
        String sql = "INSERT INTO TBL_BILL (B_ID, S_ID, TTL, AUTHOR, QUANTITY, U_PRICE, B_DATE, TAX, TTL_AMUT, GR_TTL) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Connection is null");
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (BillItem item : items) {
                    ps.setString(1, item.getBillId());
                    ps.setString(2, item.getSellerId());
                    ps.setString(3, item.getTitle());
                    ps.setString(4, item.getAuthor());
                    ps.setInt(5, item.getQuantity());
                    ps.setBigDecimal(6, item.getUnitPrice());
                    ps.setDate(7, item.getBillDate());
                    ps.setInt(8, item.getTax());
                    ps.setBigDecimal(9, item.getTotalAmount());
                    ps.setBigDecimal(10, item.getGrandTotal());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<BillItem> getBillItems(String billId) {
        List<BillItem> out = new ArrayList<>();
        String sql = "SELECT * FROM TBL_BILL WHERE B_ID = ? ORDER BY TTL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new BillItem(
                        rs.getString("B_ID"),
                        rs.getString("S_ID"),
                        rs.getString("TTL"),
                        rs.getString("AUTHOR"),
                        rs.getInt("QUANTITY"),
                        rs.getBigDecimal("U_PRICE"),
                        rs.getDate("B_DATE"),
                        rs.getInt("TAX"),
                        rs.getBigDecimal("TTL_AMUT"),
                        rs.getBigDecimal("GR_TTL")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public int getRemainingQuantity(String billId, String title, String author) {
        String billQtySql = "SELECT QUANTITY FROM TBL_BILL WHERE B_ID = ? AND TTL = ? AND AUTHOR = ?";
        String registeredQtySql = "SELECT COUNT(*) FROM TBL_BOOK_INFORMATION WHERE B_NO = ? AND BK_TITLE = ? AND AUTHOR_NAME = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            int billQty = 0;
            try (PreparedStatement ps = conn.prepareStatement(billQtySql)) {
                ps.setString(1, billId);
                ps.setString(2, title);
                ps.setString(3, author);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) billQty = rs.getInt("QUANTITY");
                    else return -1; // Item not on bill
                }
            }
            
            int registeredQty = 0;
            try (PreparedStatement ps = conn.prepareStatement(registeredQtySql)) {
                ps.setString(1, billId);
                ps.setString(2, title);
                ps.setString(3, author);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) registeredQty = rs.getInt(1);
                }
            }
            
            return billQty - registeredQty;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean billExists(String billId) {
        String sql = "SELECT 1 FROM TBL_BILL WHERE B_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
