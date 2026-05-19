package com.ssafy.ssabre.dday.service;

import com.ssafy.ssabre.dday.dto.DDayListResponse;
import com.ssafy.ssabre.dday.dto.DDayResponse;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.DDayRepository;
import com.ssafy.ssabre.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DDayService {

    private final DDayRepository dDayRepository;
    private final MemberRepository memberRepository;

    public DDayListResponse getDDays(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<DDayResponse> items = dDayRepository
                .findByMemberIdAndDeletedAtIsNullOrderByTargetDateAsc(member.getId())
                .stream()
                .map(DDayResponse::from)
                .toList();

        return DDayListResponse.of(items);
    }
}
