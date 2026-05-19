package com.ssafy.ssabre.inquiry.repository;

import com.ssafy.ssabre.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long memberId);

    void deleteByMemberId(Long memberId);
}
