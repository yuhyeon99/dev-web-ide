package com.yuhyeon.devwebide.project.domain;

/**
 * 프로젝트 저장 배치 상태
 *
 * SUCCESS: 모든 파일 저장 성공
 * FAILED: 모든 파일 저장 실패
 * PARTIAL: 일부 파일 저장 성공
 */
public enum ProjectSaveBatchStatus {
    SUCCESS,
    FAILED,
    PARTIAL
}