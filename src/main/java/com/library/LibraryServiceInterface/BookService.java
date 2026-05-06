package com.library.LibraryServiceInterface;

import com.library.LibraryDTO.BookDTO;
import com.library.LibraryDTO.BookRequest;
import com.library.LibraryEntity.LibraryCatalog.Book;

import java.util.List;

/**
 * Service contract for Book operations. Controllers depend on this interface;
 * concrete behavior is provided by {@code BookServiceImpl}.
 */
public interface BookService {

    BookDTO addBook(BookRequest request);

    Book getBookEntity(Long id);

    BookDTO getBook(Long id);

    List<BookDTO> getAllBooks();

    List<BookDTO> getAvailableBooks();

    BookDTO updateBookCopies(Long bookId, int newTotalCopies);

    BookDTO toDTO(Book book);
}
