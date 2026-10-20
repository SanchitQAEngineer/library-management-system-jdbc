import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class IssueDAO {
    private static final int MAX_BOOKS_PER_MEMBER =3 ;
    private static final int LOAN_DAYS = 14;

    // Returns the due date
    public LocalDate issueBook(int bookId, int memberId)
            throws SQLException, BookNotAvailableException {
        Connection c = DBConnection.get();
        try {
            c.setAutoCommit(false); // start transaction

            // FOR UPDATE locks this book row so two people can't issue it at once
            if (!exists(c, "SELECT id FROM books WHERE id = ? FOR UPDATE", bookId)) {
                throw new BookNotAvailableException("Book not found.");
            }
            if (!exists(c, "SELECT id FROM members WHERE id = ?", memberId)) {
                throw new BookNotAvailableException("Member not found.");
            }
            if (exists(c, "SELECT id FROM issues WHERE book_id = ? AND return_date IS NULL", bookId)) {
                throw new BookNotAvailableException("Book is already issued.");
            }
            if (activeCount(c, memberId) >= MAX_BOOKS_PER_MEMBER) {
                throw new BookNotAvailableException(
                        "Member already has " + MAX_BOOKS_PER_MEMBER + " books.");
            }

            LocalDate today = LocalDate.now();
            LocalDate due = today.plusDays(LOAN_DAYS);
            String sql = "INSERT INTO issues (book_id, member_id, issue_date, due_date) "
                    + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, bookId);
                ps.setInt(2, memberId);
                ps.setDate(3, Date.valueOf(today));
                ps.setDate(4, Date.valueOf(due));
                ps.executeUpdate();
            }

            c.commit(); // save everything
            return due;
        } catch (SQLException | BookNotAvailableException e) {
            c.rollback(); // undo everything
            throw e;
        } finally {
            c.close();
        }
    }

    // Returns how many days late the book is (0 if on time)
    public long returnBook(int bookId) throws SQLException, BookNotAvailableException {
        Connection c = DBConnection.get();
        try {
            c.setAutoCommit(false);

            int issueId = 0;
            LocalDate due = null;
            String find = "SELECT id, due_date FROM issues "
                    + "WHERE book_id = ? AND return_date IS NULL FOR UPDATE";
            try (PreparedStatement ps = c.prepareStatement(find)) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new BookNotAvailableException("This book is not currently issued.");
                    }
                    issueId = rs.getInt("id");
                    due = rs.getDate("due_date").toLocalDate();
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE issues SET return_date = ? WHERE id = ?")) {
                ps.setDate(1, Date.valueOf(LocalDate.now()));
                ps.setInt(2, issueId);
                ps.executeUpdate();
            }

            c.commit();
            return Math.max(0, ChronoUnit.DAYS.between(due, LocalDate.now()));
        } catch (SQLException | BookNotAvailableException e) {
            c.rollback();
            throw e;
        } finally {
            c.close();
        }
    }

    // All books that are currently issued
    public List<String> listActive() throws SQLException {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT i.id, b.title, m.name, i.issue_date, i.due_date "
                + "FROM issues i "
                + "JOIN books b ON b.id = i.book_id "
                + "JOIN members m ON m.id = i.member_id "
                + "WHERE i.return_date IS NULL ORDER BY i.due_date";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            LocalDate today = LocalDate.now();
            while (rs.next()) {
                LocalDate due = rs.getDate("due_date").toLocalDate();
                String flag = due.isBefore(today) ? "  ** OVERDUE **" : "";
                rows.add(String.format("Issue #%d: \"%s\" -> %s | issued %s | due %s%s",
                        rs.getInt("id"), rs.getString("title"), rs.getString("name"),
                        rs.getDate("issue_date").toLocalDate(), due, flag));
            }
        }
        return rows;
    }

    private boolean exists(Connection c, String sql, int param) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int activeCount(Connection c, int memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM issues WHERE member_id = ? AND return_date IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}