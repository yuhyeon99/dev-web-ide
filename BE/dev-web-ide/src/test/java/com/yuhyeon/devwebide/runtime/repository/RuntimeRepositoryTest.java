package com.yuhyeon.devwebide.runtime.repository;

import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.domain.RuntimeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RuntimeRepositoryTest {

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Test
    @DisplayName("런타임 저장")
    void saveRuntime() {
        Runtime runtime = Runtime.builder()
                .name("node-20")
                .displayName("Node.js 20")
                .version("20")
                .dockerImage("node:20")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();

        Runtime savedRuntime = runtimeRepository.save(runtime);

        assertThat(savedRuntime.getId()).isNotNull();
        assertThat(savedRuntime.getName()).isEqualTo("node-20");
        assertThat(savedRuntime.getDisplayName()).isEqualTo("Node.js 20");
        assertThat(savedRuntime.getVersion()).isEqualTo("20");
        assertThat(savedRuntime.getDockerImage()).isEqualTo("node:20");
        assertThat(savedRuntime.getLanguage()).isEqualTo(RuntimeLanguage.NODE);
        assertThat(savedRuntime.getStatus()).isEqualTo(RuntimeStatus.ACTIVE);
        assertThat(savedRuntime.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("name으로 런타임 조회")
    void findByName() {
        Runtime runtime = Runtime.builder()
                .name("python-3.12")
                .displayName("Python 3.12")
                .version("3.12")
                .dockerImage("python:3.12")
                .language(RuntimeLanguage.PYTHON)
                .status(RuntimeStatus.ACTIVE)
                .build();

        runtimeRepository.save(runtime);

        Optional<Runtime> result = runtimeRepository.findByName("python-3.12");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("python-3.12");
        assertThat(result.get().getDisplayName()).isEqualTo("Python 3.12");
        assertThat(result.get().getLanguage()).isEqualTo(RuntimeLanguage.PYTHON);
    }

    @Test
    @DisplayName("name 존재 여부 확인")
    void existsByName() {
        Runtime runtime = Runtime.builder()
                .name("java-21")
                .displayName("Java 21")
                .version("21")
                .dockerImage("eclipse-temurin:21")
                .language(RuntimeLanguage.JAVA)
                .status(RuntimeStatus.ACTIVE)
                .build();

        runtimeRepository.save(runtime);

        boolean exists = runtimeRepository.existsByName("java-21");
        boolean notExists = runtimeRepository.existsByName("java-17");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("상태로 런타임 목록 조회")
    void findByStatus() {
        Runtime activeNode = Runtime.builder()
                .name("node-20-active")
                .displayName("Node.js 20")
                .version("20")
                .dockerImage("node:20")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();

        Runtime activePython = Runtime.builder()
                .name("python-3.12-active")
                .displayName("Python 3.12")
                .version("3.12")
                .dockerImage("python:3.12")
                .language(RuntimeLanguage.PYTHON)
                .status(RuntimeStatus.ACTIVE)
                .build();

        Runtime inactiveJava = Runtime.builder()
                .name("java-21-inactive")
                .displayName("Java 21")
                .version("21")
                .dockerImage("eclipse-temurin:21")
                .language(RuntimeLanguage.JAVA)
                .status(RuntimeStatus.INACTIVE)
                .build();

        runtimeRepository.save(activeNode);
        runtimeRepository.save(activePython);
        runtimeRepository.save(inactiveJava);

        List<Runtime> result = runtimeRepository.findByStatus(RuntimeStatus.ACTIVE);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Runtime::getName)
                .containsExactlyInAnyOrder(
                        "node-20-active",
                        "python-3.12-active"
                );
    }

    @Test
    @DisplayName("언어 타입으로 런타임 목록 조회")
    void findByLanguage() {
        Runtime node20 = Runtime.builder()
                .name("node-20-language")
                .displayName("Node.js 20")
                .version("20")
                .dockerImage("node:20")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();

        Runtime node22 = Runtime.builder()
                .name("node-22-language")
                .displayName("Node.js 22")
                .version("22")
                .dockerImage("node:22")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.INACTIVE)
                .build();

        Runtime python = Runtime.builder()
                .name("python-3.12-language")
                .displayName("Python 3.12")
                .version("3.12")
                .dockerImage("python:3.12")
                .language(RuntimeLanguage.PYTHON)
                .status(RuntimeStatus.ACTIVE)
                .build();

        runtimeRepository.save(node20);
        runtimeRepository.save(node22);
        runtimeRepository.save(python);

        List<Runtime> result =
                runtimeRepository.findByLanguage(RuntimeLanguage.NODE);

        assertThat(result).hasSize(2);
        assertThat(result)
                .allMatch(runtime -> runtime.getLanguage() == RuntimeLanguage.NODE);
        assertThat(result)
                .extracting(Runtime::getName)
                .containsExactlyInAnyOrder(
                        "node-20-language",
                        "node-22-language"
                );
    }

    @Test
    @DisplayName("언어 타입과 상태로 런타임 목록 조회")
    void findByLanguageAndStatus() {
        Runtime activeNode = Runtime.builder()
                .name("node-20-active-filter")
                .displayName("Node.js 20")
                .version("20")
                .dockerImage("node:20")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();

        Runtime inactiveNode = Runtime.builder()
                .name("node-22-inactive-filter")
                .displayName("Node.js 22")
                .version("22")
                .dockerImage("node:22")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.INACTIVE)
                .build();

        Runtime activePython = Runtime.builder()
                .name("python-3.12-active-filter")
                .displayName("Python 3.12")
                .version("3.12")
                .dockerImage("python:3.12")
                .language(RuntimeLanguage.PYTHON)
                .status(RuntimeStatus.ACTIVE)
                .build();

        runtimeRepository.save(activeNode);
        runtimeRepository.save(inactiveNode);
        runtimeRepository.save(activePython);

        List<Runtime> result =
                runtimeRepository.findByLanguageAndStatus(
                        RuntimeLanguage.NODE,
                        RuntimeStatus.ACTIVE
                );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName())
                .isEqualTo("node-20-active-filter");
        assertThat(result.get(0).getLanguage())
                .isEqualTo(RuntimeLanguage.NODE);
        assertThat(result.get(0).getStatus())
                .isEqualTo(RuntimeStatus.ACTIVE);
    }

    @Test
    @DisplayName("런타임 활성/비활성 상태 변경")
    void updateRuntimeStatus() {
        Runtime runtime = Runtime.builder()
                .name("cpp-gcc")
                .displayName("C++ GCC")
                .version("latest")
                .dockerImage("gcc:latest")
                .language(RuntimeLanguage.CPP)
                .status(RuntimeStatus.ACTIVE)
                .build();

        Runtime savedRuntime = runtimeRepository.save(runtime);

        savedRuntime.deactivate();

        assertThat(savedRuntime.getStatus())
                .isEqualTo(RuntimeStatus.INACTIVE);
        assertThat(savedRuntime.isActive()).isFalse();

        savedRuntime.activate();

        assertThat(savedRuntime.getStatus())
                .isEqualTo(RuntimeStatus.ACTIVE);
        assertThat(savedRuntime.isActive()).isTrue();
    }
}