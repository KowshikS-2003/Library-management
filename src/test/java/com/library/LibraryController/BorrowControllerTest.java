package com.library.LibraryController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.LibraryDTO.BorrowRecordDTO;
import com.library.LibraryDTO.BorrowRequest;
import com.library.LibraryCustomExceptions.BookAlreadyReturnedException;
import com.library.LibraryCustomExceptions.BookNotAvailableException;
import com.library.LibraryCustomExceptions.BorrowLimitExceededException;
import com.library.LibraryCustomExceptions.ResourceNotFoundException;
import com.library.LibraryServiceInterface.BorrowService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowController.class)
@AutoConfigureMockMvc(addFilters = false)
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowService borrowService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void borrowBook_shouldReturnBorrowRecord() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        BorrowRecordDTO dto = new BorrowRecordDTO(1L, 1L, "John Doe", 1L, "Clean Code", now, null);
        when(borrowService.borrowBook(eq(1L), eq(1L))).thenReturn(dto);

        BorrowRequest request = new BorrowRequest(1L, 1L);

        mockMvc.perform(post("/api/v1/borrows/borrowBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("000001"))
                .andExpect(jsonPath("$.memberId").value("000001"))
                .andExpect(jsonPath("$.memberName").value("John Doe"))
                .andExpect(jsonPath("$.bookId").value("000001"))
                .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
                .andExpect(jsonPath("$.returnedAt").isEmpty());
    }

    @Test
    void returnBook_shouldReturnUpdatedRecord() throws Exception {
        LocalDateTime borrowedAt = LocalDateTime.now().minusDays(7);
        LocalDateTime returnedAt = LocalDateTime.now();
        BorrowRecordDTO dto = new BorrowRecordDTO(1L, 1L, "John Doe", 1L, "Clean Code", borrowedAt, returnedAt);
        when(borrowService.returnBook(eq(1L))).thenReturn(dto);

        mockMvc.perform(put("/api/v1/borrows/returnBook/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("000001"))
                .andExpect(jsonPath("$.returnedAt").isNotEmpty());
    }

    @Test
    void getAllBorrows_shouldReturnAllRecords() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        BorrowRecordDTO dto1 = new BorrowRecordDTO(1L, 1L, "John Doe", 1L, "Clean Code", now, null);
        BorrowRecordDTO dto2 = new BorrowRecordDTO(2L, 2L, "Jane Smith", 2L, "Refactoring", now, null);
        when(borrowService.getAllBorrowRecords()).thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/api/v1/borrows/getAllBorrows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllBorrows_shouldReturnEmptyList() throws Exception {
        when(borrowService.getAllBorrowRecords()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/borrows/getAllBorrows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getBorrowHistory_shouldReturnHistoryForMember() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        BorrowRecordDTO dto = new BorrowRecordDTO(1L, 1L, "John Doe", 1L, "Clean Code", now, null);
        when(borrowService.getBorrowHistory(eq(1L))).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/borrows/getBorrowHistory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].memberId").value("000001"));
    }

    @Test
    void getBorrowHistory_shouldReturnEmptyList_whenNoHistory() throws Exception {
        when(borrowService.getBorrowHistory(eq(1L))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/borrows/getBorrowHistory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void borrowBook_shouldReturn409_whenNoCopiesAvailable() throws Exception {
        when(borrowService.borrowBook(eq(1L), eq(1L)))
                .thenThrow(new BookNotAvailableException("No copies available for borrowing."));

        BorrowRequest request = new BorrowRequest(1L, 1L);

        mockMvc.perform(post("/api/v1/borrows/borrowBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("No copies available for borrowing."));
    }

    @Test
    void borrowBook_shouldReturn409_whenBorrowLimitExceeded() throws Exception {
        when(borrowService.borrowBook(eq(1L), eq(1L)))
                .thenThrow(new BorrowLimitExceededException("Member already has 3 active borrows. Return a book first."));

        BorrowRequest request = new BorrowRequest(1L, 1L);

        mockMvc.perform(post("/api/v1/borrows/borrowBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void returnBook_shouldReturn404_whenRecordNotFound() throws Exception {
        when(borrowService.returnBook(eq(99L)))
                .thenThrow(new ResourceNotFoundException("BorrowRecord with id 99 not found."));

        mockMvc.perform(put("/api/v1/borrows/returnBook/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void returnBook_shouldReturn409_whenAlreadyReturned() throws Exception {
        when(borrowService.returnBook(eq(1L)))
                .thenThrow(new BookAlreadyReturnedException("This book has already been returned."));

        mockMvc.perform(put("/api/v1/borrows/returnBook/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("This book has already been returned."));
    }
}
