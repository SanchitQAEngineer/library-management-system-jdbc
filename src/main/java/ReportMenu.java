import java.sql.*;
import java.util.Scanner;

public class ReportMenu {

    private static final String[] TITLES = {
            "1. Currently issued books (JOIN)",
            "2. Overdue books with days late (JOIN + date math)",
            "3. Most active members (GROUP BY)",
            "4. Books never issued (LEFT JOIN)",
            "5. Most popular books (GROUP BY)",
            "6. Members holding 2 or more books now (HAVING)"
    };

    private static final String[] SQL = {
            """
        SELECT i.id AS issue_id, b.title, m.name AS member, i.issue_date, i.due_date
        FROM issues i
        JOIN books b ON b.id = i.book_id
        JOIN members m ON m.id = i.member_id
        WHERE i.return_date IS NULL
        ORDER BY i.due_date
        """,
            """
        SELECT b.title, m.name AS member, m.email, i.due_date,
               DATEDIFF(CURDATE(), i.due_date) AS days_late
        FROM issues i
        JOIN books b ON b.id = i.book_id
        JOIN members m ON m.id = i.member_id
        WHERE i.return_date IS NULL AND i.due_date < CURDATE()
        ORDER BY days_late DESC
        """,
            """
        SELECT m.name, COUNT(*) AS total_borrowed
        FROM issues i
        JOIN members m ON m.id = i.member_id
        GROUP BY m.id, m.name
        ORDER BY total_borrowed DESC
        LIMIT 5
        """,
            """
        SELECT b.id, b.title, b.author
        FROM books b
        LEFT JOIN issues i ON i.book_id = b.id
        WHERE i.id IS NULL
        ORDER BY b.id
        """,
            """
        SELECT b.title, COUNT(*) AS times_issued
        FROM issues i
        JOIN books b ON b.id = i.book_id
        GROUP BY b.id, b.title
        ORDER BY times_issued DESC
        LIMIT 5
        """,
            """
        SELECT m.name, COUNT(*) AS books_held
        FROM issues i
        JOIN members m ON m.id = i.member_id
        WHERE i.return_date IS NULL
        GROUP BY m.id, m.name
        HAVING COUNT(*) >= 2
        """
    };

    public static void run(Scanner sc) throws SQLException {
        System.out.println("\n--- REPORTS ---");
        for (String t : TITLES) System.out.println(t);
        System.out.println("0. Back");
        System.out.print("Choose report: ");

        int n;
        try {
            n = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a number.");
            return;
        }
        if (n == 0) return;
        if (n < 1 || n > SQL.length) {
            System.out.println("Invalid report.");
            return;
        }
        print(TITLES[n - 1], SQL[n - 1]);
    }

    private static void print(String title, String sql) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            System.out.println("\n" + title);
            StringBuilder head = new StringBuilder();
            for (int i = 1; i <= cols; i++) {
                if (i > 1) head.append(" | ");
                head.append(md.getColumnLabel(i));
            }
            System.out.println(head);
            System.out.println("-".repeat(head.length()));

            int rows = 0;
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) row.append(" | ");
                    row.append(rs.getString(i));
                }
                System.out.println(row);
                rows++;
            }
            if (rows == 0) System.out.println("(no rows)");
        }
    }
}