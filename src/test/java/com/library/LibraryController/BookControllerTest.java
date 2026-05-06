package com.library.LibraryController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.LibraryDTO.BookDTO;
import com.library.LibraryDTO.BookRequest;
import com.library.LibraryDTO.UpdateCopiesRequest;
import com.library.LibraryCustomExceptions.InvalidOperationException;
import com.library.LibraryCustomExceptions.ResourceNotFoundException;
import com.library.LibraryServiceInterface.BookService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addBook_shouldReturnCreatedBook() throws Exception {
        BookDTO dto = new BookDTO(1L, "Clean Code", "Robert Martin", 5, 5);
        when(bookService.addBook(any(BookRequest.class))).thenReturn(dto);

        BookRequest request = new BookRequest("Clean Code", "Robert Martin", 5);

        mockMvc.perform(post("/api/v1/books/addBooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("000001"))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert Martin"))
                .andExpect(jsonPath("$.totalCopies").value(5))
                .andExpect(jsonPath("$.availableCopies").value(5));
    }

    @Test
    void getAllBooks_shouldReturnListOfBooks() throws Exception {
        BookDTO dto1 = new BookDTO(1L, "Clean Code", "Robert Martin", 5, 5);
        BookDTO dto2 = new BookDTO(2L, "Refactoring", "Martin Fowler", 3, 3);
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/api/v1/books/getAllBooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[1].title").value("Refactoring"));
    }

    @Test
    void getAllBooks_shouldReturnEmptyList() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/books/getAllBooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAvailableBooks_shouldReturnAvailableBooks() throws Exception {
        BookDTO dto = new BookDTO(1L, "Clean Code", "Robert Martin", 5, 3);
        when(bookService.getAvailableBooks()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/books/getAvailableBooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].availableCopies").value(3));
    }

    @Test
    void getBook_shouldReturnBookById() throws Exception {
        BookDTO dto = new BookDTO(1L, "Clean Code", "Robert Martin", 5, 5);
        when(bookService.getBook(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/books/getBook/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("000001"))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void updateCopies_shouldReturnUpdatedBook() throws Exception {
        BookDTO dto = new BookDTO(1L, "Clean Code", "Robert Martin", 8, 8);
        when(bookService.updateBookCopies(eq(1L), eq(8))).thenReturn(dto);

        UpdateCopiesRequest request = new UpdateCopiesRequest(8);

        mockMvc.perform(put("/api/v1/books/updateCopies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCopies").value(8))
                .andExpect(jsonPath("$.availableCopies").value(8));
    }

    @Test
    void getBook_shouldReturn404_whenBookNotFound() throws Exception {
        when(bookService.getBook(99L)).thenThrow(new ResourceNotFoundException("Book with id 99 not found."));

        mockMvc.perform(get("/api/v1/books/getBook/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Book with id 99 not found."));
    }

    @Test
    void updateCopies_shouldReturn400_whenCannotReduceBelowLentOut() throws Exception {
        when(bookService.updateBookCopies(eq(1L), eq(1)))
                .thenThrow(new InvalidOperationException("Cannot reduce total_copies below the number currently lent out."));

        UpdateCopiesRequest request = new UpdateCopiesRequest(1);

        mockMvc.perform(put("/api/v1/books/updateCopies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
