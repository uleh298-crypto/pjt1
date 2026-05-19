package com.ssafy.ssabre.post.repository;

import com.ssafy.ssabre.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostIdAndDeletedAtIsNull(Long postId);

    void deleteByPostId(Long postId);

    @Query("SELECT pi FROM PostImage pi WHERE pi.post.id IN :postIds AND pi.deletedAt IS NULL")
    List<PostImage> findByPostIdInAndDeletedAtIsNull(@Param("postIds") List<Long> postIds);
}
