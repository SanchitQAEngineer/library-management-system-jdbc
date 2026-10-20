import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public Member add(String name, String email) throws SQLException {
        Validator.requireText(name, "Name", 100);
        Validator.requireEmail(email);

        String sql = "INSERT INTO members (name, email) VALUES (?, ?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Member(keys.getInt(1), name.trim(), email.trim());
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("A member with this email already exists.");
            }
            throw e;
        }
    }

    public List<Member> getAll() throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT id, name, email FROM members ORDER BY id";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Member(rs.getInt("id"), rs.getString("name"), rs.getString("email")));
            }
        }
        return list;
    }

    public boolean update(int id, String name, String email) throws SQLException {
        Validator.requireText(name, "Name", 100);
        Validator.requireEmail(email);

        String sql = "UPDATE members SET name = ?, email = ? WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("A member with this email already exists.");
            }
            throw e;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new IllegalArgumentException("Cannot delete: this member has issue records.");
            }
            throw e;
        }
    }
}