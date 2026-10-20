import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryTest {
    private Library library;
    private Member member;

    @BeforeEach
    void setUp() {
        library = new Library();
        member = library.addMember("Sanchit");
    }

    @Test
    void newBookIsAvailable() {
        Book book = library.addBook("Clean Code", "Robert Martin");
        assertTrue(book.isAvailable());
    }

    @Test
    void issueBookMarksItAsIssued() throws BookNotAvailableException {
        Book book = library.addBook("Clean Code", "Robert Martin");
        library.issueBook(book.getId(), member.getId());
        assertFalse(book.isAvailable());
        assertEquals(member.getId(), book.getIssuedTo());
    }

    @Test
    void issuingAlreadyIssuedBookThrows() throws BookNotAvailableException {
        Book book = library.addBook("Clean Code", "Robert Martin");
        library.issueBook(book.getId(), member.getId());
        Member other = library.addMember("Aman");
        assertThrows(BookNotAvailableException.class,
                () -> library.issueBook(book.getId(), other.getId()));
    }

    @Test
    void memberCannotIssueMoreThanThreeBooks() throws BookNotAvailableException {
        for (int i = 1; i <= 3; i++) {
            Book b = library.addBook("Book " + i, "Author");
            library.issueBook(b.getId(), member.getId());
        }
        Book fourth = library.addBook("Book 4", "Author");
        assertThrows(BookNotAvailableException.class,
                () -> library.issueBook(fourth.getId(), member.getId()));
    }

    @Test
    void issuingUnknownBookThrows() {
        assertThrows(BookNotAvailableException.class,
                () -> library.issueBook(999, member.getId()));
    }

    @Test
    void issuingToUnknownMemberThrows() {
        Book book = library.addBook("Clean Code", "Robert Martin");
        assertThrows(BookNotAvailableException.class,
                () -> library.issueBook(book.getId(), 999));
    }

    @Test
    void returnBookMakesItAvailableAgain() throws BookNotAvailableException {
        Book book = library.addBook("Clean Code", "Robert Martin");
        library.issueBook(book.getId(), member.getId());
        library.returnBook(book.getId());
        assertTrue(book.isAvailable());
    }

    @Test
    void returningNonIssuedBookThrows() {
        Book book = library.addBook("Clean Code", "Robert Martin");
        assertThrows(BookNotAvailableException.class,
                () -> library.returnBook(book.getId()));
    }

    @Test
    void cannotRemoveIssuedBook() throws BookNotAvailableException {
        Book book = library.addBook("Clean Code", "Robert Martin");
        library.issueBook(book.getId(), member.getId());
        assertThrows(BookNotAvailableException.class,
                () -> library.removeBook(book.getId()));
    }

    @Test
    void removeAvailableBookWorks() throws BookNotAvailableException {
        Book book = library.addBook("Clean Code", "Robert Martin");
        library.removeBook(book.getId());
        assertTrue(library.getAllBooks().isEmpty());
    }

    @Test
    void searchFindsByTitleAndAuthorIgnoringCase() {
        library.addBook("Clean Code", "Robert Martin");
        library.addBook("Java Basics", "Herbert Schildt");
        assertEquals(1, library.search("clean").size());
        assertEquals(1, library.search("SCHILDT").size());
        assertEquals(0, library.search("python").size());
    }
}