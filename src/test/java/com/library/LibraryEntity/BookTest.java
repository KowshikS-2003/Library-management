package com.library.LibraryEntity;

import org.junit.jupiter.api.Test;

import com.library.LibraryEntity.LibraryCatalog.Book;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void defaultConstructor_shouldCreateEmptyBook() {
        Book book = new Book();
        assertNull(book.getId());
        assertNull(book.getTitle());
        assertNull(book.getAuthor());
        assertEquals(0, book.getTotalCopies());
        assertEquals(0, book.getAvailableCopies());
    }

    @Test
    void parameterizedConstructor_shouldSetFields() {
        Book book = new Book("Clean Code", "Robert Martin", 5, 3);

        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert Martin", book.getAuthor());
        assertEquals(5, book.getTotalCopies());
        assertEquals(3, book.getAvailableCopies());
    }

    @Test
    void setId_shouldUpdateId() {
        Book book = new Book();
        book.setId(10L);
        assertEquals(10L, book.getId());
    }

    @Test
    void setTitle_shouldUpdateTitle() {
        Book book = new Book();
        book.setTitle("New Title");
        assertEquals("New Title", book.getTitle());
    }

    @Test
    void setAuthor_shouldUpdateAuthor() {
        Book book = new Book();
        book.setAuthor("New Author");
        assertEquals("New Author", book.getAuthor());
    }

    @Test
    void setTotalCopies_shouldUpdateTotalCopies() {
        Book book = new Book();
        book.setTotalCopies(10);
        assertEquals(10, book.getTotalCopies());
    }

    @Test
    void setAvailableCopies_shouldUpdateAvailableCopies() {
        Book book = new Book();
        book.setAvailableCopies(7);
        assertEquals(7, book.getAvailableCopies());
    }

    @Test
    void toString_shouldContainAllFields() {
        Book book = new Book("Clean Code", "Robert Martin", 5, 3);
        book.setId(1L);

        String result = book.toString();

        assertTrue(result.contains("1"));
        assertTrue(result.contains("Clean Code"));
        assertTrue(result.contains("Robert Martin"));
        assertTrue(result.contains("3"));
        assertTrue(result.contains("5"));
    }
}
