import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Library {
    private static final int MAX_BOOKS_PER_MEMBER = 3;

    private final Map<Integer, Book> books = new LinkedHashMap<>();
    private final Map<Integer, Member> members = new LinkedHashMap<>();
    private int nextBookId = 1;
    private int nextMemberId = 1;

    public Book addBook(String title, String author) {
        Book book = new Book(nextBookId++, clean(title), clean(author), 0);
        books.put(book.getId(), book);
        return book;
    }

    public Member addMember(String name) {
        Member member = new Member(nextMemberId++, clean(name));
        members.put(member.getId(), member);
        return member;
    }

    public void removeBook(int bookId) throws BookNotAvailableException {
        Book book = books.get(bookId);
        if (book == null) {
            throw new BookNotAvailableException("Book not found.");
        }
        if (!book.isAvailable()) {
            throw new BookNotAvailableException("Cannot remove: book is currently issued.");
        }
        books.remove(bookId);
    }

    public List<Book> search(String keyword) {
        String k = keyword.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(k)
                        || b.getAuthor().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }

    public long countIssuedTo(int memberId) {
        return books.values().stream()
                .filter(b -> b.getIssuedTo() == memberId)
                .count();
    }

    public void issueBook(int bookId, int memberId) throws BookNotAvailableException {
        Book book = books.get(bookId);
        if (book == null) {
            throw new BookNotAvailableException("Book not found.");
        }
        if (!members.containsKey(memberId)) {
            throw new BookNotAvailableException("Member not found.");
        }
        if (!book.isAvailable()) {
            throw new BookNotAvailableException("Book is already issued.");
        }
        if (countIssuedTo(memberId) >= MAX_BOOKS_PER_MEMBER) {
            throw new BookNotAvailableException(
                    "Member already has " + MAX_BOOKS_PER_MEMBER + " books.");
        }
        book.issue(memberId);
    }

    public void returnBook(int bookId) throws BookNotAvailableException {
        Book book = books.get(bookId);
        if (book == null) {
            throw new BookNotAvailableException("Book not found.");
        }
        if (book.isAvailable()) {
            throw new BookNotAvailableException("This book was not issued.");
        }
        book.giveBack();
    }

    public Collection<Book> getAllBooks() { return books.values(); }
    public Collection<Member> getAllMembers() { return members.values(); }

    // Used by FileStorage when loading saved data
    public void loadBook(Book book) {
        books.put(book.getId(), book);
        nextBookId = Math.max(nextBookId, book.getId() + 1);
    }

    public void loadMember(Member member) {
        members.put(member.getId(), member);
        nextMemberId = Math.max(nextMemberId, member.getId() + 1);
    }

    // "|" is our file separator, so we remove it from user input
    private String clean(String text) {
        return text.replace("|", "/").trim();
    }
}