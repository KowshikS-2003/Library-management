package com.library.LibraryCustomExceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void resourceNotFoundException_shouldCarryMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Book not found");
        assertEquals("Book not found", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void bookNotAvailableException_shouldCarryMessage() {
        BookNotAvailableException ex = new BookNotAvailableException("No copies available");
        assertEquals("No copies available", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void borrowLimitExceededException_shouldCarryMessage() {
        BorrowLimitExceededException ex = new BorrowLimitExceededException("Limit exceeded");
        assertEquals("Limit exceeded", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void bookAlreadyReturnedException_shouldCarryMessage() {
        BookAlreadyReturnedException ex = new BookAlreadyReturnedException("Already returned");
        assertEquals("Already returned", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void invalidOperationException_shouldCarryMessage() {
        InvalidOperationException ex = new InvalidOperationException("Invalid operation");
        assertEquals("Invalid operation", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }
}
