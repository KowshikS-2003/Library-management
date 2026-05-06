package com.library.LibraryController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.LibraryDTO.MemberDTO;
import com.library.LibraryDTO.MemberRequest;
import com.library.LibraryCustomExceptions.ResourceNotFoundException;
import com.library.LibraryServiceInterface.MemberService;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addMember_shouldReturnCreatedMember() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        MemberDTO dto = new MemberDTO(1L, "John Doe", "john@example.com", now);
        when(memberService.addMember(any(MemberRequest.class))).thenReturn(dto);

        MemberRequest request = new MemberRequest("John Doe", "john@example.com");

        mockMvc.perform(post("/api/v1/members/addMember")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("000001"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getAllMembers_shouldReturnListOfMembers() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        MemberDTO dto1 = new MemberDTO(1L, "John Doe", "john@example.com", now);
        MemberDTO dto2 = new MemberDTO(2L, "Jane Smith", "jane@example.com", now);
        when(memberService.getAllMembers()).thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/api/v1/members/getAllMembers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));
    }

    @Test
    void getAllMembers_shouldReturnEmptyList() throws Exception {
        when(memberService.getAllMembers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/members/getAllMembers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMember_shouldReturnMemberById() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        MemberDTO dto = new MemberDTO(1L, "John Doe", "john@example.com", now);
        when(memberService.getMember(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/members/getMember/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("000001"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getMember_shouldReturn404_whenMemberNotFound() throws Exception {
        when(memberService.getMember(99L)).thenThrow(new ResourceNotFoundException("Member with id 99 not found."));

        mockMvc.perform(get("/api/v1/members/getMember/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Member with id 99 not found."));
    }
}
