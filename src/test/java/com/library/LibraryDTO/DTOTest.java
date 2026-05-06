package com.library.LibraryDTO;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    // ---- BookDTO ----

    @Test
    void bookDTO_defaultConstructor() {
        BookDTO dto = new BookDTO();
        assertNull(dto.getId());
        assertNull(dto.getTitle());
        assertNull(dto.getAuthor());
        assertEquals(0, dto.getTotalCopies());
        assertEquals(0, dto.getAvailableCopies());
    }

    @Test
    void bookDTO_parameterizedConstructor() {
        BookDTO dto = new BookDTO(1L, "Title", "Author", 5, 3);
        assertEquals(1L, dto.getId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Author", dto.getAuthor());
        assertEquals(5, dto.getTotalCopies());
        assertEquals(3, dto.getAvailableCopies());
    }

    @Test
    void bookDTO_setters() {
        BookDTO dto = new BookDTO();
        dto.setId(2L);
        dto.setTitle("New Title");
        dto.setAuthor("New Author");
        dto.setTotalCopies(10);
        dto.setAvailableCopies(7);

        assertEquals(2L, dto.getId());
        assertEquals("New Title", dto.getTitle());
        assertEquals("New Author", dto.getAuthor());
        assertEquals(10, dto.getTotalCopies());
        assertEquals(7, dto.getAvailableCopies());
    }

    // ---- BookRequest ----

    @Test
    void bookRequest_defaultConstructor() {
        BookRequest req = new BookRequest();
        assertNull(req.getTitle());
        assertNull(req.getAuthor());
        assertEquals(0, req.getTotalCopies());
    }

    @Test
    void bookRequest_parameterizedConstructor() {
        BookRequest req = new BookRequest("Title", "Author", 5);
        assertEquals("Title", req.getTitle());
        assertEquals("Author", req.getAuthor());
        assertEquals(5, req.getTotalCopies());
    }

    @Test
    void bookRequest_setters() {
        BookRequest req = new BookRequest();
        req.setTitle("T");
        req.setAuthor("A");
        req.setTotalCopies(3);

        assertEquals("T", req.getTitle());
        assertEquals("A", req.getAuthor());
        assertEquals(3, req.getTotalCopies());
    }

    // ---- MemberDTO ----

    @Test
    void memberDTO_defaultConstructor() {
        MemberDTO dto = new MemberDTO();
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getEmail());
        assertNull(dto.getCreatedAt());
    }

    @Test
    void memberDTO_parameterizedConstructor() {
        LocalDateTime now = LocalDateTime.now();
        MemberDTO dto = new MemberDTO(1L, "John", "john@test.com", now);
        assertEquals(1L, dto.getId());
        assertEquals("John", dto.getName());
        assertEquals("john@test.com", dto.getEmail());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void memberDTO_setters() {
        MemberDTO dto = new MemberDTO();
        LocalDateTime time = LocalDateTime.of(2025, 1, 1, 0, 0);
        dto.setId(3L);
        dto.setName("Jane");
        dto.setEmail("jane@test.com");
        dto.setCreatedAt(time);

        assertEquals(3L, dto.getId());
        assertEquals("Jane", dto.getName());
        assertEquals("jane@test.com", dto.getEmail());
        assertEquals(time, dto.getCreatedAt());
    }

    // ---- MemberRequest ----

    @Test
    void memberRequest_defaultConstructor() {
        MemberRequest req = new MemberRequest();
        assertNull(req.getName());
        assertNull(req.getEmail());
    }

    @Test
    void memberRequest_parameterizedConstructor() {
        MemberRequest req = new MemberRequest("John", "john@test.com");
        assertEquals("John", req.getName());
        assertEquals("john@test.com", req.getEmail());
    }

    @Test
    void memberRequest_setters() {
        MemberRequest req = new MemberRequest();
        req.setName("Jane");
        req.setEmail("jane@test.com");

        assertEquals("Jane", req.getName());
        assertEquals("jane@test.com", req.getEmail());
    }

    // ---- BorrowRecordDTO ----

    @Test
    void borrowRecordDTO_defaultConstructor() {
        BorrowRecordDTO dto = new BorrowRecordDTO();
        assertNull(dto.getId());
        assertNull(dto.getMemberId());
        assertNull(dto.getMemberName());
        assertNull(dto.getBookId());
        assertNull(dto.getBookTitle());
        assertNull(dto.getBorrowedAt());
        assertNull(dto.getReturnedAt());
    }

    @Test
    void borrowRecordDTO_parameterizedConstructor() {
        LocalDateTime borrowed = LocalDateTime.now().minusDays(7);
        LocalDateTime returned = LocalDateTime.now();
        BorrowRecordDTO dto = new BorrowRecordDTO(1L, 2L, "John", 3L, "Clean Code", borrowed, returned);

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getMemberId());
        assertEquals("John", dto.getMemberName());
        assertEquals(3L, dto.getBookId());
        assertEquals("Clean Code", dto.getBookTitle());
        assertEquals(borrowed, dto.getBorrowedAt());
        assertEquals(returned, dto.getReturnedAt());
    }

    @Test
    void borrowRecordDTO_setters() {
        BorrowRecordDTO dto = new BorrowRecordDTO();
        LocalDateTime now = LocalDateTime.now();
        dto.setId(5L);
        dto.setMemberId(10L);
        dto.setMemberName("Jane");
        dto.setBookId(20L);
        dto.setBookTitle("Refactoring");
        dto.setBorrowedAt(now);
        dto.setReturnedAt(now);

        assertEquals(5L, dto.getId());
        assertEquals(10L, dto.getMemberId());
        assertEquals("Jane", dto.getMemberName());
        assertEquals(20L, dto.getBookId());
        assertEquals("Refactoring", dto.getBookTitle());
        assertEquals(now, dto.getBorrowedAt());
        assertEquals(now, dto.getReturnedAt());
    }

    // ---- BorrowRequest ----

    @Test
    void borrowRequest_defaultConstructor() {
        BorrowRequest req = new BorrowRequest();
        assertNull(req.getMemberId());
        assertNull(req.getBookId());
    }

    @Test
    void borrowRequest_parameterizedConstructor() {
        BorrowRequest req = new BorrowRequest(1L, 2L);
        assertEquals(1L, req.getMemberId());
        assertEquals(2L, req.getBookId());
    }

    @Test
    void borrowRequest_setters() {
        BorrowRequest req = new BorrowRequest();
        req.setMemberId(5L);
        req.setBookId(10L);

        assertEquals(5L, req.getMemberId());
        assertEquals(10L, req.getBookId());
    }

    // ---- UpdateCopiesRequest ----

    @Test
    void updateCopiesRequest_defaultConstructor() {
        UpdateCopiesRequest req = new UpdateCopiesRequest();
        assertEquals(0, req.getTotalCopies());
    }

    @Test
    void updateCopiesRequest_parameterizedConstructor() {
        UpdateCopiesRequest req = new UpdateCopiesRequest(10);
        assertEquals(10, req.getTotalCopies());
    }

    @Test
    void updateCopiesRequest_setter() {
        UpdateCopiesRequest req = new UpdateCopiesRequest();
        req.setTotalCopies(15);
        assertEquals(15, req.getTotalCopies());
    }
}
