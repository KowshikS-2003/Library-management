package com.library.LibraryServiceInterface;

import com.library.LibraryDTO.MemberDTO;
import com.library.LibraryDTO.MemberRequest;
import com.library.LibraryEntity.LibraryMmember.Member;

import java.util.List;

/**
 * Service contract for Member operations. Controllers depend on this interface;
 * concrete behavior is provided by {@code MemberServiceImpl}.
 */
public interface MemberService {

    MemberDTO addMember(MemberRequest request);

    Member getMemberEntity(Long id);

    MemberDTO getMember(Long id);

    List<MemberDTO> getAllMembers();

    MemberDTO toDTO(Member member);
}
