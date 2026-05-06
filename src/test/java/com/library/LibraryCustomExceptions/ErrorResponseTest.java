package com.library.LibraryCustomExceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        ErrorResponse response = new ErrorResponse(404, "Not Found", "Resource not found");

        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Resource not found", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void setStatus_shouldUpdateStatus() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "Invalid");
        response.setStatus(500);
        assertEquals(500, response.getStatus());
    }

    @Test
    void setError_shouldUpdateError() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "Invalid");
        response.setError("Internal Server Error");
        assertEquals("Internal Server Error", response.getError());
    }

    @Test
    void setMessage_shouldUpdateMessage() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "Invalid");
        response.setMessage("Updated message");
        assertEquals("Updated message", response.getMessage());
    }

    @Test
    void setTimestamp_shouldUpdateTimestamp() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "Invalid");
        java.time.LocalDateTime newTime = java.time.LocalDateTime.of(2025, 1, 1, 0, 0);
        response.setTimestamp(newTime);
        assertEquals(newTime, response.getTimestamp());
    }
}
