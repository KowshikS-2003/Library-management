package com.library.LibraryService;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BookNotificationServiceImplTest {

    private final BookNotificationServiceImpl service = new BookNotificationServiceImpl();

    @Test
    void sendBookCreatedNotification_shouldExecuteWithoutException() {
        assertDoesNotThrow(() -> service.sendBookCreatedNotification(1L, "Clean Code"));
    }

    @Test
    void sendBookCreatedNotification_shouldHandleNullTitle() {
        assertDoesNotThrow(() -> service.sendBookCreatedNotification(2L, null));
    }
}
