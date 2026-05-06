package com.library.LibraryController;

import com.library.LibraryDTO.MemberDTO;
import com.library.LibraryDTO.MemberRequest;
import com.library.LibraryServiceInterface.MemberService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@Validated
public class MemberController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/addMember")
    public ResponseEntity<MemberDTO> addMember(@Valid @RequestBody MemberRequest request) {
        logger.info("POST /api/v1/members/addMember - Adding new member");
        MemberDTO member = memberService.addMember(request);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/getAllMembers")
    public ResponseEntity<List<MemberDTO>> getAllMembers() {
        logger.info("GET /api/v1/members/getAllMembers - Fetching all members");
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/getMember/{id}")
    public ResponseEntity<MemberDTO> getMember(@PathVariable("id") @Positive(message = "Member ID must be a positive number") Long id) {
        logger.info("GET /api/v1/members/getMember/{} - Fetching member", id);
        return ResponseEntity.ok(memberService.getMember(id));
    }
}
