package com.yuhyeon.devwebide.runtime.repository;

import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.domain.RuntimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RuntimeRepository extends JpaRepository<Runtime, Long> {

    /**
     * 런타임 내부 이름으로 조회
     *
     * 예: node-20, python-3.12, java-21
     */
    Optional<Runtime> findByName(String name);

    /**
     * 런타임 내부 이름 존재 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 런타임 상태로 목록 조회
     *
     * ACTIVE 상태인 런타임만 프로젝트 생성 화면에 노출할 때 사용
     */
    List<Runtime> findByStatus(RuntimeStatus status);

    /**
     * 언어 타입으로 런타임 목록 조회
     *
     * 예: NODE, PYTHON, JAVA, CPP
     */
    List<Runtime> findByLanguage(RuntimeLanguage language);

    /**
     * 언어 타입과 상태로 런타임 목록 조회
     *
     * 예: 활성화된 Node.js 런타임만 조회
     */
    List<Runtime> findByLanguageAndStatus(
            RuntimeLanguage language,
            RuntimeStatus status
    );
}