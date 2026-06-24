package com.yuhyeon.devwebide.runtime.domain;

/**
 * 런타임 언어 타입
 *
 * 프로젝트 생성 시 선택 가능한 실행 언어를 나타냅니다.
 */
public enum RuntimeLanguage {
    /**
     * Node.js 런타임
     */
    NODE,

    /**
     * Python 런타임
     */
    PYTHON,

    /**
     * Java 런타임
     */
    JAVA,

    /**
     * C++ 런타임
     */
    CPP
}