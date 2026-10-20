public class Book {
    private final int id;
    private final String title;
    private final String author;
    private final String isbn;
    private int issuedTo; // 0 means available

    public Book(int id, String title, String author, int issuedTo) {
        this(id, title, author, null, issuedTo);
    }

    public Book(int id, String title, String author, String isbn, int issuedTo) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.issuedTo = issuedTo;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public int getIssuedTo() { return issuedTo; }
    public boolean isAvailable() { return issuedTo == 0; }

    public void issue(int memberId) { this.issuedTo = memberId; }
    public void giveBack() { this.issuedTo = 0; }

    @Override
    public String toString() {
        String status = isAvailable() ? "Available" : "Issued to member #" + issuedTo;
        return String.format("[%d] %s by %s - %s", id, title, author, status);
    }
}