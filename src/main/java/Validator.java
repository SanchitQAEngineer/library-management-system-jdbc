public class Validator {
    public static void requireText(String value, String field, int max) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }
        if (value.trim().length() > max) {
            throw new IllegalArgumentException(field + " must be at most " + max + " characters.");
        }
    }

    public static String optionalIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) return null;
        String digits = isbn.replace("-", "").trim();
        if (!digits.matches("\\d{10}|\\d{13}")) {
            throw new IllegalArgumentException("ISBN must have 10 or 13 digits.");
        }
        return digits;
    }

    public static void requireEmail(String email) {
        if (email == null || !email.trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
    }
}