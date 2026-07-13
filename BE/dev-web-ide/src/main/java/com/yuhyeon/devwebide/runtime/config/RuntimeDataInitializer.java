package com.yuhyeon.devwebide.runtime.config;

import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.domain.RuntimeStatus;
import com.yuhyeon.devwebide.runtime.repository.RuntimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 런타임 기본 데이터 초기화
 *
 * 운영 DB가 비어 있어도 프로젝트 생성 화면에서
 * 선택 가능한 기본 런타임이 노출되도록 서버 시작 시 보강합니다.
 */
@Component
@RequiredArgsConstructor
public class RuntimeDataInitializer implements ApplicationRunner {

    private final RuntimeRepository runtimeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Runtime> defaultRuntimes = List.of(
                Runtime.builder()
                        .name("node-20")
                        .displayName("Node.js 20")
                        .version("20")
                        .dockerImage("node:20")
                        .language(RuntimeLanguage.NODE)
                        .status(RuntimeStatus.ACTIVE)
                        .build(),
                Runtime.builder()
                        .name("python-3.12")
                        .displayName("Python 3.12")
                        .version("3.12")
                        .dockerImage("python:3.12")
                        .language(RuntimeLanguage.PYTHON)
                        .status(RuntimeStatus.ACTIVE)
                        .build(),
                Runtime.builder()
                        .name("java-21")
                        .displayName("Java 21")
                        .version("21")
                        .dockerImage("eclipse-temurin:21")
                        .language(RuntimeLanguage.JAVA)
                        .status(RuntimeStatus.ACTIVE)
                        .build(),
                Runtime.builder()
                        .name("cpp-gcc")
                        .displayName("C++ GCC")
                        .version("latest")
                        .dockerImage("gcc:latest")
                        .language(RuntimeLanguage.CPP)
                        .status(RuntimeStatus.ACTIVE)
                        .build()
        );

        defaultRuntimes.stream()
                .filter(runtime -> !runtimeRepository.existsByName(runtime.getName()))
                .forEach(runtimeRepository::save);
    }
}
