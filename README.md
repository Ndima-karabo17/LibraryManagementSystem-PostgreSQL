# Library Management System

A desktop application for managing books, authors, patrons, and borrowing
records, built with **Java (Swing + JDBC)** on a **PostgreSQL** backend.

This extends the original SQL-only project (table design and sample data)
with a real GUI application and a proper `BorrowingRecords` table for
tracking loans, due dates, and return history.

## Features

- **Books** — add, edit, search by title, filter by availability / year
  published, toggle borrowed/available, append genres, delete, "set all
  available" bulk action.
- **Authors** — add, edit, search by name, delete (books by a deleted
  author simply lose their author reference, they aren't deleted).
- **Patrons** — add, edit, search by name, delete.
- **Borrowing** — borrow a book (picks from currently-available books and
  existing patrons), return a book, view active loans, view overdue loans
  (highlighted in red), view full borrowing history.

Borrowing and returning run as single database transactions: the
`BorrowingRecords` row, the book's `available` flag, and the patron's
`borrowed_books` array are always updated together, so they can't drift
out of sync.

## Project layout

```
LibraryManagementSystem/
  pom.xml
  sql/schema.sql                     -- full schema + sample data
  src/main/resources/db.properties.example
  src/main/java/com/library/
    Main.java                        -- entry point
    db/DatabaseConfig.java           -- JDBC connection loader
    model/                           -- Author, Book, Patron, BorrowingRecord
    dao/                             -- AuthorDAO, BookDAO, PatronDAO, BorrowingDAO
    ui/                              -- MainFrame + one panel per tab
```

## Prerequisites

- JDK 17 or newer
- Maven 3.6+
- PostgreSQL 12+ running locally or reachable over the network

## Setup

### 1. Create the database and load the schema

```bash
createdb library
psql -d library -f sql/schema.sql
```

This creates `Authors`, `Books`, `Patrons`, and `BorrowingRecords`, and
loads the same sample data as the original project.

### 2. Configure the connection

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Edit `db.properties` with your actual connection details:

```properties
db.url=jdbc:postgresql://localhost:5432/library
db.user=postgres
db.password=yourpassword
```

`db.properties` is not tracked by the example file itself — keep your real
credentials out of version control (add it to `.gitignore`).

### 3. Build and run

```bash
mvn clean package
java -jar target/library-management-system.jar
```

`mvn package` bundles the PostgreSQL JDBC driver into the jar (via the
shade plugin), so the jar is fully self-contained — no classpath fiddling
needed to run it.

Alternatively, during development, run directly with Maven:

```bash
mvn compile exec:java -Dexec.mainClass=com.library.Main
```
(add the `exec-maven-plugin` to `pom.xml` if you want this shortcut, or
just run `Main` from your IDE with the `postgresql` dependency on the
classpath.)

## Notes on design choices

- **`BorrowingRecords` table vs. the `borrowed_books` array column** —
  the array on `Patrons` only tells you what's currently checked out, not
  when or for how long. A dedicated table with `borrow_date`, `due_date`,
  `return_date`, and `returned` supports due-date tracking, overdue
  reports, and full history — while the app still keeps the array in sync
  for backward compatibility with the original schema.
- **DAO pattern** — each table has a `*DAO` class that owns all SQL for
  that entity. The UI never writes SQL directly, which keeps the Swing
  panels focused on presentation.
- **PreparedStatements everywhere** — all user input goes through bound
  parameters, so there's no SQL injection risk from the search or
  add/edit forms.

## Extending this project

Straightforward next steps if you want to keep building:
- Add fine/penalty tracking for overdue loans.
- Add a login screen with librarian vs. patron roles.
- Add CSV export for reports (available books, overdue list, etc.).
- Replace Swing with JavaFX for a more modern UI, reusing the DAO layer
  as-is since it has no UI dependencies.
