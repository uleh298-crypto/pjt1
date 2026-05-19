package com.ssafy.ssabre.study.repository;

import com.ssafy.ssabre.global.entity.GroupMemberStatus;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.study.entity.Study;
import com.ssafy.ssabre.study.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    /**
     * 특정 멤버의 활성 스터디 멤버십 조회 (탈퇴 제외, 삭제된 스터디 제외)
     */
    @Query("SELECT sm FROM StudyMember sm WHERE sm.member.id = :memberId AND sm.deletedAt IS NULL AND sm.study.deletedAt IS NULL AND sm.status != :excludeStatus")
    List<StudyMember> findByMemberIdAndDeletedAtIsNullAndStatusNot(
            @Param("memberId") Long memberId,
            @Param("excludeStatus") GroupMemberStatus excludeStatus);

    default List<StudyMember> findActiveMembershipsByMemberId(Long memberId) {
        return findByMemberIdAndDeletedAtIsNullAndStatusNot(memberId, GroupMemberStatus.QUIT);
    }

    /**
     * 스터디와 멤버로 멤버십 조회
     */
    Optional<StudyMember> findByStudyAndMember(Study study, Member member);

    /**
     * 스터디와 멤버로 활성 멤버십 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(sm) > 0 THEN true ELSE false END FROM StudyMember sm WHERE sm.study = :study AND sm.member = :member AND sm.status = :status AND sm.deletedAt IS NULL")
    boolean existsByStudyAndMemberAndActive(
            @Param("study") Study study,
            @Param("member") Member member,
            @Param("status") GroupMemberStatus status);

    default boolean existsActiveMember(Study study, Member member) {
        return existsByStudyAndMemberAndActive(study, member, GroupMemberStatus.ACTIVE);
    }

    /**
     * 스터디의 활성 멤버 수 조회
     */
    @Query("SELECT COUNT(sm) FROM StudyMember sm WHERE sm.study = :study AND sm.status = :status AND sm.deletedAt IS NULL")
    long countActiveMembers(@Param("study") Study study, @Param("status") GroupMemberStatus status);

    default long countActiveMembers(Study study) {
        return countActiveMembers(study, GroupMemberStatus.ACTIVE);
    }

    /**
     * 스터디의 모든 활성 멤버 조회
     */
    @Query("SELECT sm FROM StudyMember sm LEFT JOIN FETCH sm.member WHERE sm.study = :study AND sm.status = :status AND sm.deletedAt IS NULL")
    List<StudyMember> findActiveMembers(@Param("study") Study study, @Param("status") GroupMemberStatus status);

    default List<StudyMember> findActiveMembers(Study study) {
        return findActiveMembers(study, GroupMemberStatus.ACTIVE);
    }

    /**
     * 스터디 ID로 활성 멤버 조회 (member fetch join)
     */
    @Query("SELECT sm FROM StudyMember sm LEFT JOIN FETCH sm.member LEFT JOIN FETCH sm.study WHERE sm.study.id = :studyId AND sm.status = :status AND sm.deletedAt IS NULL")
    List<StudyMember> findActiveMembersByStudyId(@Param("studyId") Long studyId, @Param("status") GroupMemberStatus status);

    default List<StudyMember> findActiveMembersByStudyId(Long studyId) {
        return findActiveMembersByStudyId(studyId, GroupMemberStatus.ACTIVE);
    }

    void deleteByMemberId(Long memberId);
}
