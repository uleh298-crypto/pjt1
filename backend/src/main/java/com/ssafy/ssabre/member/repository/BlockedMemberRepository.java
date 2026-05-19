package com.ssafy.ssabre.member.repository;

import com.ssafy.ssabre.member.entity.BlockedMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedMemberRepository extends JpaRepository<BlockedMember, Long> {
    void deleteByBlockerIdOrBlockedId(Long blockerId, Long blockedId);
}
