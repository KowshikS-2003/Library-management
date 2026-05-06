package com.library.LibraryEntity;

import org.junit.jupiter.api.Test;

import com.library.LibraryEntity.LibraryMmember.Member;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @Test
    void defaultConstructor_shouldCreateEmptyMember() {
        Member member = new Member();
        assertNull(member.getId());
        assertNull(member.getName());
        assertNull(member.getEmail());
    }

    @Test
    void parameterizedConstructor_shouldSetFields() {
        Member member = new Member("John Doe", "john@example.com");

        assertEquals("John Doe", member.getName());
        assertEquals("john@example.com", member.getEmail());
        assertNotNull(member.getCreatedAt());
    }

    @Test
    void setId_shouldUpdateId() {
        Member member = new Member();
        member.setId(5L);
        assertEquals(5L, member.getId());
    }

    @Test
    void setName_shouldUpdateName() {
        Member member = new Member();
        member.setName("Jane");
        assertEquals("Jane", member.getName());
    }

    @Test
    void setEmail_shouldUpdateEmail() {
        Member member = new Member();
        member.setEmail("jane@example.com");
        assertEquals("jane@example.com", member.getEmail());
    }

    @Test
    void setCreatedAt_shouldUpdateCreatedAt() {
        Member member = new Member();
        LocalDateTime time = LocalDateTime.of(2025, 6, 15, 10, 30);
        member.setCreatedAt(time);
        assertEquals(time, member.getCreatedAt());
    }

    @Test
    void toString_shouldContainAllFields() {
        Member member = new Member("John Doe", "john@example.com");
        member.setId(1L);

        String result = member.toString();

        assertTrue(result.contains("1"));
        assertTrue(result.contains("John Doe"));
        assertTrue(result.contains("john@example.com"));
    }
}
