package com.library.LibraryService;

import com.library.LibraryCustomExceptions.ResourceNotFoundException;
import com.library.LibraryDTO.MemberDTO;
import com.library.LibraryDTO.MemberRequest;
import com.library.LibraryEntity.LibraryMmember.Member;
import com.library.LibraryRepository.MemberRepo.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member("John Doe", "john@example.com");
        member.setId(1L);
    }

    @Test
    void addMember_shouldSaveAndReturnMemberDTO() {
        MemberRequest request = new MemberRequest("John Doe", "john@example.com");
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        MemberDTO result = memberService.addMember(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void getMember_shouldReturnMemberDTO_whenMemberExists() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        MemberDTO result = memberService.getMember(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void getMember_shouldThrowResourceNotFoundException_whenMemberNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberService.getMember(99L));
    }

    @Test
    void getMemberEntity_shouldReturnMember_whenExists() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        Member result = memberService.getMemberEntity(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getMemberEntity_shouldThrowResourceNotFoundException_whenNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberService.getMemberEntity(99L));
    }

    @Test
    void getAllMembers_shouldReturnListOfMemberDTOs() {
        Member member2 = new Member("Jane Smith", "jane@example.com");
        member2.setId(2L);
        when(memberRepository.findAll()).thenReturn(Arrays.asList(member, member2));

        List<MemberDTO> result = memberService.getAllMembers();

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
    }

    @Test
    void getAllMembers_shouldReturnEmptyList_whenNoMembers() {
        when(memberRepository.findAll()).thenReturn(Collections.emptyList());

        List<MemberDTO> result = memberService.getAllMembers();

        assertTrue(result.isEmpty());
    }

    @Test
    void toDTO_shouldMapAllFields() {
        MemberDTO dto = memberService.toDTO(member);

        assertEquals(member.getId(), dto.getId());
        assertEquals(member.getName(), dto.getName());
        assertEquals(member.getEmail(), dto.getEmail());
        assertEquals(member.getCreatedAt(), dto.getCreatedAt());
    }
}
