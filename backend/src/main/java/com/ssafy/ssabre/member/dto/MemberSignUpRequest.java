package com.ssafy.ssabre.member.dto;

import com.ssafy.ssabre.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberSignUpRequest(
                @Schema(description = "이메일", example = "user@example.com") @NotBlank(message = "이메일은 필수입니다.") @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "이메일 형식이 올바르지 않습니다.") String email,

                @Schema(description = "비밀번호 (영문, 숫자, 특수문자 포함)", example = "Example123!") @NotBlank(message = "비밀번호는 필수입니다.") @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.") String password,

                @Schema(description = "이름", example = "홍길동") @NotBlank(message = "이름은 필수입니다.") String name,

                @Schema(description = "학번", example = "1234567") Integer studentNo,

                @Schema(description = "캠퍼스 ID", example = "1") Long campus,

                @Schema(description = "기수", example = "14") Integer generation,

                @Schema(description = "1학기 반", example = "1") Integer classNo,

                @Schema(description = "Mattermost ID", example = "mattermost_user_id") @NotBlank(message = "Mattermost ID는 필수입니다.") String mattermostId) {
        public Member toEntity(String encodedPassword) {
                return Member.builder()
                                .email(email)
                                .password(encodedPassword)
                                .name(name)
                                .studentNo(studentNo)
                                .mattermostId(mattermostId)
                                .build();
        }
}
