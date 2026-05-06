package com.library.LibraryService;

import com.library.LibraryCustomExceptions.BookAlreadyReturnedException;
import com.library.LibraryCustomExceptions.BookNotAvailableException;
import com.library.LibraryCustomExceptions.BorrowLimitExceededException;
import com.library.LibraryCustomExceptions.ResourceNotFoundException;
import com.library.LibraryDTO.BorrowRecordDTO;
import com.library.LibraryEntity.LibraryCatalog.Book;
import com.library.LibraryEntity.LibraryCatalog.BorrowRecord;
import com.library.LibraryEntity.LibraryMmember.Member;
import com.library.LibraryRepository.CatalogRepo.BorrowRecordRepository;
import com.library.LibraryServiceInterface.BookService;
import com.library.LibraryServiceInterface.MemberService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookService bookService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    private Book book;
    private Member member;
    private BorrowRecord borrowRecord;

    @BeforeEach
    void setUp() {
        book = new Book("Clean Code", "Robert Martin", 5, 3);
        book.setId(1L);

        member = new Member("John Doe", "john@example.com");
        member.setId(1L);

        borrowRecord = new BorrowRecord(1L, 1L);
        borrowRecord.setId(1L);
    }

    @Test
    void borrowBook_shouldCreateBorrowRecord_whenValid() {
        when(bookService.getBookEntity(1L)).thenReturn(book);
        when(memberService.getMemberEntity(1L)).thenReturn(member);
        when(borrowRecordRepository.countByMemberIdAndReturnedAtIsNull(1L)).thenReturn(0L);
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(borrowRecord);

        BorrowRecordDTO result = borrowService.borrowBook(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getMemberId());
        assertEquals("John Doe", result.getMemberName());
        assertEquals(1L, result.getBookId());
        assertEquals("Clean Code", result.getBookTitle());
        assertNotNull(result.getBorrowedAt());
        assertNull(result.getReturnedAt());
        assertEquals(2, book.getAvailableCopies()); // decremented from 3
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    void borrowBook_shouldThrowBookNotAvailableException_whenNoCopies() {
        book.setAvailableCopies(0);
        when(bookService.getBookEntity(1L)).thenReturn(book);

        assertThrows(BookNotAvailableException.class, () -> borrowService.borrowBook(1L, 1L));
        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void borrowBook_shouldThrowBorrowLimitExceededException_whenLimitReached() {
        when(bookService.getBookEntity(1L)).thenReturn(book);
        when(memberService.getMemberEntity(1L)).thenReturn(member);
        when(borrowRecordRepository.countByMemberIdAndReturnedAtIsNull(1L)).thenReturn(3L);

        assertThrows(BorrowLimitExceededException.class, () -> borrowService.borrowBook(1L, 1L));
        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void returnBook_shouldSetReturnedAtAndIncrementCopies() {
        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(borrowRecord));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(borrowRecord);
        when(bookService.getBookEntity(1L)).thenReturn(book);
        when(memberService.getMemberEntity(1L)).thenReturn(member);

        int availableBefore = book.getAvailableCopies();
        BorrowRecordDTO result = borrowService.returnBook(1L);

        assertNotNull(result);
        assertNotNull(borrowRecord.getReturnedAt());
        assertEquals(availableBefore + 1, book.getAvailableCopies());
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    void returnBook_shouldThrowResourceNotFoundException_whenRecordNotFound() {
        when(borrowRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> borrowService.returnBook(99L));
    }

    @Test
    void returnBook_shouldThrowBookAlreadyReturnedException_whenAlreadyReturned() {
        borrowRecord.setReturnedAt(LocalDateTime.now());
        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(borrowRecord));

        assertThrows(BookAlreadyReturnedException.class, () -> borrowService.returnBook(1L));
        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void getBorrowHistory_shouldReturnRecordsForMember() {
        when(borrowRecordRepository.findByMemberIdOrderByBorrowedAtDesc(1L))
                .thenReturn(List.of(borrowRecord));
        when(memberService.getMemberEntity(1L)).thenReturn(member);
        when(bookService.getBookEntity(1L)).thenReturn(book);

        List<BorrowRecordDTO> result = borrowService.getBorrowHistory(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getMemberId());
    }

    @Test
    void getBorrowHistory_shouldReturnEmptyList_whenNoHistory() {
        when(borrowRecordRepository.findByMemberIdOrderByBorrowedAtDesc(1L))
                .thenReturn(Collections.emptyList());

        List<BorrowRecordDTO> result = borrowService.getBorrowHistory(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllBorrowRecords_shouldReturnAllRecords() {
        BorrowRecord record2 = new BorrowRecord(1L, 1L);
        record2.setId(2L);
        when(borrowRecordRepository.findAllByOrderByBorrowedAtDesc())
                .thenReturn(Arrays.asList(borrowRecord, record2));
        when(memberService.getMemberEntity(1L)).thenReturn(member);
        when(bookService.getBookEntity(1L)).thenReturn(book);

        List<BorrowRecordDTO> result = borrowService.getAllBorrowRecords();

        assertEquals(2, result.size());
    }

    @Test
    void getAllBorrowRecords_shouldReturnEmptyList_whenNoRecords() {
        when(borrowRecordRepository.findAllByOrderByBorrowedAtDesc())
                .thenReturn(Collections.emptyList());

        List<BorrowRecordDTO> result = borrowService.getAllBorrowRecords();

        assertTrue(result.isEmpty());
    }
}
