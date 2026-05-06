package com.library.LibraryRepository.CatalogRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.LibraryEntity.LibraryCatalog.BorrowRecord;

import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    long countByMemberIdAndReturnedAtIsNull(Long memberId);

    List<BorrowRecord> findByMemberIdOrderByBorrowedAtDesc(Long memberId);

    List<BorrowRecord> findAllByOrderByBorrowedAtDesc();
}
