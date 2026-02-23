package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.OrderDetail;
import com.library.model.OrderHeader;
import com.library.model.OrderSummary;
import com.library.service.ProcurementIdGenerator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private final ProcurementIdGenerator idGenerator = new ProcurementIdGenerator();

    public boolean isOrderSchemaAvailable() {
        String sql = "SELECT COUNT(*) AS C FROM USER_TABLES WHERE TABLE_NAME IN ('TBL_ORDER_HEADER', 'TBL_ORDER_DETAILS')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (conn == null) return false;
            if (rs.next()) return rs.getInt("C") == 2;
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    public String peekNextOrderId() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return "ORD-000001";
            return idGenerator.nextOrderId(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return "ORD-000001";
        }
    }

    public OrderHeader createOrder(String sellerId, Date orderDate, List<OrderDetail> details) throws SQLException {
        String insertHeader = "INSERT INTO TBL_ORDER_HEADER (ORDER_ID, S_ID, ORDER_DATE) VALUES (?, ?, ?)";
        String insertDetail = "INSERT INTO TBL_ORDER_DETAILS (ORDER_ID, BOOK_TITLE, AUTHOR, PUBLICATION, QUANTITY) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("DB connection is null.");
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String orderId = idGenerator.nextOrderId(conn);
                Date dt = orderDate == null ? currentDate(conn) : orderDate;

                try (PreparedStatement ps = conn.prepareStatement(insertHeader)) {
                    ps.setString(1, orderId);
                    ps.setString(2, sellerId);
                    ps.setDate(3, dt);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertDetail)) {
                    for (OrderDetail d : details) {
                        ps.setString(1, orderId);
                        ps.setString(2, d.getBookTitle());
                        ps.setString(3, d.getAuthor());
                        ps.setString(4, d.getPublication());
                        ps.setInt(5, d.getQuantity());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                return new OrderHeader(orderId, sellerId, dt);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAuto);
            }
        }
    }

    public boolean updateOrderReplaceDetails(String orderId, String sellerId, Date orderDate, List<OrderDetail> details) {
        String updateHeader = "UPDATE TBL_ORDER_HEADER SET S_ID=?, ORDER_DATE=? WHERE ORDER_ID=?";
        String deleteDetails = "DELETE FROM TBL_ORDER_DETAILS WHERE ORDER_ID=?";
        String insertDetail = "INSERT INTO TBL_ORDER_DETAILS (ORDER_ID, BOOK_TITLE, AUTHOR, PUBLICATION, QUANTITY) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(updateHeader)) {
                    ps.setString(1, sellerId);
                    ps.setDate(2, orderDate);
                    ps.setString(3, orderId);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteDetails)) {
                    ps.setString(1, orderId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertDetail)) {
                    for (OrderDetail d : details) {
                        ps.setString(1, orderId);
                        ps.setString(2, d.getBookTitle());
                        ps.setString(3, d.getAuthor());
                        ps.setString(4, d.getPublication());
                        ps.setInt(5, d.getQuantity());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAuto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<OrderSummary> getAllOrderSummaries() {
        List<OrderSummary> out = new ArrayList<>();
        String sql = "SELECT ORDER_ID, ORDER_DATE FROM TBL_ORDER_HEADER ORDER BY ORDER_DATE DESC, ORDER_ID DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.add(new OrderSummary(rs.getString("ORDER_ID"), rs.getDate("ORDER_DATE")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public OrderHeader getOrderHeaderById(String orderId) {
        String sql = "SELECT ORDER_ID, S_ID, ORDER_DATE FROM TBL_ORDER_HEADER WHERE ORDER_ID=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new OrderHeader(rs.getString("ORDER_ID"), rs.getString("S_ID"), rs.getDate("ORDER_DATE"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<OrderDetail> getOrderDetails(String orderId) {
        List<OrderDetail> out = new ArrayList<>();
        String sql = "SELECT ORDER_ID, BOOK_TITLE, AUTHOR, PUBLICATION, QUANTITY FROM TBL_ORDER_DETAILS WHERE ORDER_ID=? ORDER BY BOOK_TITLE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new OrderDetail(
                        rs.getString("ORDER_ID"),
                        rs.getString("BOOK_TITLE"),
                        rs.getString("AUTHOR"),
                        rs.getString("PUBLICATION"),
                        rs.getInt("QUANTITY")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public Date getCurrentDbDate() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return new Date(System.currentTimeMillis());
            return currentDate(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Date(System.currentTimeMillis());
        }
    }

    private Date currentDate(Connection conn) throws SQLException {
        String sql = "SELECT SYSDATE AS D FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDate("D");
        }
    }
}
