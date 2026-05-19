package com.ssafy.ssabre.member.repository;

import com.ssafy.ssabre.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 중복 가입 방지용
    boolean existsByEmail(String email);

    java.util.Optional<Member> findByEmail(String email);

    java.util.Optional<Member> findByEmailAndDeletedAtIsNull(String email);

    java.util.Optional<Member> findByMattermostId(String mattermostId);

    java.util.Optional<Member> findByMattermostIdAndDeletedAtIsNull(String mattermostId);

    boolean existsByMattermostId(String mattermostId);

    boolean existsByStudentNo(Integer studentNo);

    // 활동 중인 회원 중에서만 학번 중복 검사
    boolean existsByStudentNoAndDeletedAtIsNull(Integer studentNo);
}