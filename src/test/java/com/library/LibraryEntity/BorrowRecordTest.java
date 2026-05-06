package com.library.LibraryEntity;

import org.junit.jupiter.api.Test;

import com.library.LibraryEntity.LibraryCatalog.BorrowRecord;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BorrowRecordTest {

    @Test
    void defaultConstructor_shouldCreateEmptyRecord() {
        BorrowRecord record = new BorrowRecord();
        assertNull(record.getId());
        assertNull(record.getMemberId());
        assertNull(record.getBookId());
        assertNull(record.getReturnedAt());
    }

    @Test
    void parameterizedConstructor_shouldSetMemberIdBookIdAndBorrowedAt() {
        BorrowRecord record = new BorrowRecord(1L, 2L);

        assertEquals(1L, record.getMemberId());
        assertEquals(2L, record.getBookId());
        assertNotNull(record.getBorrowedAt());
        assertNull(record.getReturnedAt());
    }

    @Test
    void setId_shouldUpdateId() {
        BorrowRecord record = new BorrowRecord();
        record.setId(10L);
        assertEquals(10L, record.getId());
    }

    @Test
    void setMemberId_shouldUpdateMemberId() {
        BorrowRecord record = new BorrowRecord();
        record.setMemberId(5L);
        assertEquals(5L, record.getMemberId());
    }

    @Test
    void setBookId_shouldUpdateBookId() {
        BorrowRecord record = new BorrowRecord();
        record.setBookId(7L);
        assertEquals(7L, record.getBookId());
    }

    @Test
    void setBorrowedAt_shouldUpdateBorrowedAt() {
        BorrowRecord record = new BorrowRecord();
        LocalDateTime time = LocalDateTime.of(2025, 3, 10, 14, 0);
        record.setBorrowedAt(time);
        assertEquals(time, record.getBorrowedAt());
    }

    @Test
    void setReturnedAt_shouldUpdateReturnedAt() {
        BorrowRecord record = new BorrowRecord();
        LocalDateTime time = LocalDateTime.of(2025, 3, 20, 14, 0);
        record.setReturnedAt(time);
        assertEquals(time, record.getReturnedAt());
    }

    @Test
    void toString_activeRecord_shouldContainActiveStatus() {
        BorrowRecord record = new BorrowRecord(1L, 2L);
        record.setId(1L);

        String result = record.toString();

        assertTrue(result.contains("active"));
        assertTrue(result.contains("1"));
        assertTrue(result.contains("2"));
    }

    @Test
    void toString_returnedRecord_shouldContainReturnedStatus() {
        BorrowRecord record = new BorrowRecord(1L, 2L);
        record.setId(1L);
        record.setReturnedAt(LocalDateTime.now());

        String result = record.toString();

        assertTrue(result.contains("returned"));
    }
}
