package com.library.LibraryService;

import com.library.LibraryAppConstants.AppConstants;
import com.library.LibraryCustomExceptions.BookAlreadyReturnedException;
import com.library.LibraryCustomExceptions.BookNotAvailableException;
import com.library.LibraryCustomExceptions.BorrowLimitExceededException;
import com.library.LibraryCustomExceptions.ResourceNotFoundException;
import com.library.LibraryDTO.BorrowRecordDTO;
import com.library.LibraryEntity.LibraryCatalog.Book;
import com.library.LibraryEntity.LibraryCatalog.BorrowRecord;
import com.library.LibraryEntity.LibraryMmember.Member;
import com.library.LibraryRepository.CatalogRepo.BorrowRecordRepository;
import com.library.LibraryServiceInterface.BookService;
import com.library.LibraryServiceInterface.BorrowService;
import com.library.LibraryServiceInterface.MemberService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowServiceImpl implements BorrowService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowServiceImpl.class);

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookService bookService;
    private final MemberService memberService;

    public BorrowServiceImpl(BorrowRecordRepository borrowRecordRepository,
                             BookService bookService,
                             MemberService memberService) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookService = bookService;
        this.memberService = memberService;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "books", key = "#bookId"),
            @CacheEvict(value = {"allBooks", "availableBooks", "allBorrows"}, allEntries = true),
            @CacheEvict(value = "borrowHistory", key = "#memberId")
    })
    @Transactional("catalogTransactionManager")
    public BorrowRecordDTO borrowBook(Long memberId, Long bookId) {
        logger.info(AppConstants.LOG_BORROW_REQUEST, memberId, bookId);

        Book book = bookService.getBookEntity(bookId);
        if (book.getAvailableCopies() <= 0) {
            logger.warn(AppConstants.LOG_NO_COPIES, bookId);
            throw new BookNotAvailableException(AppConstants.NO_COPIES_AVAILABLE);
        }

        // Validate member exists in the member database
        Member member = memberService.getMemberEntity(memberId);

        long activeBorrows = borrowRecordRepository.countByMemberIdAndReturnedAtIsNull(memberId);
        if (activeBorrows >= AppConstants.MAX_ACTIVE_BORROWS) {
            logger.warn(AppConstants.LOG_BORROW_LIMIT, memberId);
            throw new BorrowLimitExceededException(AppConstants.BORROW_LIMIT_EXCEEDED);
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        BorrowRecord record = new BorrowRecord(memberId, bookId);
        BorrowRecord saved = borrowRecordRepository.save(record);
        logger.info(AppConstants.LOG_BORROW_CREATED, saved.getId());
        return toDTO(saved, member.getName(), book.getTitle());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = {"allBooks", "availableBooks", "allBorrows", "borrowHistory"}, allEntries = true),
            @CacheEvict(value = "books", allEntries = true)
    })
    @Transactional("catalogTransactionManager")
    public BorrowRecordDTO returnBook(Long borrowId) {
        logger.info(AppConstants.LOG_RETURN_REQUEST, borrowId);

        BorrowRecord record = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> {
                    logger.error(AppConstants.LOG_BORROW_NOT_FOUND, borrowId);
                    return new ResourceNotFoundException(
                            String.format(AppConstants.BORROW_RECORD_NOT_FOUND, borrowId));
                });

        if (record.getReturnedAt() != null) {
            logger.warn(AppConstants.LOG_ALREADY_RETURNED, borrowId);
            throw new BookAlreadyReturnedException(AppConstants.BOOK_ALREADY_RETURNED);
        }

        record.setReturnedAt(LocalDateTime.now());

        Book book = bookService.getBookEntity(record.getBookId());
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        BorrowRecord saved = borrowRecordRepository.save(record);
        logger.info(AppConstants.LOG_RETURN_SUCCESS, borrowId);

        Member member = memberService.getMemberEntity(record.getMemberId());
        return toDTO(saved, member.getName(), book.getTitle());
    }

    @Override
    @Cacheable(value = "borrowHistory", key = "#memberId")
    public List<BorrowRecordDTO> getBorrowHistory(Long memberId) {
        logger.info(AppConstants.LOG_FETCHING_HISTORY, memberId);
        return borrowRecordRepository.findByMemberIdOrderByBorrowedAtDesc(memberId)
                .stream().map(this::toDTOWithLookup).collect(Collectors.toList());
    }

    @Override
    @Cacheable("allBorrows")
    public List<BorrowRecordDTO> getAllBorrowRecords() {
        logger.info(AppConstants.LOG_FETCHING_ALL_BORROWS);
        return borrowRecordRepository.findAllByOrderByBorrowedAtDesc()
                .stream().map(this::toDTOWithLookup).collect(Collectors.toList());
    }

    private BorrowRecordDTO toDTOWithLookup(BorrowRecord record) {
        Member member = memberService.getMemberEntity(record.getMemberId());
        Book book = bookService.getBookEntity(record.getBookId());
        return toDTO(record, member.getName(), book.getTitle());
    }

    private BorrowRecordDTO toDTO(BorrowRecord record, String memberName, String bookTitle) {
        return new BorrowRecordDTO(
                record.getId(),
                record.getMemberId(),
                memberName,
                record.getBookId(),
                bookTitle,
                record.getBorrowedAt(),
                record.getReturnedAt()
        );
    }
}
