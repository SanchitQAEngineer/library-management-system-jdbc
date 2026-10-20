import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    public static Connection get() throws SQLException {
        ClassLoader cl = DBConnection.class.getClassLoader();
        InputStream found = cl.getResourceAsStream("db-test.properties");
        if (found == null) {
            found = cl.getResourceAsStream("db.properties");
        }
        if (found == null) {
            throw new SQLException("No db.properties file found.");
        }
        try (InputStream in = found) {
            Properties p = new Properties();
            p.load(in);
            return DriverManager.getConnection(
                    p.getProperty("url"), p.getProperty("user"), p.getProperty("password"));
        } catch (IOException e) {
            throw new SQLException("Cannot read properties file", e);
        }
    }

    public static void main(String[] args) throws SQLException {
        try (Connection c = get()) {
            System.out.println("Connected: " + c.getCatalog());
        }
    }
}