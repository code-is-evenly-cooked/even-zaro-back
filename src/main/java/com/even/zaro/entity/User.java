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
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "users",
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

    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "mbti",  length = 10)
    private Mbti mbti;

    @Column(name = "status", nullable = false, length = 10)
    private Status status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "follower_count")
    @Builder.Default
    private int followerCount = 0;

    @Column(name = "following_count")
    @Builder.Default
    private int followingCount = 0;

    @Column(name = "provider", nullable = false, length = 10)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "last_nickname_updated_at")
    private LocalDateTime lastNicknameUpdatedAt;

    public void updateLastLoginAt(LocalDateTime time) {
        this.lastLoginAt = time;
    }

    public void updateStatusToActive() {
        this.status = Status.ACTIVE;
    }

    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void updateLastNicknameUpdatedAt(LocalDateTime time) {
        this.lastNicknameUpdatedAt = time;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updateBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public void updateLiveAloneDate(LocalDate liveAloneDate) {
        this.liveAloneDate = liveAloneDate;
    }

    public void updateGender(Gender gender) {
        this.gender = gender;
    }

    public void updateMbti(Mbti mbti) {
        this.mbti = mbti;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public boolean isValidated() {
        return this.provider == Provider.KAKAO || this.status != Status.PENDING;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }

    public void softDeleted() {
         this.status = Status.DELETED;
         this.deletedAt = LocalDateTime.now();
    }

    public void anonymize() {
        this.nickname = "삭제된 사용자";
        this.profileImage = null;
        this.email = "deleted_" + UUID.randomUUID() + "@anonymized.even.com";
    }
}