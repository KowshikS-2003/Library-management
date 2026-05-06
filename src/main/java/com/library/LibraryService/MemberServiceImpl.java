package com.library.LibraryService;

import com.library.LibraryAppConstants.AppConstants;
import com.library.LibraryCustomExceptions.ResourceNotFoundException;
import com.library.LibraryDTO.MemberDTO;
import com.library.LibraryDTO.MemberRequest;
import com.library.LibraryEntity.LibraryMmember.Member;
import com.library.LibraryRepository.MemberRepo.MemberRepository;
import com.library.LibraryServiceInterface.MemberService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberServiceImpl implements MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @CacheEvict(value = "allMembers", allEntries = true)
    public MemberDTO addMember(MemberRequest request) {
        logger.info(AppConstants.LOG_ADDING_MEMBER, request.getName(), request.getEmail());
        Member member = new Member(request.getName(), request.getEmail());
        Member saved = memberRepository.save(member);
        logger.info(AppConstants.LOG_MEMBER_ADDED, saved.getId());
        return toDTO(saved);
    }

    @Override
    public Member getMemberEntity(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error(AppConstants.LOG_MEMBER_NOT_FOUND, id);
                    return new ResourceNotFoundException(String.format(AppConstants.MEMBER_NOT_FOUND, id));
                });
    }

    @Override
    @Cacheable(value = "members", key = "#id")
    public MemberDTO getMember(Long id) {
        logger.info(AppConstants.LOG_FETCHING_MEMBER, id);
        return toDTO(getMemberEntity(id));
    }

    @Override
    @Cacheable("allMembers")
    public List<MemberDTO> getAllMembers() {
        logger.info(AppConstants.LOG_FETCHING_ALL_MEMBERS);
        return memberRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public MemberDTO toDTO(Member member) {
        return new MemberDTO(member.getId(), member.getName(),
                member.getEmail(), member.getCreatedAt());
    }
}
