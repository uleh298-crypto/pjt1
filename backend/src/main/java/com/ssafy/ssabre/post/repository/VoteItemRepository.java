package com.ssafy.ssabre.post.repository;

import com.ssafy.ssabre.post.entity.VoteItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteItemRepository extends JpaRepository<VoteItem, Long> {
    List<VoteItem> findByVoteIdAndDeletedAtIsNullOrderByItemOrderAsc(Long voteId);
    java.util.Optional<VoteItem> findByIdAndDeletedAtIsNull(Long id);
}
