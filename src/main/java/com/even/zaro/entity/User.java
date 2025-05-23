package com.even.zaro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "`user`",
    uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"})
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "birth_date")
    private LocalDate birthday;

    @Column(name = "live_alone_date")
    private LocalDate liveAloneDate;

    @Column(name = "gender")
    private String gender;

    @Column(name = "mbti",  length = 4)
    private String mbti;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "follower_count")
    @Builder.Default
    private int followerCount = 0;

    @Column(name = "following_count")
    @Builder.Default
    private int followingCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    public void updateLastLoginAt(LocalDateTime time) {
        this.lastLoginAt = time;
    }

    public void changeStatusToActive() {
        this.status = Status.ACTIVE;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}