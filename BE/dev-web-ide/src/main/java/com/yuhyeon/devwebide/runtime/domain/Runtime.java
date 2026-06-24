package com.yuhyeon.devwebide.runtime.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 런타임 엔티티
 *
 * 프로젝트 생성 시 선택 가능한 실행 환경을 관리합니다.
 * Node.js, Python, Java, C++ 등의 런타임 목록을 저장합니다.
 */
@Entity
@Table(name = "runtimes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Runtime {

    /**
     * 런타임 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 런타임 내부 이름
     *
     * 예: node-20, python-3.12, java-21
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 화면 표시 이름
     *
     * 예: Node.js 20, Python 3.12, Java 21
     */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /**
     * 런타임 버전
     *
     * 예: 20, 3.12, 21
     */
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    /**
     * 실행에 사용할 Docker 이미지
     *
     * 예: node:20, python:3.12, eclipse-temurin:21
     */
    @Column(name = "docker_image", nullable = false, length = 200)
    private String dockerImage;

    /**
     * 런타임 언어 타입
     *
     * NODE, PYTHON, JAVA, CPP
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 30)
    private RuntimeLanguage language;

    /**
     * 런타임 상태
     *
     * ACTIVE 상태인 런타임만 프로젝트 생성 화면에 노출합니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RuntimeStatus status;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Runtime 생성자
     *
     * @param name 런타임 내부 이름
     * @param displayName 화면 표시 이름
     * @param version 런타임 버전
     * @param dockerImage Docker 이미지
     * @param language 런타임 언어 타입
     * @param status 런타임 상태
     */
    @Builder
    public Runtime(
            String name,
            String displayName,
            String version,
            String dockerImage,
            RuntimeLanguage language,
            RuntimeStatus status
    ) {
        this.name = name;
        this.displayName = displayName;
        this.version = version;
        this.dockerImage = dockerImage;
        this.language = language;
        this.status = status;
    }

    /**
     * 런타임 활성 여부 확인
     *
     * @return 활성 여부
     */
    public boolean isActive() {
        return this.status == RuntimeStatus.ACTIVE;
    }

    /**
     * 런타임 비활성 처리
     */
    public void deactivate() {
        this.status = RuntimeStatus.INACTIVE;
    }

    /**
     * 런타임 활성 처리
     */
    public void activate() {
        this.status = RuntimeStatus.ACTIVE;
    }
}