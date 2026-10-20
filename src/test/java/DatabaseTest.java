import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {
    private static final BookDAO books = new BookDAO();
    private static final MemberDAO members = new MemberDAO();
    private static final IssueDAO issues = new IssueDAO();

    // Safety guard: never run (and wipe tables) on the real database
    @BeforeAll
    static void onlyRunOnTestDatabase() throws SQLException {
        try (Connection c = DBConnection.get()) {
            assertEquals("library_test_db", c.getCatalog(),
                    "Tests must run on library_test_db only!");
        }
    }

    @BeforeEach
    void cleanTables() throws SQLException {
        try (Connection c = DBConnection.get(); Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM issues");
            st.executeUpdate("DELETE FROM books");
            st.executeUpdate("DELETE FROM members");
            st.executeUpdate("ALTER TABLE issues AUTO_INCREMENT = 1");
            st.executeUpdate("ALTER TABLE books AUTO_INCREMENT = 1");
            st.executeUpdate("ALTER TABLE members AUTO_INCREMENT = 1");
        }
    }

    private int newBook(String title) throws SQLException {
        return books.add(title, "Author", null).getId();
    }

    private int newMember(int n) throws SQLException {
        return members.add("Member " + n, "m" + n + "@test.com").getId();
    }

    private int countIssues() throws SQLException {
        try (Connection c = DBConnection.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM issues")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // ---------- Books ----------

    @Test
    void addBookStoresRow() throws Exception {
        books.add("Clean Code", "Robert Martin", "9780132350884");
        assertEquals(1, books.getAll().size());
        assertEquals("Clean Code", books.getAll().get(0).getTitle());
    }

    @Test
    void emptyTitleRejected() {
        assertThrows(IllegalArgumentException.class, () -> books.add("  ", "Author", null));
    }

    @Test
    void badIsbnRejected() {
        assertThrows(IllegalArgumentException.class, () -> books.add("Title", "Author", "123"));
    }

    @Test
    void duplicateIsbnRejected() throws Exception {
        books.add("Book A", "Author", "9780132350884");
        assertThrows(IllegalArgumentException.class,
                () -> books.add("Book B", "Author", "9780132350884"));
    }

    @Test
    void searchIgnoresCase() throws Exception {
        books.add("Clean Code", "Robert Martin", null);
        assertEquals(1, books.search("clean").size());
        assertEquals(0, books.search("python").size());
    }

    @Test
    void updateMissingBookReturnsFalse() throws Exception {
        assertFalse(books.update(999, "Title", "Author", null));
    }

    // ---------- Members ----------

    @Test
    void invalidEmailRejected() {
        assertThrows(IllegalArgumentException.class, () -> members.add("Aman", "abc"));
    }

    @Test
    void duplicateEmailRejected() throws Exception {
        members.add("Aman", "aman@test.com");
        assertThrows(IllegalArgumentException.class, () -> members.add("Riya", "aman@test.com"));
    }

    // ---------- Issue / return (transactions) ----------

    @Test
    void issueSetsDueDate14DaysAhead() throws Exception {
        int b = newBook("Book");
        int m = newMember(1);
        assertEquals(LocalDate.now().plusDays(14), issues.issueBook(b, m));
    }

    @Test
    void doubleIssueBlocked() throws Exception {
        int b = newBook("Book");
        int m1 = newMember(1);
        int m2 = newMember(2);
        issues.issueBook(b, m1);
        assertThrows(BookNotAvailableException.class, () -> issues.issueBook(b, m2));
    }

    @Test
    void fourthBookBlocked() throws Exception {
        int m = newMember(1);
        for (int i = 1; i <= 3; i++) {
            issues.issueBook(newBook("Book " + i), m);
        }
        int fourth = newBook("Book 4");
        assertThrows(BookNotAvailableException.class, () -> issues.issueBook(fourth, m));
    }

    @Test
    void unknownBookBlocked() throws Exception {
        int m = newMember(1);
        assertThrows(BookNotAvailableException.class, () -> issues.issueBook(999, m));
    }

    @Test
    void unknownMemberBlocked() throws Exception {
        int b = newBook("Book");
        assertThrows(BookNotAvailableException.class, () -> issues.issueBook(b, 999));
    }

    @Test
    void returnMakesBookAvailableAgain() throws Exception {
        int b = newBook("Book");
        int m1 = newMember(1);
        int m2 = newMember(2);
        issues.issueBook(b, m1);
        issues.returnBook(b);
        assertDoesNotThrow(() -> issues.issueBook(b, m2));
    }

    @Test
    void returningNotIssuedBookBlocked() throws Exception {
        int b = newBook("Book");
        assertThrows(BookNotAvailableException.class, () -> issues.returnBook(b));
    }

    @Test
    void failedIssueLeavesNoRows() throws Exception {
        int b = newBook("Book");
        assertThrows(BookNotAvailableException.class, () -> issues.issueBook(b, 999));
        assertEquals(0, countIssues());
    }

    @Test
    void deleteBookWithHistoryBlocked() throws Exception {
        int b = newBook("Book");
        int m = newMember(1);
        issues.issueBook(b, m);
        assertThrows(IllegalArgumentException.class, () -> books.delete(b));
    }

    @Test
    void lateReturnReportsDaysLate() throws Exception {
        int b = newBook("Late Book");
        int m = newMember(1);
        issues.issueBook(b, m);
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE issues SET due_date = ? WHERE book_id = ?")) {
            ps.setDate(1, Date.valueOf(LocalDate.now().minusDays(5)));
            ps.setInt(2, b);
            ps.executeUpdate();
        }
        assertEquals(5, issues.returnBook(b));
    }
}