package com.library.LibraryController;

import com.library.LibraryDTO.BookDTO;
import com.library.LibraryDTO.BookRequest;
import com.library.LibraryDTO.UpdateCopiesRequest;
import com.library.LibraryServiceInterface.BookService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@Validated
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/addBooks")
    public ResponseEntity<BookDTO> addBook(@Valid @RequestBody BookRequest request) {
        logger.info("POST /api/v1/books/addBooks - Adding new book");
        BookDTO book = bookService.addBook(request);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/getAllBooks")
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        logger.info("GET /api/v1/books/getAllBooks - Fetching all books");
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/getAvailableBooks")
    public ResponseEntity<List<BookDTO>> getAvailableBooks() {
        logger.info("GET /api/v1/books/getAvailableBooks - Fetching available books");
        return ResponseEntity.ok(bookService.getAvailableBooks());
    }

    @GetMapping("/getBook/{id}")
    public ResponseEntity<BookDTO> getBook(@PathVariable("id") @Positive(message = "Book ID must be a positive number") Long id) {
        logger.info("GET /api/v1/books/getBook/{} - Fetching book", id);
        return ResponseEntity.ok(bookService.getBook(id));
    }

    @PutMapping("/updateCopies/{id}")
    public ResponseEntity<BookDTO> updateCopies(@PathVariable("id") @Positive(message = "Book ID must be a positive number") Long id,
                                                @Valid @RequestBody UpdateCopiesRequest request) {
        logger.info("PUT /api/v1/books/updateCopies/{} - Updating copies", id);
        return ResponseEntity.ok(bookService.updateBookCopies(id, request.getTotalCopies()));
    }
}
