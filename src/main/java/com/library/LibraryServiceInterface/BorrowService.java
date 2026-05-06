package com.library.LibraryServiceInterface;

import com.library.LibraryDTO.BorrowRecordDTO;

import java.util.List;

/**
 * Service contract for Borrow operations. Controllers depend on this interface;
 * concrete behavior is provided by {@code BorrowServiceImpl}.
 */
public interface BorrowService {

    BorrowRecordDTO borrowBook(Long memberId, Long bookId);

    BorrowRecordDTO returnBook(Long borrowId);

    List<BorrowRecordDTO> getBorrowHistory(Long memberId);

    List<BorrowRecordDTO> getAllBorrowRecords();
}
