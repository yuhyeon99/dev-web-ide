package com.yuhyeon.devwebide.terminal.service;

import com.yuhyeon.devwebide.terminal.domain.TerminalLog;
import com.yuhyeon.devwebide.terminal.domain.TerminalStreamType;
import com.yuhyeon.devwebide.terminal.dto.TerminalLogListResponse;
import com.yuhyeon.devwebide.terminal.repository.TerminalLogRepository;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import com.yuhyeon.devwebide.workspace.repository.WorkspaceSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TerminalLogServiceTest {

    private static final Long WORKSPACE_SESSION_ID = 1L;

    @Mock
    private WorkspaceSessionRepository workspaceSessionRepository;

    @Mock
    private TerminalLogRepository terminalLogRepository;

    @InjectMocks
    private TerminalLogService terminalLogService;

    @Test
    @DisplayName("workspaceSessionId 기준 전체 로그 조회에 성공한다")
    void getTerminalLogs() {
        // given
        WorkspaceSession workspaceSession = mock(WorkspaceSession.class);
        TerminalLog firstLog = createTerminalLog(1L, 1L, TerminalStreamType.STDOUT, "first");
        TerminalLog secondLog = createTerminalLog(2L, 2L, TerminalStreamType.STDERR, "second");

        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));

        given(terminalLogRepository.findByWorkspaceSessionIdOrderBySequenceNoAsc(
                WORKSPACE_SESSION_ID
        )).willReturn(List.of(firstLog, secondLog));

        given(terminalLogRepository.findTopByWorkspaceSessionIdOrderBySequenceNoDesc(
                WORKSPACE_SESSION_ID
        )).willReturn(Optional.of(secondLog));

        // when
        TerminalLogListResponse response =
                terminalLogService.getTerminalLogs(WORKSPACE_SESSION_ID, null);

        // then
        assertThat(response.workspaceSessionId()).isEqualTo(WORKSPACE_SESSION_ID);
        assertThat(response.logs()).hasSize(2);
        assertThat(response.logs().get(0).content()).isEqualTo("first");
        assertThat(response.logs().get(1).content()).isEqualTo("second");
        assertThat(response.lastSequenceNo()).isEqualTo(2L);
    }

    @Test
    @DisplayName("sequenceNo 오름차순으로 응답한다")
    void getTerminalLogs_orderBySequenceNoAsc() {
        // given
        WorkspaceSession workspaceSession = mock(WorkspaceSession.class);
        TerminalLog firstLog = createTerminalLog(1L, 1L, TerminalStreamType.STDOUT, "first");
        TerminalLog secondLog = createTerminalLog(2L, 2L, TerminalStreamType.STDOUT, "second");
        TerminalLog thirdLog = createTerminalLog(3L, 3L, TerminalStreamType.STDOUT, "third");

        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));

        given(terminalLogRepository.findByWorkspaceSessionIdOrderBySequenceNoAsc(
                WORKSPACE_SESSION_ID
        )).willReturn(List.of(firstLog, secondLog, thirdLog));

        given(terminalLogRepository.findTopByWorkspaceSessionIdOrderBySequenceNoDesc(
                WORKSPACE_SESSION_ID
        )).willReturn(Optional.of(thirdLog));

        // when
        TerminalLogListResponse response =
                terminalLogService.getTerminalLogs(WORKSPACE_SESSION_ID, null);

        // then
        assertThat(response.logs())
                .extracting(log -> log.sequenceNo())
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("afterSequenceNo가 있으면 이후 로그만 조회한다")
    void getTerminalLogsAfterSequenceNo() {
        // given
        WorkspaceSession workspaceSession = mock(WorkspaceSession.class);
        TerminalLog thirdLog = createTerminalLog(3L, 3L, TerminalStreamType.STDOUT, "third");
        TerminalLog fourthLog = createTerminalLog(4L, 4L, TerminalStreamType.STDOUT, "fourth");

        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));

        given(terminalLogRepository
                .findByWorkspaceSessionIdAndSequenceNoGreaterThanOrderBySequenceNoAsc(
                        WORKSPACE_SESSION_ID,
                        2L
                )).willReturn(List.of(thirdLog, fourthLog));

        given(terminalLogRepository.findTopByWorkspaceSessionIdOrderBySequenceNoDesc(
                WORKSPACE_SESSION_ID
        )).willReturn(Optional.of(fourthLog));

        // when
        TerminalLogListResponse response =
                terminalLogService.getTerminalLogs(WORKSPACE_SESSION_ID, 2L);

        // then
        assertThat(response.logs())
                .extracting(log -> log.sequenceNo())
                .containsExactly(3L, 4L);
        assertThat(response.lastSequenceNo()).isEqualTo(4L);
    }

    @Test
    @DisplayName("로그가 없으면 빈 목록을 반환한다")
    void getTerminalLogsEmpty() {
        // given
        WorkspaceSession workspaceSession = mock(WorkspaceSession.class);

        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));

        given(terminalLogRepository.findByWorkspaceSessionIdOrderBySequenceNoAsc(
                WORKSPACE_SESSION_ID
        )).willReturn(List.of());

        given(terminalLogRepository.findTopByWorkspaceSessionIdOrderBySequenceNoDesc(
                WORKSPACE_SESSION_ID
        )).willReturn(Optional.empty());

        // when
        TerminalLogListResponse response =
                terminalLogService.getTerminalLogs(WORKSPACE_SESSION_ID, null);

        // then
        assertThat(response.logs()).isEmpty();
    }

    @Test
    @DisplayName("lastSequenceNo를 반환한다")
    void getTerminalLogsLastSequenceNo() {
        // given
        WorkspaceSession workspaceSession = mock(WorkspaceSession.class);
        TerminalLog lastLog = createTerminalLog(10L, 10L, TerminalStreamType.SYSTEM, "last");

        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));

        given(terminalLogRepository.findByWorkspaceSessionIdOrderBySequenceNoAsc(
                WORKSPACE_SESSION_ID
        )).willReturn(List.of());

        given(terminalLogRepository.findTopByWorkspaceSessionIdOrderBySequenceNoDesc(
                WORKSPACE_SESSION_ID
        )).willReturn(Optional.of(lastLog));

        // when
        TerminalLogListResponse response =
                terminalLogService.getTerminalLogs(WORKSPACE_SESSION_ID, null);

        // then
        assertThat(response.lastSequenceNo()).isEqualTo(10L);
    }

    @Test
    @DisplayName("로그가 없으면 lastSequenceNo는 null이다")
    void getTerminalLogsLastSequenceNoNull() {
        // given
        WorkspaceSession workspaceSession = mock(WorkspaceSession.class);

        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));

        given(terminalLogRepository.findByWorkspaceSessionIdOrderBySequenceNoAsc(
                WORKSPACE_SESSION_ID
        )).willReturn(List.of());

        given(terminalLogRepository.findTopByWorkspaceSessionIdOrderBySequenceNoDesc(
                WORKSPACE_SESSION_ID
        )).willReturn(Optional.empty());

        // when
        TerminalLogListResponse response =
                terminalLogService.getTerminalLogs(WORKSPACE_SESSION_ID, null);

        // then
        assertThat(response.lastSequenceNo()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 workspaceSessionId면 예외가 발생한다")
    void throwExceptionWhenWorkspaceSessionNotFound() {
        // given
        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> terminalLogService.getTerminalLogs(
                WORKSPACE_SESSION_ID,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("워크스페이스 세션을 찾을 수 없습니다.");

        then(terminalLogRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("afterSequenceNo가 음수면 예외가 발생한다")
    void throwExceptionWhenAfterSequenceNoIsNegative() {
        // when & then
        assertThatThrownBy(() -> terminalLogService.getTerminalLogs(
                WORKSPACE_SESSION_ID,
                -1L
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("afterSequenceNo는 0 이상이어야 합니다.");

        then(workspaceSessionRepository).shouldHaveNoInteractions();
        then(terminalLogRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("afterSequenceNo가 null이면 전체 조회 Repository 메서드를 호출한다")
    void callFindAllLogsWhenAfterSequenceNoIsNull() {
        // given
        WorkspaceSession workspaceSession = mock(WorkspaceSession.class);

        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));

        given(terminalLogRepository.findByWorkspaceSessionIdOrderBySequenceNoAsc(
                WORKSPACE_SESSION_ID
        )).willReturn(List.of());

        given(terminalLogRepository.findTopByWorkspaceSessionIdOrderBySequenceNoDesc(
                WORKSPACE_SESSION_ID
        )).willReturn(Optional.empty());

        // when
        terminalLogService.getTerminalLogs(WORKSPACE_SESSION_ID, null);

        // then
        then(terminalLogRepository).should()
                .findByWorkspaceSessionIdOrderBySequenceNoAsc(WORKSPACE_SESSION_ID);

        then(terminalLogRepository).should(never())
                .findByWorkspaceSessionIdAndSequenceNoGreaterThanOrderBySequenceNoAsc(
                        WORKSPACE_SESSION_ID,
                        null
                );
    }

    @Test
    @DisplayName("afterSequenceNo가 있으면 이후 조회 Repository 메서드를 호출한다")
    void callFindLogsAfterSequenceNoWhenAfterSequenceNoExists() {
        // given
        WorkspaceSession workspaceSession = mock(WorkspaceSession.class);

        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));

        given(terminalLogRepository
                .findByWorkspaceSessionIdAndSequenceNoGreaterThanOrderBySequenceNoAsc(
                        WORKSPACE_SESSION_ID,
                        5L
                )).willReturn(List.of());

        given(terminalLogRepository.findTopByWorkspaceSessionIdOrderBySequenceNoDesc(
                WORKSPACE_SESSION_ID
        )).willReturn(Optional.empty());

        // when
        terminalLogService.getTerminalLogs(WORKSPACE_SESSION_ID, 5L);

        // then
        then(terminalLogRepository).should()
                .findByWorkspaceSessionIdAndSequenceNoGreaterThanOrderBySequenceNoAsc(
                        WORKSPACE_SESSION_ID,
                        5L
                );

        then(terminalLogRepository).should(never())
                .findByWorkspaceSessionIdOrderBySequenceNoAsc(WORKSPACE_SESSION_ID);
    }

    private TerminalLog createTerminalLog(
            Long id,
            Long sequenceNo,
            TerminalStreamType streamType,
            String content
    ) {
        TerminalLog terminalLog = TerminalLog.builder()
                .workspaceSession(mock(WorkspaceSession.class))
                .sequenceNo(sequenceNo)
                .streamType(streamType)
                .content(content)
                .build();

        ReflectionTestUtils.setField(terminalLog, "id", id);
        ReflectionTestUtils.setField(
                terminalLog,
                "createdAt",
                LocalDateTime.of(2026, 7, 7, 10, 0).plusSeconds(sequenceNo)
        );

        return terminalLog;
    }
}
