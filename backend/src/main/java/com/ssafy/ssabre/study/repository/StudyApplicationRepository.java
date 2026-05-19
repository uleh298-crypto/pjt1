package com.ssafy.ssabre.study.repository;

import com.ssafy.ssabre.global.entity.ApplicationStatus;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.study.entity.Study;
import com.ssafy.ssabre.study.entity.StudyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {

    /**
     * 스터디별 지원 목록 조회 (study, portfolio와 member fetch join)
     */
    @Query("SELECT sa FROM StudyApplication sa LEFT JOIN FETCH sa.study s LEFT JOIN FETCH s.leader LEFT JOIN FETCH s.campus LEFT JOIN FETCH sa.portfolio p LEFT JOIN FETCH p.member WHERE sa.study.id = :studyId")
    List<StudyApplication> findByStudyId(@Param("studyId") Long studyId);

    /**
     * 스터디별 대기중인 지원 목록 조회
     */
    @Query("SELECT sa FROM StudyApplication sa LEFT JOIN FETCH sa.study s LEFT JOIN FETCH s.leader LEFT JOIN FETCH s.campus LEFT JOIN FETCH sa.portfolio p LEFT JOIN FETCH p.member WHERE sa.study.id = :studyId AND sa.status = :status")
    List<StudyApplication> findByStudyIdAndStatus(
            @Param("studyId") Long studyId,
            @Param("status") ApplicationStatus status);

    default List<StudyApplication> findPendingByStudyId(Long studyId) {
        return findByStudyIdAndStatus(studyId, ApplicationStatus.PENDING);
    }

    /**
     * 지원 ID로 조회 (study, portfolio, member fetch join)
     */
    @Query("SELECT sa FROM StudyApplication sa LEFT JOIN FETCH sa.study s LEFT JOIN FETCH s.leader LEFT JOIN FETCH s.campus LEFT JOIN FETCH sa.portfolio p LEFT JOIN FETCH p.member WHERE sa.id = :id")
    Optional<StudyApplication> findByIdWithDetails(@Param("id") Long id);

    /**
     * 특정 멤버가 특정 스터디에 이미 지원했는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM StudyApplication sa WHERE sa.study = :study AND sa.portfolio.member = :member AND sa.status = :status")
    boolean existsPendingApplication(
            @Param("study") Study study,
            @Param("member") Member member,
            @Param("status") ApplicationStatus status);

    default boolean existsPendingApplication(Study study, Member member) {
        return existsPendingApplication(study, member, ApplicationStatus.PENDING);
    }

    /**
     * 특정 멤버의 모든 지원 목록 조회
     */
    @Query("SELECT sa FROM StudyApplication sa LEFT JOIN FETCH sa.study s LEFT JOIN FETCH s.leader LEFT JOIN FETCH s.campus LEFT JOIN FETCH sa.portfolio WHERE sa.portfolio.member.id = :memberId ORDER BY sa.createdAt DESC")
    List<StudyApplication> findByMemberId(@Param("memberId") Long memberId);

    void deleteByPortfolioId(Long portfolioId);
}
