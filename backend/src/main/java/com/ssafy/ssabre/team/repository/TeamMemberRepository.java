package com.ssafy.ssabre.team.repository;

import com.ssafy.ssabre.global.entity.GroupMemberStatus;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.team.entity.Team;
import com.ssafy.ssabre.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    /**
     * 특정 멤버의 활성 팀 멤버십 조회 (탈퇴 제외, 삭제된 팀 제외)
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.member.id = :memberId AND tm.deletedAt IS NULL AND tm.team.deletedAt IS NULL AND tm.status != :excludeStatus")
    List<TeamMember> findByMemberIdAndDeletedAtIsNullAndStatusNot(
            @Param("memberId") Long memberId,
            @Param("excludeStatus") GroupMemberStatus excludeStatus);

    default List<TeamMember> findActiveMembershipsByMemberId(Long memberId) {
        return findByMemberIdAndDeletedAtIsNullAndStatusNot(memberId, GroupMemberStatus.QUIT);
    }

    /**
     * 팀과 멤버로 멤버십 조회
     */
    Optional<TeamMember> findByTeamAndMember(Team team, Member member);

    /**
     * 팀과 멤버로 활성 멤버십 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(tm) > 0 THEN true ELSE false END FROM TeamMember tm WHERE tm.team = :team AND tm.member = :member AND tm.status = :status AND tm.deletedAt IS NULL")
    boolean existsByTeamAndMemberAndActive(
            @Param("team") Team team,
            @Param("member") Member member,
            @Param("status") GroupMemberStatus status);

    default boolean existsActiveMember(Team team, Member member) {
        return existsByTeamAndMemberAndActive(team, member, GroupMemberStatus.ACTIVE);
    }

    /**
     * 팀의 활성 멤버 수 조회
     */
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team = :team AND tm.status = :status AND tm.deletedAt IS NULL")
    long countActiveMembers(@Param("team") Team team, @Param("status") GroupMemberStatus status);

    default long countActiveMembers(Team team) {
        return countActiveMembers(team, GroupMemberStatus.ACTIVE);
    }

    /**
     * 팀의 모든 활성 멤버 조회
     */
    @Query("SELECT tm FROM TeamMember tm LEFT JOIN FETCH tm.member WHERE tm.team = :team AND tm.status = :status AND tm.deletedAt IS NULL")
    List<TeamMember> findActiveMembers(@Param("team") Team team, @Param("status") GroupMemberStatus status);

    default List<TeamMember> findActiveMembers(Team team) {
        return findActiveMembers(team, GroupMemberStatus.ACTIVE);
    }

    /**
     * 팀 ID로 활성 멤버 조회 (member fetch join)
     */
    @Query("SELECT tm FROM TeamMember tm LEFT JOIN FETCH tm.member LEFT JOIN FETCH tm.team WHERE tm.team.id = :teamId AND tm.status = :status AND tm.deletedAt IS NULL")
    List<TeamMember> findActiveMembersByTeamId(@Param("teamId") Long teamId, @Param("status") GroupMemberStatus status);

    default List<TeamMember> findActiveMembersByTeamId(Long teamId) {
        return findActiveMembersByTeamId(teamId, GroupMemberStatus.ACTIVE);
    }

    void deleteByMemberId(Long memberId);
}
