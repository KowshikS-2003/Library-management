package com.library.LibraryService;

import com.library.LibraryServiceInterface.BookNotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class BookNotificationServiceImpl implements BookNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(BookNotificationServiceImpl.class);

    @Override
    @Async("asyncExecutor")
    public void sendBookCreatedNotification(Long bookId, String title) {
        logger.info("Product created notification sent");
        logger.info("Async notification triggered for book ID: {} - Title: '{}' on thread: {}",
                bookId, title, Thread.currentThread().getName());
    }
}
