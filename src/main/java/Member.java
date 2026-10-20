public class Member {
    private final int id;
    private final String name;
    private final String email;

    public Member(int id, String name) {
        this(id, name, null);
    }

    public Member(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return email == null ? String.format("[%d] %s", id, name)
                : String.format("[%d] %s (%s)", id, name, email);
    }
}