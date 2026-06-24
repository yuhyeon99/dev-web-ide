package com.yuhyeon.devwebide.project.domain;

/**
 * 프로젝트 멤버 권한
 *
 * OWNER: 프로젝트 소유자
 * MAINTAINER: 프로젝트 설정/멤버 관리 가능
 * EDITOR: 파일 수정 가능
 * VIEWER: 읽기 전용
 */
public enum ProjectMemberRole {
    OWNER,
    MAINTAINER,
    EDITOR,
    VIEWER
}