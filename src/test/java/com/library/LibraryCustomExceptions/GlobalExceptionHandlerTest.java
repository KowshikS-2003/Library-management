package com.library.LibraryCustomExceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_shouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Book with id 1 not found.");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Book with id 1 not found.", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleBookNotAvailable_shouldReturn409() {
        BookNotAvailableException ex = new BookNotAvailableException("No copies available for borrowing.");

        ResponseEntity<ErrorResponse> response = handler.handleBookNotAvailable(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("No copies available for borrowing.", response.getBody().getMessage());
    }

    @Test
    void handleBorrowLimitExceeded_shouldReturn409() {
        BorrowLimitExceededException ex = new BorrowLimitExceededException("Member already has 3 active borrows. Return a book first.");

        ResponseEntity<ErrorResponse> response = handler.handleBorrowLimitExceeded(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("Member already has 3 active borrows. Return a book first.", response.getBody().getMessage());
    }

    @Test
    void handleBookAlreadyReturned_shouldReturn409() {
        BookAlreadyReturnedException ex = new BookAlreadyReturnedException("This book has already been returned.");

        ResponseEntity<ErrorResponse> response = handler.handleBookAlreadyReturned(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("This book has already been returned.", response.getBody().getMessage());
    }

    @Test
    void handleInvalidOperation_shouldReturn400() {
        InvalidOperationException ex = new InvalidOperationException("Cannot reduce total_copies below the number currently lent out.");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidOperation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Cannot reduce total_copies below the number currently lent out.", response.getBody().getMessage());
    }

    @Test
    void handleGenericException_shouldReturn500() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred.", response.getBody().getMessage());
    }
}
