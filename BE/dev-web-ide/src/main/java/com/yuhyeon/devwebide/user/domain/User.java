package com.yuhyeon.devwebide.user.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 사용자 정보를 관리하는 엔티티 클래스입니다.
 * 'users' 테이블과 매핑됩니다.
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class) // 생성일, 수정일 자동 관리를 위한 리스너
public class User {

    /** 고유 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이메일 (로그인 ID로 사용, 고유값) */
    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    /** 사용자 닉네임 */
    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    /** 사용자 권한 (USER, ADMIN) */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    /** 계정 상태 (ACTIVE, INACTIVE, DELETED) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    /** 생성 일시 (자동 생성) */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 일시 (자동 업데이트) */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** JPA를 위한 기본 생성자 */
    protected User() {
    }

    /**
     * User 엔티티 생성을 위한 생성자
     * @param email 이메일
     * @param nickname 닉네임
     * @param role 권한
     * @param status 상태
     */
    public User(String email, String nickname, UserRole role, UserStatus status) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * ID를 기준으로 객체의 동일성을 비교합니다.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    /**
     * ID를 기준으로 해시코드를 생성합니다.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
