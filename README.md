# Library Management System (Java + MySQL)

Console application built with core Java and JDBC. Data is stored in a MySQL database.

## Features
- Add, view, search, update and delete books and members
- Input validation (empty fields, ISBN format, email format, duplicates)
- Issue and return books using database transactions (commit/rollback)
- Rules: a book can't be issued twice, maximum 3 books per member, 14-day loan period
- Overdue tracking with days late
- 6 SQL reports using JOIN, LEFT JOIN, GROUP BY and HAVING

## Tech
Java 17, JDBC, MySQL 8, Maven, JUnit 5

## Database
Three tables: `books`, `members`, `issues` (with foreign keys). See `sql/schema.sql`.

## How to run
1. Install Java 17+ and MySQL 8.
2. Run `sql/schema.sql` in MySQL Workbench to create `library_db`.
3. Copy `db.properties.example` to `src/main/resources/db.properties` and enter your MySQL password.
4. Open the project in IntelliJ IDEA and run `DbMain`.

## Tests
18 JUnit 5 integration tests run against a separate `library_test_db` database.
1. Create `library_test_db` with the same tables as `sql/schema.sql` (change the database name at the top).
2. Create `src/test/resources/db-test.properties` pointing to `library_test_db`.
3. Run `DatabaseTest`.

The tests include a safety check that refuses to run on any database other than `library_test_db`.

## Reports (menu option 13)
1. Currently issued books
2. Overdue books with days late
3. Most active members
4. Books never issued
5. Most popular books
6. Members holding 2 or more books