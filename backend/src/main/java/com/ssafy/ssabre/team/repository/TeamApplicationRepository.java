package com.ssafy.ssabre.team.repository;

import com.ssafy.ssabre.global.entity.ApplicationStatus;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.team.entity.Team;
import com.ssafy.ssabre.team.entity.TeamApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {

    /**
     * 팀별 지원 목록 조회 (team, portfolio와 member fetch join)
     */
    @Query("SELECT ta FROM TeamApplication ta LEFT JOIN FETCH ta.team t LEFT JOIN FETCH t.leader LEFT JOIN FETCH t.campus LEFT JOIN FETCH ta.portfolio p LEFT JOIN FETCH p.member WHERE ta.team.id = :teamId")
    List<TeamApplication> findByTeamId(@Param("teamId") Long teamId);

    /**
     * 팀별 대기중인 지원 목록 조회
     */
    @Query("SELECT ta FROM TeamApplication ta LEFT JOIN FETCH ta.team t LEFT JOIN FETCH t.leader LEFT JOIN FETCH t.campus LEFT JOIN FETCH ta.portfolio p LEFT JOIN FETCH p.member WHERE ta.team.id = :teamId AND ta.status = :status")
    List<TeamApplication> findByTeamIdAndStatus(
            @Param("teamId") Long teamId,
            @Param("status") ApplicationStatus status);

    default List<TeamApplication> findPendingByTeamId(Long teamId) {
        return findByTeamIdAndStatus(teamId, ApplicationStatus.PENDING);
    }

    /**
     * 지원 ID로 조회 (team, portfolio, member fetch join)
     */
    @Query("SELECT ta FROM TeamApplication ta LEFT JOIN FETCH ta.team t LEFT JOIN FETCH t.leader LEFT JOIN FETCH t.campus LEFT JOIN FETCH ta.portfolio p LEFT JOIN FETCH p.member WHERE ta.id = :id")
    Optional<TeamApplication> findByIdWithDetails(@Param("id") Long id);

    /**
     * 특정 멤버가 특정 팀에 이미 지원했는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(ta) > 0 THEN true ELSE false END FROM TeamApplication ta WHERE ta.team = :team AND ta.portfolio.member = :member AND ta.status = :status")
    boolean existsPendingApplication(
            @Param("team") Team team,
            @Param("member") Member member,
            @Param("status") ApplicationStatus status);

    default boolean existsPendingApplication(Team team, Member member) {
        return existsPendingApplication(team, member, ApplicationStatus.PENDING);
    }

    /**
     * 특정 멤버의 모든 지원 목록 조회
     */
    @Query("SELECT ta FROM TeamApplication ta LEFT JOIN FETCH ta.team t LEFT JOIN FETCH t.leader LEFT JOIN FETCH t.campus LEFT JOIN FETCH ta.portfolio WHERE ta.portfolio.member.id = :memberId ORDER BY ta.createdAt DESC")
    List<TeamApplication> findByMemberId(@Param("memberId") Long memberId);

    void deleteByPortfolioId(Long portfolioId);
}
