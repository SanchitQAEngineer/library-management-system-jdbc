import java.io.*;

public class FileStorage {
    private static final String BOOKS_FILE = "books.txt";
    private static final String MEMBERS_FILE = "members.txt";

    public static void save(Library library) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKS_FILE))) {
            for (Book b : library.getAllBooks()) {
                pw.println(b.getId() + "|" + b.getTitle() + "|"
                        + b.getAuthor() + "|" + b.getIssuedTo());
            }
        } catch (IOException e) {
            System.out.println("Could not save books: " + e.getMessage());
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(MEMBERS_FILE))) {
            for (Member m : library.getAllMembers()) {
                pw.println(m.getId() + "|" + m.getName());
            }
        } catch (IOException e) {
            System.out.println("Could not save members: " + e.getMessage());
        }
    }

    public static void load(Library library) {
        File booksFile = new File(BOOKS_FILE);
        if (booksFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(booksFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split("\\|");
                    if (p.length == 4) {
                        library.loadBook(new Book(Integer.parseInt(p[0]), p[1], p[2],
                                Integer.parseInt(p[3])));
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.out.println("Could not load books: " + e.getMessage());
            }
        }

        File membersFile = new File(MEMBERS_FILE);
        if (membersFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(membersFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split("\\|");
                    if (p.length == 2) {
                        library.loadMember(new Member(Integer.parseInt(p[0]), p[1]));
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.out.println("Could not load members: " + e.getMessage());
            }
        }
    }
}