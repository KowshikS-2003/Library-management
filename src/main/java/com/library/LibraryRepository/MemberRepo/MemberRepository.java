package com.library.LibraryRepository.MemberRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.LibraryEntity.LibraryMmember.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
