package com.library.LibraryController;

import com.library.LibraryDTO.BorrowRecordDTO;
import com.library.LibraryDTO.BorrowRequest;
import com.library.LibraryServiceInterface.BorrowService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/borrows")
@Validated
public class BorrowController {

    private static final Logger logger = LoggerFactory.getLogger(BorrowController.class);

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping("/borrowBook")
    public ResponseEntity<BorrowRecordDTO> borrowBook(@Valid @RequestBody BorrowRequest request) {
        logger.info("POST /api/v1/borrows/borrowBook - Borrowing book");
        BorrowRecordDTO record = borrowService.borrowBook(request.getMemberId(), request.getBookId());
        return ResponseEntity.ok(record);
    }

    @PutMapping("/returnBook/{id}")
    public ResponseEntity<BorrowRecordDTO> returnBook(@PathVariable("id") @Positive(message = "Borrow ID must be a positive number") Long id) {
        logger.info("PUT /api/v1/borrows/returnBook/{} - Returning book", id);
        BorrowRecordDTO record = borrowService.returnBook(id);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/getAllBorrows")
    public ResponseEntity<List<BorrowRecordDTO>> getAllBorrows() {
        logger.info("GET /api/v1/borrows/getAllBorrows - Fetching all borrow records");
        return ResponseEntity.ok(borrowService.getAllBorrowRecords());
    }

    @GetMapping("/getBorrowHistory/{memberId}")
    public ResponseEntity<List<BorrowRecordDTO>> getBorrowHistory(@PathVariable("memberId") @Positive(message = "Member ID must be a positive number") Long memberId) {
        logger.info("GET /api/v1/borrows/getBorrowHistory/{} - Fetching borrow history", memberId);
        return ResponseEntity.ok(borrowService.getBorrowHistory(memberId));
    }
}
