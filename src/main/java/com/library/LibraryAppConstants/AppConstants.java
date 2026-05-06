package com.library.LibraryAppConstants;

public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // ---- Business Rules ----
    public static final int MAX_ACTIVE_BORROWS = 3;
    public static final int MIN_AVAILABLE_COPIES = 0;

    // ---- Error Messages ----
    public static final String BOOK_NOT_FOUND = "Book with id %d not found.";
    public static final String MEMBER_NOT_FOUND = "Member with id %d not found.";
    public static final String BORROW_RECORD_NOT_FOUND = "BorrowRecord with id %d not found.";
    public static final String NO_COPIES_AVAILABLE = "No copies available for borrowing.";
    public static final String BORROW_LIMIT_EXCEEDED = "Member already has 3 active borrows. Return a book first.";
    public static final String BOOK_ALREADY_RETURNED = "This book has already been returned.";
    public static final String CANNOT_REDUCE_COPIES = "Cannot reduce total_copies below the number currently lent out.";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred.";

    // ---- Error Labels ----
    public static final String ERROR_NOT_FOUND = "Not Found";
    public static final String ERROR_CONFLICT = "Conflict";
    public static final String ERROR_BAD_REQUEST = "Bad Request";
    public static final String ERROR_VALIDATION_FAILED = "Validation Failed";
    public static final String ERROR_INTERNAL_SERVER = "Internal Server Error";

    // ---- Log Messages ----
    public static final String LOG_ADDING_BOOK = "Adding new book: title='{}', author='{}'";
    public static final String LOG_BOOK_ADDED = "Book added successfully with id={}";
    public static final String LOG_BOOK_NOT_FOUND = "Book with id={} not found";
    public static final String LOG_FETCHING_BOOK = "Fetching book with id={}";
    public static final String LOG_FETCHING_ALL_BOOKS = "Fetching all books";
    public static final String LOG_FETCHING_AVAILABLE_BOOKS = "Fetching available books";
    public static final String LOG_UPDATING_COPIES = "Updating copies for book id={} to totalCopies={}";
    public static final String LOG_CANNOT_REDUCE_COPIES = "Cannot reduce total copies below lent out count for book id={}";
    public static final String LOG_COPIES_UPDATED = "Book copies updated successfully for id={}";

    public static final String LOG_ADDING_MEMBER = "Adding new member: name='{}', email='{}'";
    public static final String LOG_MEMBER_ADDED = "Member added successfully with id={}";
    public static final String LOG_MEMBER_NOT_FOUND = "Member with id={} not found";
    public static final String LOG_FETCHING_MEMBER = "Fetching member with id={}";
    public static final String LOG_FETCHING_ALL_MEMBERS = "Fetching all members";

    public static final String LOG_BORROW_REQUEST = "Processing borrow request: memberId={}, bookId={}";
    public static final String LOG_NO_COPIES = "No copies available for book id={}";
    public static final String LOG_BORROW_LIMIT = "Member id={} has reached borrow limit of 3";
    public static final String LOG_BORROW_CREATED = "Borrow record created with id={}";
    public static final String LOG_RETURN_REQUEST = "Processing return for borrow record id={}";
    public static final String LOG_BORROW_NOT_FOUND = "BorrowRecord with id={} not found";
    public static final String LOG_ALREADY_RETURNED = "Borrow record id={} has already been returned";
    public static final String LOG_RETURN_SUCCESS = "Book returned successfully for borrow record id={}";
    public static final String LOG_FETCHING_HISTORY = "Fetching borrow history for member id={}";
    public static final String LOG_FETCHING_ALL_BORROWS = "Fetching all borrow records";

    public static final String LOG_RESOURCE_NOT_FOUND = "Resource not found: {}";
    public static final String LOG_BOOK_NOT_AVAILABLE = "Book not available: {}";
    public static final String LOG_BORROW_LIMIT_EXCEEDED = "Borrow limit exceeded: {}";
    public static final String LOG_BOOK_ALREADY_RETURNED = "Book already returned: {}";
    public static final String LOG_INVALID_OPERATION = "Invalid operation: {}";
    public static final String LOG_VALIDATION_FAILED = "Validation failed: {}";
    public static final String LOG_UNEXPECTED_ERROR = "Unexpected error occurred";
}
