package com.ssafy.ssabre.study.repository;

import com.ssafy.ssabre.global.entity.GroupStatus;
import com.ssafy.ssabre.study.entity.Study;
import com.ssafy.ssabre.study.entity.StudyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {

    /**
     * ID로 스터디 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT s FROM Study s LEFT JOIN FETCH s.leader LEFT JOIN FETCH s.campus WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Study> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * ID로 스터디 조회 with leader fetch join
     */
    @Query("SELECT s FROM Study s LEFT JOIN FETCH s.leader LEFT JOIN FETCH s.campus WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Study> findByIdWithLeader(@Param("id") Long id);

    /**
     * 캠퍼스별 가장 최근 모집중인 스터디 조회
     */
    @Query("""
            SELECT s FROM Study s
            LEFT JOIN FETCH s.leader
            LEFT JOIN FETCH s.campus
            WHERE s.status = :status
            AND s.deletedAt IS NULL
            AND EXISTS (
                SELECT 1 FROM Enrollment e
                WHERE e.member.id = s.leader.id
                AND e.deletedAt IS NULL
                AND e.classes.campus.id = :campusId
            )
            ORDER BY s.createdAt DESC
            LIMIT 1
            """)
    Optional<Study> findLatestOpenStudyByCampusId(
            @Param("campusId") Long campusId,
            @Param("status") GroupStatus status);

    default Optional<Study> findLatestOpenStudyByCampusId(Long campusId) {
        return findLatestOpenStudyByCampusId(campusId, GroupStatus.OPEN);
    }

    /**
     * 내가 속한 스터디 조회 (StudyMember 테이블 기준)
     */
    @Query("""
            SELECT DISTINCT s FROM Study s
            LEFT JOIN FETCH s.leader
            LEFT JOIN FETCH s.campus
            JOIN StudyMember sm ON s.id = sm.study.id
            WHERE sm.member.id = :memberId
            AND s.deletedAt IS NULL
            AND sm.deletedAt IS NULL
            AND sm.status != 'QUIT'
            """)
    List<Study> findMyStudies(@Param("memberId") Long memberId);

    /**
     * 모든 스터디 조회 (삭제되지 않은 것만, leader, campus fetch join)
     */
    @Query("SELECT s FROM Study s LEFT JOIN FETCH s.leader LEFT JOIN FETCH s.campus WHERE s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    List<Study> findAllNotDeleted();

    /**
     * 캠퍼스별 스터디 조회
     */
    @Query("SELECT s FROM Study s LEFT JOIN FETCH s.leader LEFT JOIN FETCH s.campus WHERE s.campus.id = :campusId AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    List<Study> findByCampusId(@Param("campusId") Long campusId);

    /**
     * 타입별 스터디 조회
     */
    @Query("SELECT s FROM Study s LEFT JOIN FETCH s.leader LEFT JOIN FETCH s.campus WHERE s.type = :type AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    List<Study> findByType(@Param("type") StudyType type);

    /**
     * 캠퍼스 및 타입별 스터디 조회
     */
    @Query("SELECT s FROM Study s LEFT JOIN FETCH s.leader LEFT JOIN FETCH s.campus WHERE s.campus.id = :campusId AND s.type = :type AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    List<Study> findByCampusIdAndType(@Param("campusId") Long campusId, @Param("type") StudyType type);

    /**
     * 상태별 스터디 조회
     */
    @Query("SELECT s FROM Study s LEFT JOIN FETCH s.leader LEFT JOIN FETCH s.campus WHERE s.status = :status AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    List<Study> findByStatus(@Param("status") GroupStatus status);

    List<Study> findByLeaderId(Long leaderId);
}
