import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public Book add(String title, String author, String isbn) throws SQLException {
        Validator.requireText(title, "Title", 150);
        Validator.requireText(author, "Author", 100);
        String cleanIsbn = Validator.optionalIsbn(isbn);

        String sql = "INSERT INTO books (title, author, isbn) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title.trim());
            ps.setString(2, author.trim());
            ps.setString(3, cleanIsbn);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Book(keys.getInt(1), title.trim(), author.trim(), cleanIsbn, 0);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("A book with this ISBN already exists.");
            }
            throw e;
        }
    }

    public List<Book> getAll() throws SQLException {
        return query("SELECT id, title, author, isbn FROM books ORDER BY id", null);
    }

    public List<Book> search(String keyword) throws SQLException {
        String like = "%" + keyword.trim() + "%";
        return query("SELECT id, title, author, isbn FROM books "
                + "WHERE title LIKE ? OR author LIKE ? ORDER BY id", like);
    }

    public boolean update(int id, String title, String author, String isbn) throws SQLException {
        Validator.requireText(title, "Title", 150);
        Validator.requireText(author, "Author", 100);
        String cleanIsbn = Validator.optionalIsbn(isbn);

        String sql = "UPDATE books SET title = ?, author = ?, isbn = ? WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title.trim());
            ps.setString(2, author.trim());
            ps.setString(3, cleanIsbn);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("A book with this ISBN already exists.");
            }
            throw e;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new IllegalArgumentException("Cannot delete: this book has issue records.");
            }
            throw e;
        }
    }

    private List<Book> query(String sql, String like) throws SQLException {
        List<Book> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (like != null) {
                ps.setString(1, like);
                ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Book(rs.getInt("id"), rs.getString("title"),
                            rs.getString("author"), rs.getString("isbn"), 0));
                }
            }
        }
        return list;
    }
}