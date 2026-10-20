import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class DbMain {
    private static final Scanner sc = new Scanner(System.in);
    private static final BookDAO books = new BookDAO();
    private static final MemberDAO members = new MemberDAO();
    private static final IssueDAO issues = new IssueDAO();

    public static void main(String[] args) {
        while (true) {
            printMenu();
            int choice = readInt("Choose: ");
            try {
                switch (choice) {
                    case 1 -> addBook();
                    case 2 -> listBooks();
                    case 3 -> searchBooks();
                    case 4 -> updateBook();
                    case 5 -> deleteBook();
                    case 6 -> addMember();
                    case 7 -> listMembers();
                    case 8 -> updateMember();
                    case 9 -> deleteMember();
                    case 10 -> issueBook();
                    case 11 -> returnBook();
                    case 12 -> listIssued();
                    case 13 -> ReportMenu.run(sc);
                    case 0 -> {
                        System.out.println("Bye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (IllegalArgumentException | BookNotAvailableException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n===== LIBRARY (DATABASE) =====");
        System.out.println("1. Add book         6. Add member");
        System.out.println("2. List books       7. List members");
        System.out.println("3. Search books     8. Update member");
        System.out.println("4. Update book      9. Delete member");
        System.out.println("5. Delete book     10. Issue book");
        System.out.println("11. Return book    12. Issued books");
        System.out.println("13. Reports         0. Exit");
    }

    private static void addBook() throws SQLException {
        System.out.print("Title: ");
        String title = sc.nextLine();
        System.out.print("Author: ");
        String author = sc.nextLine();
        System.out.print("ISBN (optional, 10 or 13 digits): ");
        String isbn = sc.nextLine();
        printBook(books.add(title, author, isbn));
    }

    private static void listBooks() throws SQLException {
        show(books.getAll());
    }

    private static void searchBooks() throws SQLException {
        System.out.print("Search (title or author): ");
        show(books.search(sc.nextLine()));
    }

    private static void updateBook() throws SQLException {
        int id = readInt("Book ID: ");
        System.out.print("New title: ");
        String title = sc.nextLine();
        System.out.print("New author: ");
        String author = sc.nextLine();
        System.out.print("New ISBN (optional): ");
        String isbn = sc.nextLine();
        System.out.println(books.update(id, title, author, isbn) ? "Book updated." : "Book not found.");
    }

    private static void deleteBook() throws SQLException {
        System.out.println(books.delete(readInt("Book ID: ")) ? "Book deleted." : "Book not found.");
    }

    private static void addMember() throws SQLException {
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.println("Registered: " + members.add(name, email));
    }

    private static void listMembers() throws SQLException {
        List<Member> list = members.getAll();
        if (list.isEmpty()) System.out.println("No members yet.");
        list.forEach(System.out::println);
    }

    private static void updateMember() throws SQLException {
        int id = readInt("Member ID: ");
        System.out.print("New name: ");
        String name = sc.nextLine();
        System.out.print("New email: ");
        String email = sc.nextLine();
        System.out.println(members.update(id, name, email) ? "Member updated." : "Member not found.");
    }

    private static void deleteMember() throws SQLException {
        System.out.println(members.delete(readInt("Member ID: ")) ? "Member deleted." : "Member not found.");
    }

    private static void issueBook() throws SQLException, BookNotAvailableException {
        int bookId = readInt("Book ID: ");
        int memberId = readInt("Member ID: ");
        LocalDate due = issues.issueBook(bookId, memberId);
        System.out.println("Book issued. Due date: " + due);
    }

    private static void returnBook() throws SQLException, BookNotAvailableException {
        long lateDays = issues.returnBook(readInt("Book ID: "));
        System.out.println(lateDays == 0 ? "Book returned on time."
                : "Book returned. It was " + lateDays + " day(s) late.");
    }

    private static void listIssued() throws SQLException {
        List<String> rows = issues.listActive();
        if (rows.isEmpty()) System.out.println("No books are currently issued.");
        rows.forEach(System.out::println);
    }

    private static void show(List<Book> list) {
        if (list.isEmpty()) System.out.println("No books found.");
        list.forEach(DbMain::printBook);
    }

    private static void printBook(Book b) {
        String isbn = b.getIsbn() == null ? "" : " (ISBN: " + b.getIsbn() + ")";
        System.out.println("[" + b.getId() + "] " + b.getTitle() + " by " + b.getAuthor() + isbn);
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }
}