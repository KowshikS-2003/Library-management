package com.library.LibraryServiceInterface;

/**
 * Service contract for asynchronous book-related notifications.
 * Concrete behavior is provided by {@code BookNotificationServiceImpl}.
 */
public interface BookNotificationService {

    void sendBookCreatedNotification(Long bookId, String title);
}
