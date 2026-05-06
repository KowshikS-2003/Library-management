package com.library.LibraryService;

import com.library.LibraryCustomExceptions.InvalidOperationException;
import com.library.LibraryCustomExceptions.ResourceNotFoundException;
import com.library.LibraryDTO.BookDTO;
import com.library.LibraryDTO.BookRequest;
import com.library.LibraryEntity.LibraryCatalog.Book;
import com.library.LibraryRepository.CatalogRepo.BookRepository;
import com.library.LibraryServiceInterface.BookNotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookNotificationService bookNotificationService;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book("Clean Code", "Robert Martin", 5, 5);
        book.setId(1L);
    }

    @Test
    void addBook_shouldSaveAndReturnBookDTO() {
        BookRequest request = new BookRequest("Clean Code", "Robert Martin", 5);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDTO result = bookService.addBook(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Clean Code", result.getTitle());
        assertEquals("Robert Martin", result.getAuthor());
        assertEquals(5, result.getTotalCopies());
        assertEquals(5, result.getAvailableCopies());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void getBook_shouldReturnBookDTO_whenBookExists() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookDTO result = bookService.getBook(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Clean Code", result.getTitle());
    }

    @Test
    void getBook_shouldThrowResourceNotFoundException_whenBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBook(99L));
    }

    @Test
    void getBookEntity_shouldReturnBook_whenExists() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.getBookEntity(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getBookEntity_shouldThrowResourceNotFoundException_whenNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookEntity(99L));
    }

    @Test
    void getAllBooks_shouldReturnListOfBookDTOs() {
        Book book2 = new Book("Refactoring", "Martin Fowler", 3, 3);
        book2.setId(2L);
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book, book2));

        List<BookDTO> result = bookService.getAllBooks();

        assertEquals(2, result.size());
        assertEquals("Clean Code", result.get(0).getTitle());
        assertEquals("Refactoring", result.get(1).getTitle());
    }

    @Test
    void getAllBooks_shouldReturnEmptyList_whenNoBooks() {
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        List<BookDTO> result = bookService.getAllBooks();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableBooks_shouldReturnOnlyAvailableBooks() {
        when(bookRepository.findByAvailableCopiesGreaterThanOrderByTitle(0))
                .thenReturn(List.of(book));

        List<BookDTO> result = bookService.getAvailableBooks();

        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).getTitle());
    }

    @Test
    void updateBookCopies_shouldIncreaseAvailableCopies() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        Book updated = new Book("Clean Code", "Robert Martin", 8, 8);
        updated.setId(1L);
        when(bookRepository.save(any(Book.class))).thenReturn(updated);

        BookDTO result = bookService.updateBookCopies(1L, 8);

        assertEquals(8, result.getTotalCopies());
        assertEquals(8, result.getAvailableCopies());
    }

    @Test
    void updateBookCopies_shouldDecreaseCopiesWhenPossible() {
        // book has 5 total, 5 available (0 lent out) -> can reduce to 3
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        Book updated = new Book("Clean Code", "Robert Martin", 3, 3);
        updated.setId(1L);
        when(bookRepository.save(any(Book.class))).thenReturn(updated);

        BookDTO result = bookService.updateBookCopies(1L, 3);

        assertEquals(3, result.getTotalCopies());
    }

    @Test
    void updateBookCopies_shouldThrowInvalidOperationException_whenReducingBelowLentOut() {
        // book has 5 total, 2 available (3 lent out) -> cannot reduce to 1
        book.setAvailableCopies(2);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(InvalidOperationException.class, () -> bookService.updateBookCopies(1L, 1));
    }

    @Test
    void toDTO_shouldMapAllFields() {
        BookDTO dto = bookService.toDTO(book);

        assertEquals(book.getId(), dto.getId());
        assertEquals(book.getTitle(), dto.getTitle());
        assertEquals(book.getAuthor(), dto.getAuthor());
        assertEquals(book.getTotalCopies(), dto.getTotalCopies());
        assertEquals(book.getAvailableCopies(), dto.getAvailableCopies());
    }
}
