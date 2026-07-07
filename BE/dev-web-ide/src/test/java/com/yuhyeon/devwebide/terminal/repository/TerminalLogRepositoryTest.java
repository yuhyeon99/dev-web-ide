package com.yuhyeon.devwebide.terminal.repository;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.domain.RuntimeStatus;
import com.yuhyeon.devwebide.terminal.domain.TerminalLog;
import com.yuhyeon.devwebide.terminal.domain.TerminalStreamType;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSessionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TerminalLogRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @jakarta.annotation.Resource
    private TerminalLogRepository terminalLogRepository;

    @Test
    @DisplayName("터미널 로그를 저장한다")
    void saveTerminalLog() {
        // given
        WorkspaceSession workspaceSession = saveWorkspaceSession();

        TerminalLog terminalLog = TerminalLog.builder()
                .workspaceSession(workspaceSession)
                .sequenceNo(1L)
                .streamType(TerminalStreamType.STDOUT)
                .content("hello")
                .build();

        // when
        TerminalLog savedTerminalLog =
                terminalLogRepository.save(terminalLog);

        flushAndClear();

        // then
        TerminalLog foundTerminalLog = terminalLogRepository
                .findById(savedTerminalLog.getId())
                .orElseThrow();

        assertThat(foundTerminalLog.getId()).isNotNull();
        assertThat(foundTerminalLog.getWorkspaceSession().getId())
                .isEqualTo(workspaceSession.getId());
        assertThat(foundTerminalLog.getSequenceNo()).isEqualTo(1L);
        assertThat(foundTerminalLog.getStreamType()).isEqualTo(TerminalStreamType.STDOUT);
        assertThat(foundTerminalLog.getContent()).isEqualTo("hello");
        assertThat(foundTerminalLog.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("workspaceSessionId 기준 로그 목록을 조회한다")
    void findByWorkspaceSessionIdOrderBySequenceNoAsc() {
        // given
        WorkspaceSession workspaceSession = saveWorkspaceSession();

        saveTerminalLog(workspaceSession, 1L, TerminalStreamType.STDOUT, "first");
        saveTerminalLog(workspaceSession, 2L, TerminalStreamType.STDERR, "second");

        flushAndClear();

        // when
        List<TerminalLog> logs =
                terminalLogRepository.findByWorkspaceSessionIdOrderBySequenceNoAsc(
                        workspaceSession.getId()
                );

        // then
        assertThat(logs).hasSize(2);
        assertThat(logs)
                .extracting(TerminalLog::getContent)
                .containsExactly("first", "second");
    }

    @Test
    @DisplayName("sequenceNo 오름차순으로 로그 목록을 조회한다")
    void findByWorkspaceSessionIdOrderBySequenceNoAsc_sorted() {
        // given
        WorkspaceSession workspaceSession = saveWorkspaceSession();

        saveTerminalLog(workspaceSession, 3L, TerminalStreamType.STDOUT, "third");
        saveTerminalLog(workspaceSession, 1L, TerminalStreamType.STDOUT, "first");
        saveTerminalLog(workspaceSession, 2L, TerminalStreamType.STDOUT, "second");

        flushAndClear();

        // when
        List<TerminalLog> logs =
                terminalLogRepository.findByWorkspaceSessionIdOrderBySequenceNoAsc(
                        workspaceSession.getId()
                );

        // then
        assertThat(logs)
                .extracting(TerminalLog::getSequenceNo)
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("특정 sequenceNo 이후 로그를 조회한다")
    void findByWorkspaceSessionIdAndSequenceNoGreaterThanOrderBySequenceNoAsc() {
        // given
        WorkspaceSession workspaceSession = saveWorkspaceSession();

        saveTerminalLog(workspaceSession, 1L, TerminalStreamType.STDOUT, "first");
        saveTerminalLog(workspaceSession, 2L, TerminalStreamType.STDOUT, "second");
        saveTerminalLog(workspaceSession, 3L, TerminalStreamType.STDOUT, "third");
        saveTerminalLog(workspaceSession, 4L, TerminalStreamType.STDOUT, "fourth");

        flushAndClear();

        // when
        List<TerminalLog> logs =
                terminalLogRepository
                        .findByWorkspaceSessionIdAndSequenceNoGreaterThanOrderBySequenceNoAsc(
                                workspaceSession.getId(),
                                2L
                        );

        // then
        assertThat(logs)
                .extracting(TerminalLog::getSequenceNo)
                .containsExactly(3L, 4L);
    }

    @Test
    @DisplayName("workspaceSession 기준 마지막 sequenceNo 로그를 조회한다")
    void findTopByWorkspaceSessionIdOrderBySequenceNoDesc() {
        // given
        WorkspaceSession workspaceSession = saveWorkspaceSession();

        saveTerminalLog(workspaceSession, 1L, TerminalStreamType.STDOUT, "first");
        saveTerminalLog(workspaceSession, 3L, TerminalStreamType.STDERR, "third");
        saveTerminalLog(workspaceSession, 2L, TerminalStreamType.SYSTEM, "second");

        flushAndClear();

        // when
        Optional<TerminalLog> latestLog =
                terminalLogRepository.findTopByWorkspaceSessionIdOrderBySequenceNoDesc(
                        workspaceSession.getId()
                );

        // then
        assertThat(latestLog).isPresent();
        assertThat(latestLog.get().getSequenceNo()).isEqualTo(3L);
        assertThat(latestLog.get().getContent()).isEqualTo("third");
    }

    @Test
    @DisplayName("STDOUT STDERR SYSTEM 타입 로그를 저장한다")
    void saveAllStreamTypes() {
        // given
        WorkspaceSession workspaceSession = saveWorkspaceSession();

        saveTerminalLog(workspaceSession, 1L, TerminalStreamType.STDOUT, "stdout");
        saveTerminalLog(workspaceSession, 2L, TerminalStreamType.STDERR, "stderr");
        saveTerminalLog(workspaceSession, 3L, TerminalStreamType.SYSTEM, "system");

        flushAndClear();

        // when
        List<TerminalLog> logs =
                terminalLogRepository.findByWorkspaceSessionIdOrderBySequenceNoAsc(
                        workspaceSession.getId()
                );

        // then
        assertThat(logs)
                .extracting(TerminalLog::getStreamType)
                .containsExactly(
                        TerminalStreamType.STDOUT,
                        TerminalStreamType.STDERR,
                        TerminalStreamType.SYSTEM
                );
    }

    @Test
    @DisplayName("다른 WorkspaceSession 로그와 섞이지 않는다")
    void findByWorkspaceSessionId_excludesOtherWorkspaceSessionLogs() {
        // given
        WorkspaceSession firstWorkspaceSession = saveWorkspaceSession();
        WorkspaceSession secondWorkspaceSession = saveWorkspaceSession();

        saveTerminalLog(firstWorkspaceSession, 1L, TerminalStreamType.STDOUT, "first-session");
        saveTerminalLog(secondWorkspaceSession, 1L, TerminalStreamType.STDOUT, "second-session");

        flushAndClear();

        // when
        List<TerminalLog> logs =
                terminalLogRepository.findByWorkspaceSessionIdOrderBySequenceNoAsc(
                        firstWorkspaceSession.getId()
                );

        // then
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getWorkspaceSession().getId())
                .isEqualTo(firstWorkspaceSession.getId());
        assertThat(logs.get(0).getContent()).isEqualTo("first-session");
    }

    private TerminalLog saveTerminalLog(
            WorkspaceSession workspaceSession,
            Long sequenceNo,
            TerminalStreamType streamType,
            String content
    ) {
        TerminalLog terminalLog = TerminalLog.builder()
                .workspaceSession(workspaceSession)
                .sequenceNo(sequenceNo)
                .streamType(streamType)
                .content(content)
                .build();

        terminalLogRepository.save(terminalLog);
        return terminalLog;
    }

    private WorkspaceSession saveWorkspaceSession() {
        User user = User.builder()
                .email("terminal-" + UUID.randomUUID() + "@example.com")
                .nickname("터미널테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        entityManager.persist(user);

        Runtime runtime = Runtime.builder()
                .name("node-" + UUID.randomUUID())
                .displayName("Node.js")
                .version("20")
                .dockerImage("node:20")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();

        entityManager.persist(runtime);

        Project project = Project.builder()
                .ownerUser(user)
                .runtime(runtime)
                .name("터미널 테스트 프로젝트")
                .description("터미널 로그 테스트 프로젝트입니다.")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/terminal-" + UUID.randomUUID())
                .build();

        entityManager.persist(project);

        WorkspaceSession workspaceSession = WorkspaceSession.builder()
                .project(project)
                .runtime(runtime)
                .user(user)
                .status(WorkspaceSessionStatus.RUNNING)
                .build();

        entityManager.persist(workspaceSession);
        return workspaceSession;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
