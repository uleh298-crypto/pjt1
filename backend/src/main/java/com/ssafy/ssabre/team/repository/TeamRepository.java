package com.ssafy.ssabre.team.repository;

import com.ssafy.ssabre.global.entity.GroupStatus;
import com.ssafy.ssabre.team.entity.Team;
import com.ssafy.ssabre.team.entity.TeamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * ID로 팀 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.leader LEFT JOIN FETCH t.campus WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Team> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * ID로 팀 조회 with leader fetch join
     */
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.leader LEFT JOIN FETCH t.campus WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Team> findByIdWithLeader(@Param("id") Long id);

    /**
     * 캠퍼스별 가장 최근 모집중인 팀 조회
     */
    @Query("""
            SELECT t FROM Team t
            LEFT JOIN FETCH t.leader
            LEFT JOIN FETCH t.campus
            WHERE t.status = :status
            AND t.deletedAt IS NULL
            AND EXISTS (
                SELECT 1 FROM Enrollment e
                WHERE e.member.id = t.leader.id
                AND e.deletedAt IS NULL
                AND e.classes.campus.id = :campusId
            )
            ORDER BY t.createdAt DESC
            LIMIT 1
            """)
    Optional<Team> findLatestOpenTeamByCampusId(
            @Param("campusId") Long campusId,
            @Param("status") GroupStatus status);

    default Optional<Team> findLatestOpenTeamByCampusId(Long campusId) {
        return findLatestOpenTeamByCampusId(campusId, GroupStatus.OPEN);
    }

    /**
     * 내가 속한 팀 조회 (TeamMember 테이블 기준)
     */
    @Query("""
            SELECT DISTINCT t FROM Team t
            LEFT JOIN FETCH t.leader
            LEFT JOIN FETCH t.campus
            JOIN TeamMember tm ON t.id = tm.team.id
            WHERE tm.member.id = :memberId
            AND t.deletedAt IS NULL
            AND tm.deletedAt IS NULL
            AND tm.status != 'QUIT'
            """)
    List<Team> findMyTeams(@Param("memberId") Long memberId);

    /**
     * 모든 팀 조회 (삭제되지 않은 것만, leader, campus fetch join)
     */
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.leader LEFT JOIN FETCH t.campus WHERE t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Team> findAllNotDeleted();

    /**
     * 캠퍼스별 팀 조회
     */
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.leader LEFT JOIN FETCH t.campus WHERE t.campus.id = :campusId AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Team> findByCampusId(@Param("campusId") Long campusId);

    /**
     * 타입별 팀 조회
     */
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.leader LEFT JOIN FETCH t.campus WHERE t.type = :type AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Team> findByType(@Param("type") TeamType type);

    /**
     * 캠퍼스 및 타입별 팀 조회
     */
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.leader LEFT JOIN FETCH t.campus WHERE t.campus.id = :campusId AND t.type = :type AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Team> findByCampusIdAndType(@Param("campusId") Long campusId, @Param("type") TeamType type);

    /**
     * 상태별 팀 조회
     */
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.leader LEFT JOIN FETCH t.campus WHERE t.status = :status AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Team> findByStatus(@Param("status") GroupStatus status);

    List<Team> findByLeaderId(Long leaderId);
}
