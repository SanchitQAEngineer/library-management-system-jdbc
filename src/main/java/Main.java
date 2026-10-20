import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final Library library = new Library();

    public static void main(String[] args) {
        FileStorage.load(library);

        while (true) {
            printMenu();
            int choice = readInt("Choose: ");
            try {
                switch (choice) {
                    case 1 -> addBook();
                    case 2 -> removeBook();
                    case 3 -> searchBooks();
                    case 4 -> listBooks();
                    case 5 -> addMember();
                    case 6 -> listMembers();
                    case 7 -> issueBook();
                    case 8 -> returnBook();
                    case 0 -> {
                        FileStorage.save(library);
                        System.out.println("Data saved. Bye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (BookNotAvailableException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n===== LIBRARY MENU =====");
        System.out.println("1. Add book");
        System.out.println("2. Remove book");
        System.out.println("3. Search books");
        System.out.println("4. List all books");
        System.out.println("5. Add member");
        System.out.println("6. List members");
        System.out.println("7. Issue book");
        System.out.println("8. Return book");
        System.out.println("0. Save and exit");
    }

    private static void addBook() {
        System.out.print("Title: ");
        String title = sc.nextLine();
        System.out.print("Author: ");
        String author = sc.nextLine();
        Book b = library.addBook(title, author);
        System.out.println("Added: " + b);
    }

    private static void removeBook() throws BookNotAvailableException {
        library.removeBook(readInt("Book ID: "));
        System.out.println("Book removed.");
    }

    private static void searchBooks() {
        System.out.print("Search (title or author): ");
        List<Book> results = library.search(sc.nextLine());
        if (results.isEmpty()) System.out.println("No books found.");
        results.forEach(System.out::println);
    }

    private static void listBooks() {
        if (library.getAllBooks().isEmpty()) System.out.println("No books yet.");
        library.getAllBooks().forEach(System.out::println);
    }

    private static void addMember() {
        System.out.print("Member name: ");
        Member m = library.addMember(sc.nextLine());
        System.out.println("Registered: " + m);
    }

    private static void listMembers() {
        if (library.getAllMembers().isEmpty()) System.out.println("No members yet.");
        library.getAllMembers().forEach(System.out::println);
    }

    private static void issueBook() throws BookNotAvailableException {
        int bookId = readInt("Book ID: ");
        int memberId = readInt("Member ID: ");
        library.issueBook(bookId, memberId);
        System.out.println("Book issued.");
    }

    private static void returnBook() throws BookNotAvailableException {
        library.returnBook(readInt("Book ID: "));
        System.out.println("Book returned.");
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