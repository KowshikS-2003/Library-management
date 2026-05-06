package com.library.LibraryDTO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BorrowRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = PaddedIdSerializer.class)
    private Long id;
    @JsonSerialize(using = PaddedIdSerializer.class)
    private Long memberId;
    private String memberName;
    @JsonSerialize(using = PaddedIdSerializer.class)
    private Long bookId;
    private String bookTitle;
    private LocalDateTime borrowedAt;
    private LocalDateTime returnedAt;

    public BorrowRecordDTO() {}

    public BorrowRecordDTO(Long id, Long memberId, String memberName,
                           Long bookId, String bookTitle,
                           LocalDateTime borrowedAt, LocalDateTime returnedAt) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowedAt = borrowedAt;
        this.returnedAt = returnedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public LocalDateTime getBorrowedAt() { return borrowedAt; }
    public void setBorrowedAt(LocalDateTime borrowedAt) { this.borrowedAt = borrowedAt; }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
}
