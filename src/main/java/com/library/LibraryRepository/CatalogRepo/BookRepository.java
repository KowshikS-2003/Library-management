package com.library.LibraryRepository.CatalogRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.LibraryEntity.LibraryCatalog.Book;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByAvailableCopiesGreaterThanOrderByTitle(int minCopies);
}
