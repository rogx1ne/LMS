package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.BookCopy;
import com.library.model.BookCopyStatusRow;
import com.library.model.BookStockItem;
import com.library.service.BookIdGenerator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private final BookIdGenerator idGenerator = new BookIdGenerator();

    public String peekNextAccessionNo() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return "A00001";
            return idGenerator.nextAccessionNo(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return "A00001";
        }
    }

    public BookCopy addBookCopy(BookCopy draft) throws SQLException {
        String mergeCatalogSql =
            "MERGE INTO TBL_BOOK_CATALOG c " +
            "USING (SELECT ? AS AUTHOR_NAME, ? AS BK_TITLE, ? AS EDITION FROM dual) src " +
            "ON (c.AUTHOR_NAME = src.AUTHOR_NAME AND c.BK_TITLE = src.BK_TITLE AND c.EDITION = src.EDITION) " +
            "WHEN NOT MATCHED THEN INSERT (AUTHOR_NAME, BK_TITLE, EDITION) VALUES (src.AUTHOR_NAME, src.BK_TITLE, src.EDITION)";

        String insertSql =
            "INSERT INTO TBL_BOOK_INFORMATION (" +
            "ACCESS_NO, AUTHOR_NAME, BK_TITLE, VOLUME, EDITION, PUBLISHER, PUB_PLACE, PUB_YEAR, PAGES, SOURCE, " +
            "CLASS_NO, BOOK_NO, U_PRICE, B_NO, B_DATE, WITHDRAWN, REMARKS, STATUS" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("DB connection is null.");
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                String accessionNo = idGenerator.nextAccessionNo(conn);

                try (PreparedStatement mergePs = conn.prepareStatement(mergeCatalogSql)) {
                    mergePs.setString(1, draft.getAuthorName());
                    mergePs.setString(2, draft.getTitle());
                    mergePs.setInt(3, draft.getEdition());
                    mergePs.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, accessionNo);
                    ps.setString(2, draft.getAuthorName());
                    ps.setString(3, draft.getTitle());
                    if (draft.getVolume() == null) ps.setNull(4, java.sql.Types.NUMERIC); else ps.setInt(4, draft.getVolume());
                    ps.setInt(5, draft.getEdition());
                    ps.setString(6, draft.getPublisher());
                    ps.setString(7, draft.getPublicationPlace());
                    ps.setInt(8, draft.getPublicationYear());
                    ps.setInt(9, draft.getPages());
                    ps.setString(10, draft.getSource());
                    ps.setString(11, draft.getClassNo());
                    ps.setString(12, draft.getBookNo());
                    ps.setBigDecimal(13, draft.getCost());
                    ps.setString(14, draft.getBillNo());
                    ps.setDate(15, draft.getBillDate());
                    ps.setDate(16, draft.getWithdrawnDate());
                    ps.setString(17, draft.getRemarks());
                    ps.setString(18, draft.getStatus());
                    ps.executeUpdate();
                }

                conn.commit();

                return new BookCopy(
                    accessionNo,
                    draft.getAuthorName(),
                    draft.getTitle(),
                    draft.getVolume(),
                    draft.getEdition(),
                    draft.getPublisher(),
                    draft.getPublicationPlace(),
                    draft.getPublicationYear(),
                    draft.getPages(),
                    draft.getSource(),
                    draft.getClassNo(),
                    draft.getBookNo(),
                    draft.getCost(),
                    draft.getBillNo(),
                    draft.getBillDate(),
                    draft.getWithdrawnDate(),
                    draft.getRemarks(),
                    draft.getStatus()
                );
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        }
    }

    public boolean updateBookCopy(BookCopy b) {
        String mergeCatalogSql =
            "MERGE INTO TBL_BOOK_CATALOG c " +
            "USING (SELECT ? AS AUTHOR_NAME, ? AS BK_TITLE, ? AS EDITION FROM dual) src " +
            "ON (c.AUTHOR_NAME = src.AUTHOR_NAME AND c.BK_TITLE = src.BK_TITLE AND c.EDITION = src.EDITION) " +
            "WHEN NOT MATCHED THEN INSERT (AUTHOR_NAME, BK_TITLE, EDITION) VALUES (src.AUTHOR_NAME, src.BK_TITLE, src.EDITION)";

        String sql =
            "UPDATE TBL_BOOK_INFORMATION SET AUTHOR_NAME=?, BK_TITLE=?, VOLUME=?, EDITION=?, PUBLISHER=?, PUB_PLACE=?, " +
            "PUB_YEAR=?, PAGES=?, SOURCE=?, CLASS_NO=?, BOOK_NO=?, U_PRICE=?, B_NO=?, B_DATE=?, WITHDRAWN=?, REMARKS=?, STATUS=? " +
            "WHERE ACCESS_NO=?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement mergePs = conn.prepareStatement(mergeCatalogSql)) {
                    mergePs.setString(1, b.getAuthorName());
                    mergePs.setString(2, b.getTitle());
                    mergePs.setInt(3, b.getEdition());
                    mergePs.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, b.getAuthorName());
                    ps.setString(2, b.getTitle());
                    if (b.getVolume() == null) ps.setNull(3, java.sql.Types.NUMERIC); else ps.setInt(3, b.getVolume());
                    ps.setInt(4, b.getEdition());
                    ps.setString(5, b.getPublisher());
                    ps.setString(6, b.getPublicationPlace());
                    ps.setInt(7, b.getPublicationYear());
                    ps.setInt(8, b.getPages());
                    ps.setString(9, b.getSource());
                    ps.setString(10, b.getClassNo());
                    ps.setString(11, b.getBookNo());
                    ps.setBigDecimal(12, b.getCost());
                    ps.setString(13, b.getBillNo());
                    ps.setDate(14, b.getBillDate());
                    ps.setDate(15, b.getWithdrawnDate());
                    ps.setString(16, b.getRemarks());
                    ps.setString(17, b.getStatus());
                    ps.setString(18, b.getAccessionNo());
                    int rows = ps.executeUpdate();
                    conn.commit();
                    return rows > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public BookCopy getBookByAccessionNo(String accessionNo) {
        String sql = "SELECT * FROM TBL_BOOK_INFORMATION WHERE ACCESS_NO = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accessionNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCopy(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<BookCopy> getAllBookCopies() {
        List<BookCopy> out = new ArrayList<>();
        String sql = "SELECT * FROM TBL_BOOK_INFORMATION ORDER BY ACCESS_NO DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.add(mapCopy(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public List<BookStockItem> getStockSummary() {
        List<BookStockItem> out = new ArrayList<>();
        String sql =
            "SELECT BK_TITLE, AUTHOR_NAME, EDITION, PUBLISHER || ', ' || PUB_PLACE AS PUBLICATION, " +
            "COUNT(*) AS QTY " +
            "FROM TBL_BOOK_INFORMATION " +
            "WHERE STATUS = 'ACTIVE' " +
            "GROUP BY BK_TITLE, AUTHOR_NAME, EDITION, PUBLISHER, PUB_PLACE " +
            "ORDER BY BK_TITLE, AUTHOR_NAME, EDITION";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                out.add(new BookStockItem(
                    rs.getString("BK_TITLE"),
                    rs.getString("AUTHOR_NAME"),
                    rs.getInt("EDITION"),
                    rs.getString("PUBLICATION"),
                    rs.getInt("QTY")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return out;
    }

    public List<BookCopyStatusRow> getStockDrillDown(String title, String authorName, int edition) {
        List<BookCopyStatusRow> out = new ArrayList<>();
        String sql =
            "SELECT ACCESS_NO, STATUS, WITHDRAWN " +
            "FROM TBL_BOOK_INFORMATION " +
            "WHERE BK_TITLE = ? AND AUTHOR_NAME = ? AND EDITION = ? " +
            "ORDER BY ACCESS_NO";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, authorName);
            ps.setInt(3, edition);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new BookCopyStatusRow(
                        rs.getString("ACCESS_NO"),
                        rs.getString("STATUS"),
                        rs.getDate("WITHDRAWN")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public List<BookStockItem> checkAndLogLowStock(int threshold, String alertedTo) {
        List<BookStockItem> lowItems = new ArrayList<>();
        String lowStockSql =
            "SELECT BK_TITLE, AUTHOR_NAME, EDITION, PUBLISHER || ', ' || PUB_PLACE AS PUBLICATION, COUNT(*) AS QTY " +
            "FROM TBL_BOOK_INFORMATION " +
            "WHERE STATUS = 'ACTIVE' " +
            "GROUP BY BK_TITLE, AUTHOR_NAME, EDITION, PUBLISHER, PUB_PLACE " +
            "HAVING COUNT(*) < ? " +
            "ORDER BY BK_TITLE";

        String logSql =
            "INSERT INTO TBL_BOOK_ALERT_LOG (ALERT_TYPE, BK_TITLE, AUTHOR_NAME, EDITION, CURRENT_QTY, THRESHOLD_QTY, ALERTED_TO) " +
            "SELECT 'LOW_STOCK', ?, ?, ?, ?, ?, ? FROM dual " +
            "WHERE NOT EXISTS ( " +
            "  SELECT 1 FROM TBL_BOOK_ALERT_LOG " +
            "  WHERE ALERT_TYPE = 'LOW_STOCK' " +
            "    AND BK_TITLE = ? " +
            "    AND AUTHOR_NAME = ? " +
            "    AND EDITION = ? " +
            "    AND TRUNC(ALERT_TS) = TRUNC(SYSDATE) " +
            ")";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement lowPs = conn.prepareStatement(lowStockSql);
             PreparedStatement logPs = conn.prepareStatement(logSql)) {

            lowPs.setInt(1, threshold);
            try (ResultSet rs = lowPs.executeQuery()) {
                while (rs.next()) {
                    BookStockItem item = new BookStockItem(
                        rs.getString("BK_TITLE"),
                        rs.getString("AUTHOR_NAME"),
                        rs.getInt("EDITION"),
                        rs.getString("PUBLICATION"),
                        rs.getInt("QTY")
                    );
                    lowItems.add(item);

                    logPs.setString(1, item.getTitle());
                    logPs.setString(2, item.getAuthorName());
                    logPs.setInt(3, item.getEdition());
                    logPs.setInt(4, item.getQuantity());
                    logPs.setInt(5, threshold);
                    logPs.setString(6, alertedTo);
                    logPs.setString(7, item.getTitle());
                    logPs.setString(8, item.getAuthorName());
                    logPs.setInt(9, item.getEdition());
                    logPs.addBatch();
                }
            }

            if (!lowItems.isEmpty()) {
                logPs.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lowItems;
    }

    private BookCopy mapCopy(ResultSet rs) throws SQLException {
        return new BookCopy(
            rs.getString("ACCESS_NO"),
            rs.getString("AUTHOR_NAME"),
            rs.getString("BK_TITLE"),
            (rs.getObject("VOLUME") == null ? null : rs.getInt("VOLUME")),
            rs.getInt("EDITION"),
            rs.getString("PUBLISHER"),
            rs.getString("PUB_PLACE"),
            rs.getInt("PUB_YEAR"),
            rs.getInt("PAGES"),
            rs.getString("SOURCE"),
            rs.getString("CLASS_NO"),
            rs.getString("BOOK_NO"),
            rs.getBigDecimal("U_PRICE"),
            rs.getString("B_NO"),
            rs.getDate("B_DATE"),
            rs.getDate("WITHDRAWN"),
            rs.getString("REMARKS"),
            rs.getString("STATUS")
        );
    }

    public Date getCurrentDbDate() {
        String sql = "SELECT SYSDATE AS NOW_DT FROM dual";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDate("NOW_DT");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Date(System.currentTimeMillis());
    }
}
