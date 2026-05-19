package com.ssafy.ssabre.campus.repository;

import com.ssafy.ssabre.campus.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.member LEFT JOIN FETCH e.classes c LEFT JOIN FETCH c.campus WHERE e.member.id = :memberId AND e.deletedAt IS NULL")
    java.util.Optional<Enrollment> findByMember_IdAndDeletedAtIsNull(@Param("memberId") Long memberId);

    void deleteByMemberId(Long memberId);
}
