package com.ssafy.ssabre.inquiry.service;

import com.ssafy.ssabre.inquiry.dto.InquiryCreateRequest;
import com.ssafy.ssabre.inquiry.dto.InquiryListResponse;
import com.ssafy.ssabre.inquiry.dto.InquiryResponse;
import com.ssafy.ssabre.inquiry.entity.Inquiry;
import com.ssafy.ssabre.inquiry.repository.InquiryRepository;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createInquiry(InquiryCreateRequest request, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Inquiry inquiry = Inquiry.builder()
                .member(member)
                .content(request.content())
                .build();

        inquiryRepository.save(inquiry);
    }

    public InquiryListResponse getInquiries(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<InquiryResponse> items = inquiryRepository
                .findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(InquiryResponse::from)
                .toList();

        return InquiryListResponse.of(items);
    }
}
