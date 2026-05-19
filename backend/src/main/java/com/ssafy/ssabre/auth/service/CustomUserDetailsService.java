package com.ssafy.ssabre.auth.service;

import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByEmailAndDeletedAtIsNull(username)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다."));
    }

    // 해당하는 User 의 데이터가 존재한다면 UserDetails 객체로 만들어서 리턴
    private UserDetails createUserDetails(Member member) {
        // 비밀번호는 이미 암호화 되어있다고 가정
        // 권한은 ROLE_USER 로 하드코딩 (나중에 Role Enum 추가 가능)
        return User.builder()
                .username(member.getEmail()) // Principal이 될 값 (이메일 or ID) -> 这里用 email
                .password(member.getPassword())
                .roles("USER")
                .build();
    }
}
