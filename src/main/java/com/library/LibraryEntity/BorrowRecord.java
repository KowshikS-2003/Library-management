package com.library.LibraryEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_record")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "borrowed_at", nullable = false)
    private LocalDateTime borrowedAt = LocalDateTime.now();

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    public BorrowRecord() {}

    public BorrowRecord(Long memberId, Long bookId) {
        this.memberId = memberId;
        this.bookId = bookId;
        this.borrowedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public LocalDateTime getBorrowedAt() { return borrowedAt; }
    public void setBorrowedAt(LocalDateTime borrowedAt) { this.borrowedAt = borrowedAt; }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }

    @Override
    public String toString() {
        String status = (returnedAt == null) ? "active" : "returned " + returnedAt;
        return String.format("BorrowRecord{id=%d, memberId=%d, bookId=%d, borrowed=%s, %s}",
                id, memberId, bookId, borrowedAt, status);
    }
}
