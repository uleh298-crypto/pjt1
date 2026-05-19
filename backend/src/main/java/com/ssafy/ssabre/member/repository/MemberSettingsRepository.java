package com.ssafy.ssabre.member.repository;

import com.ssafy.ssabre.member.entity.MemberSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberSettingsRepository extends JpaRepository<MemberSettings, Long> {
    void deleteByMemberId(Long memberId);
}
