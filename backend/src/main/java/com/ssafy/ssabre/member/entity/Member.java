package com.ssafy.ssabre.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ssafy.ssabre.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "members")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    private Integer studentNo;

    @Column(length = 100)
    private String mattermostId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(length = 255)
    private String profileImageUrl;

    @Builder
    public Member(String email, String password, String name, Integer studentNo, String mattermostId,
            String profileImageUrl) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.studentNo = studentNo;
        this.mattermostId = mattermostId;
        this.profileImageUrl = profileImageUrl;
    }

    public void update(String profileImageUrl) {
        if (profileImageUrl != null)
            this.profileImageUrl = profileImageUrl;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void reactivate(String encodedPassword, String name, Integer studentNo, String mattermostId,
            String profileImageUrl) {
        this.password = encodedPassword;
        this.name = name;
        this.studentNo = studentNo;
        this.mattermostId = mattermostId;
        this.profileImageUrl = profileImageUrl;
        this.deletedAt = null;
    }
}