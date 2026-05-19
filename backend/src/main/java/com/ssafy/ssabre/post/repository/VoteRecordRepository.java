package com.ssafy.ssabre.post.repository;

import com.ssafy.ssabre.post.entity.VoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRecordRepository extends JpaRepository<VoteRecord, Long> {
    Optional<VoteRecord> findByVoteIdAndMemberIdAndDeletedAtIsNull(Long voteId, Long memberId);
    int countByItemIdAndDeletedAtIsNull(Long itemId);
    int countByVoteIdAndDeletedAtIsNull(Long voteId);
    void deleteByMemberId(Long memberId);
}
