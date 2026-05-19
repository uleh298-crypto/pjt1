package com.ssafy.ssabre.member.repository;

import com.ssafy.ssabre.member.entity.DDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DDayRepository extends JpaRepository<DDay, Long> {
    List<DDay> findByMemberIdAndDeletedAtIsNullOrderByTargetDateAsc(Long memberId);

    void deleteByMemberId(Long memberId);
}
