# Deliverable 5 — Library Management System

## Step 1: Entity Identification & Relationships

### Entities

#### 1. Member
Represents a library patron who can borrow books. Each member is uniquely identified by an auto-generated `id` and has a unique `email` to prevent duplicate registrations. Members are the actors in the borrow workflow.

**Attributes:**
| Attribute    | Description                              |
|-------------|------------------------------------------|
| `id`        | Primary key, uniquely identifies a member |
| `name`      | Full name of the member                   |
| `email`     | Unique email address                      |
| `created_at`| Timestamp of registration                 |

#### 2. Book
Represents a title in the library's catalog. A book entry tracks both the total number of physical copies the library owns (`total_copies`) and how many are currently on the shelf (`available_copies`). This distinction is critical for enforcing the "no borrow if unavailable" rule.

**Attributes:**
| Attribute         | Description                                      |
|-------------------|--------------------------------------------------|
| `id`              | Primary key, uniquely identifies a book           |
| `title`           | Title of the book                                 |
| `author`          | Author of the book                                |
| `total_copies`    | Total physical copies the library owns            |
| `available_copies`| Copies currently available for borrowing          |

#### 3. BorrowRecord
A junction/association entity that records every borrow transaction. It connects a `Member` to a `Book` with timestamps. A `NULL` value in `returned_at` means the book is still checked out — this is how we count a member's active borrows.

**Attributes:**
| Attribute     | Description                                         |
|---------------|-----------------------------------------------------|
| `id`          | Primary key, uniquely identifies a borrow record     |
| `member_id`   | Foreign key → Member                                |
| `book_id`     | Foreign key → Book                                  |
| `borrowed_at` | Timestamp when the book was borrowed                 |
| `returned_at` | Timestamp when returned; NULL if still borrowed      |

---

### Relationships

```
Member (1) ──────< (Many) BorrowRecord (Many) >────── (1) Book
```

1. **Member → BorrowRecord (One-to-Many)**
   - One member can have many borrow records over time.
   - `BorrowRecord.member_id` is a foreign key referencing `Member.id`.
   - This allows tracking a member's full borrow history and counting active borrows (where `returned_at IS NULL`) to enforce the 3-book limit.

2. **Book → BorrowRecord (One-to-Many)**
   - One book can appear in many borrow records (different members borrow it at different times, or multiple copies are lent out simultaneously).
   - `BorrowRecord.book_id` is a foreign key referencing `Book.id`.
   - Combined with `available_copies`, this supports the availability constraint.

3. **Book has limited quantity**
   - `available_copies` is decremented atomically when a book is borrowed and incremented when returned.
   - The system checks `available_copies > 0` before allowing a borrow.

---

## Step 2: Schema Design & Normalization

### Normalization Analysis (Third Normal Form)

The schema satisfies **3NF**:

- **1NF**: All columns hold atomic (indivisible) values. No repeating groups or arrays.
- **2NF**: Every non-key attribute is fully functionally dependent on the entire primary key (all tables use single-column surrogate keys, so 2NF is automatically satisfied).
- **3NF**: No transitive dependencies exist.
  - In `Member`: `name`, `email`, `created_at` all depend only on `id`.
  - In `Book`: `title`, `author`, `total_copies`, `available_copies` all depend only on `id`.
  - In `BorrowRecord`: `member_id`, `book_id`, `borrowed_at`, `returned_at` all depend only on `id`. There is no transitive chain — `member_id` and `book_id` are foreign keys, not derived from each other.

The DDL scripts are in `schema.sql`. The Java implementation is in `src/main/java/com/library/`:
- `DatabaseManager.java` — connection management and schema initialisation
- `LibraryService.java` — CRUD operations, transactional borrow/return, and join queries
- `Main.java` — demo driver that exercises all steps

---

## Step 5: SQL Join Queries — Explanation

### Query 1 — Borrow details (Member name, Book title, dates)

```sql
SELECT m.name, b.title, br.borrowed_at, br.returned_at
FROM BorrowRecord br
INNER JOIN Member m ON br.member_id = m.id
INNER JOIN Book b ON br.book_id = b.id
ORDER BY br.borrowed_at DESC;
```

**Joins used:**
- **INNER JOIN** `Member` on `BorrowRecord.member_id = Member.id` — links each borrow record to the member who borrowed the book, giving us the member's `name`.
- **INNER JOIN** `Book` on `BorrowRecord.book_id = Book.id` — links each borrow record to the book that was borrowed, giving us the book's `title`.
- INNER JOIN is appropriate because every borrow record must reference a valid member and book (enforced by foreign keys). There are no orphan records.

### Query 2 — Top 3 most borrowed books

```sql
SELECT b.title, b.author, COUNT(br.id) AS times_borrowed
FROM BorrowRecord br
INNER JOIN Book b ON br.book_id = b.id
GROUP BY b.id, b.title, b.author
ORDER BY times_borrowed DESC
LIMIT 3;
```

**Joins and clauses used:**
- **INNER JOIN** `Book` — connects borrow records to book metadata.
- **GROUP BY** `b.id, b.title, b.author` — aggregates borrow records per book.
- **COUNT(br.id)** — counts total borrow events for each book.
- **ORDER BY ... DESC LIMIT 3** — sorts by popularity and takes the top 3.

---

## Step 6: Indexing — Explanation

### Index Statements

```sql
CREATE INDEX idx_borrowrecord_member_id ON BorrowRecord(member_id);
CREATE INDEX idx_borrowrecord_book_id ON BorrowRecord(book_id);
CREATE UNIQUE INDEX idx_member_email ON Member(email);
```

### Why these indexes help

1. **`idx_borrowrecord_member_id`** — The borrow-book transaction checks `SELECT COUNT(*) FROM BorrowRecord WHERE member_id = ? AND returned_at IS NULL`. Without an index, the database performs a full table scan of all borrow records. With the index, it jumps directly to the rows for that member. This is critical as the BorrowRecord table grows.

2. **`idx_borrowrecord_book_id`** — Speeds up join queries (e.g., "top 3 most borrowed books") and any lookup that filters borrow records by book.

3. **`idx_member_email`** (unique index) — The `UNIQUE` constraint on `email` already creates an implicit unique index in most databases. This index accelerates login/lookup-by-email queries from O(n) full scan to O(log n) B-tree lookup. For a library system where members are frequently looked up by email, this is essential.

**General performance benefit:** Indexes create B-tree (or similar) data structures that allow the database engine to locate rows in logarithmic time instead of scanning every row. The trade-off is slightly slower INSERT/UPDATE operations and additional storage, but for read-heavy workloads like a library system, the benefit far outweighs the cost.
