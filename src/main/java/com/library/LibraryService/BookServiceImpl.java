package com.library.LibraryService;

import com.library.LibraryAppConstants.AppConstants;
import com.library.LibraryCustomExceptions.InvalidOperationException;
import com.library.LibraryCustomExceptions.ResourceNotFoundException;
import com.library.LibraryDTO.BookDTO;
import com.library.LibraryDTO.BookRequest;
import com.library.LibraryEntity.LibraryCatalog.Book;
import com.library.LibraryRepository.CatalogRepo.BookRepository;
import com.library.LibraryServiceInterface.BookNotificationService;
import com.library.LibraryServiceInterface.BookService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;
    private final BookNotificationService bookNotificationService;

    public BookServiceImpl(BookRepository bookRepository, BookNotificationService bookNotificationService) {
        this.bookRepository = bookRepository;
        this.bookNotificationService = bookNotificationService;
    }

    @Override
    @CacheEvict(value = {"allBooks", "availableBooks"}, allEntries = true)
    public BookDTO addBook(BookRequest request) {
        logger.info(AppConstants.LOG_ADDING_BOOK, request.getTitle(), request.getAuthor());
        Book book = new Book(request.getTitle(), request.getAuthor(),
                request.getTotalCopies(), request.getTotalCopies());
        Book saved = bookRepository.save(book);
        logger.info(AppConstants.LOG_BOOK_ADDED, saved.getId());
        bookNotificationService.sendBookCreatedNotification(saved.getId(), saved.getTitle());
        return toDTO(saved);
    }

    @Override
    public Book getBookEntity(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error(AppConstants.LOG_BOOK_NOT_FOUND, id);
                    return new ResourceNotFoundException(String.format(AppConstants.BOOK_NOT_FOUND, id));
                });
    }

    @Override
    @Cacheable(value = "books", key = "#id")
    public BookDTO getBook(Long id) {
        logger.info(AppConstants.LOG_FETCHING_BOOK, id);
        return toDTO(getBookEntity(id));
    }

    @Override
    @Cacheable("allBooks")
    public List<BookDTO> getAllBooks() {
        logger.info(AppConstants.LOG_FETCHING_ALL_BOOKS);
        return bookRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Cacheable("availableBooks")
    public List<BookDTO> getAvailableBooks() {
        logger.info(AppConstants.LOG_FETCHING_AVAILABLE_BOOKS);
        return bookRepository.findByAvailableCopiesGreaterThanOrderByTitle(0)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "books", key = "#bookId"),
            @CacheEvict(value = {"allBooks", "availableBooks"}, allEntries = true)
    })
    public BookDTO updateBookCopies(Long bookId, int newTotalCopies) {
        logger.info(AppConstants.LOG_UPDATING_COPIES, bookId, newTotalCopies);
        Book book = getBookEntity(bookId);
        int delta = newTotalCopies - book.getTotalCopies();
        int newAvailable = book.getAvailableCopies() + delta;
        if (newAvailable < 0) {
            logger.error(AppConstants.LOG_CANNOT_REDUCE_COPIES, bookId);
            throw new InvalidOperationException(AppConstants.CANNOT_REDUCE_COPIES);
        }
        book.setTotalCopies(newTotalCopies);
        book.setAvailableCopies(newAvailable);
        Book saved = bookRepository.save(book);
        logger.info(AppConstants.LOG_COPIES_UPDATED, bookId);
        return toDTO(saved);
    }

    @Override
    public BookDTO toDTO(Book book) {
        return new BookDTO(book.getId(), book.getTitle(), book.getAuthor(),
                book.getTotalCopies(), book.getAvailableCopies());
    }
}
