package com.yuhyeon.devwebide.execution.service;

import com.yuhyeon.devwebide.execution.domain.ContainerInstance;
import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;
import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.terminal.domain.TerminalLog;
import com.yuhyeon.devwebide.terminal.domain.TerminalStreamType;
import com.yuhyeon.devwebide.terminal.repository.TerminalLogRepository;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

/**
 * 로컬 컨테이너 실행 서비스
 *
 * 실제 ECS/Fargate 연동 전까지 사용하는 임시 구현체입니다.
 */
@Service
public class LocalContainerExecutionService implements ContainerExecutionService {

    private static final String PROVIDER = "LOCAL";
    private static final Duration PROCESS_TIMEOUT = Duration.ofSeconds(30);

    private final TerminalLogRepository terminalLogRepository;
    private final Path storageRoot;

    public LocalContainerExecutionService(
            TerminalLogRepository terminalLogRepository,
            @Value("${app.storage.project-root:./storage}") String storageRoot
    ) {
        this.terminalLogRepository = terminalLogRepository;
        this.storageRoot = Path.of(storageRoot)
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public ContainerStartResult start(
            Project project,
            Runtime runtime,
            WorkspaceSession workspaceSession
    ) {
        runProjectCommand(project, runtime, workspaceSession);

        return new ContainerStartResult(
                PROVIDER,
                null,
                "local-" + UUID.randomUUID(),
                runtime.getDockerImage(),
                ContainerInstanceStatus.RUNNING,
                project.getStoragePath()
        );
    }

    @Override
    public void stop(ContainerInstance containerInstance) {
        // 실제 컨테이너 종료 연동 전 no-op
    }

    private void runProjectCommand(
            Project project,
            Runtime runtime,
            WorkspaceSession workspaceSession
    ) {
        if (runtime.getLanguage() != RuntimeLanguage.PYTHON) {
            saveLog(
                    workspaceSession,
                    TerminalStreamType.SYSTEM,
                    "현재 로컬 실행은 Python 프로젝트만 지원합니다."
            );
            return;
        }

        Path projectRootPath = resolveProjectRootPath(project);
        Path mainPath = projectRootPath.resolve("main.py").normalize();

        if (!mainPath.startsWith(projectRootPath) || !Files.exists(mainPath)) {
            saveLog(
                    workspaceSession,
                    TerminalStreamType.STDERR,
                    "main.py 파일을 찾을 수 없습니다."
            );
            return;
        }

        Optional<ProcessBuilder> pythonProcess =
                createPythonProcess(runtime, projectRootPath);

        if (pythonProcess.isEmpty()) {
            saveLog(
                    workspaceSession,
                    TerminalStreamType.STDERR,
                    "Python 실행 환경을 찾을 수 없습니다. 서버 이미지에 python3가 설치되어 있어야 합니다."
            );
            return;
        }

        ProcessBuilder processBuilder = pythonProcess.get();
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            CompletableFuture<List<String>> outputFuture =
                    CompletableFuture.supplyAsync(() -> readOutput(process));

            boolean finished = process.waitFor(
                    PROCESS_TIMEOUT.toSeconds(),
                    TimeUnit.SECONDS
            );

            if (!finished) {
                process.destroyForcibly();
            }

            List<String> lines = outputFuture.get(3, TimeUnit.SECONDS);

            if (!finished) {
                lines.add("실행 시간이 초과되었습니다.");
            }

            if (lines.isEmpty()) {
                lines.add("프로그램 출력이 없습니다.");
            }

            saveLogs(workspaceSession, TerminalStreamType.STDOUT, lines);
        } catch (IOException e) {
            saveLog(
                    workspaceSession,
                    TerminalStreamType.STDERR,
                    "Python 실행 명령을 시작할 수 없습니다: " + e.getMessage()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            saveLog(
                    workspaceSession,
                    TerminalStreamType.STDERR,
                    "Python 실행이 중단되었습니다."
            );
        } catch (ExecutionException | java.util.concurrent.TimeoutException e) {
            saveLog(
                    workspaceSession,
                    TerminalStreamType.STDERR,
                    "Python 실행 출력을 읽을 수 없습니다: " + e.getMessage()
            );
        }
    }

    private Optional<ProcessBuilder> createPythonProcess(
            Runtime runtime,
            Path projectRootPath
    ) {
        if (isCommandAvailable("python3")) {
            return Optional.of(new ProcessBuilder("python3", "main.py")
                    .directory(projectRootPath.toFile()));
        }

        if (isCommandAvailable("python")) {
            return Optional.of(new ProcessBuilder("python", "main.py")
                    .directory(projectRootPath.toFile()));
        }

        if (isCommandAvailable("docker")) {
            return Optional.of(new ProcessBuilder(
                    "docker",
                    "run",
                    "--rm",
                    "-v",
                    projectRootPath + ":/workspace",
                    "-w",
                    "/workspace",
                    runtime.getDockerImage(),
                    "python",
                    "main.py"
            ));
        }

        return Optional.empty();
    }

    private boolean isCommandAvailable(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version")
                    .redirectErrorStream(true)
                    .start();

            return process.waitFor(3, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> readOutput(Process process) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                process.getInputStream(),
                StandardCharsets.UTF_8
        ))) {
            String line;

            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            lines.add("Python 실행 출력을 읽을 수 없습니다: " + e.getMessage());
        }

        return lines;
    }

    private void saveLog(
            WorkspaceSession workspaceSession,
            TerminalStreamType streamType,
            String content
    ) {
        saveLogs(workspaceSession, streamType, List.of(content));
    }

    private void saveLogs(
            WorkspaceSession workspaceSession,
            TerminalStreamType streamType,
            List<String> lines
    ) {
        List<TerminalLog> logs = new ArrayList<>();
        long sequenceNo = 1L;

        for (String line : lines) {
            logs.add(TerminalLog.builder()
                    .workspaceSession(workspaceSession)
                    .sequenceNo(sequenceNo++)
                    .streamType(streamType)
                    .content(line)
                    .build());
        }

        terminalLogRepository.saveAll(logs);
    }

    private Path resolveProjectRootPath(Project project) {
        String storagePath = removeLeadingSlash(project.getStoragePath());
        Path projectRootPath = storageRoot.resolve(storagePath).normalize();

        if (!projectRootPath.startsWith(storageRoot)) {
            throw new IllegalArgumentException("허용되지 않은 프로젝트 저장 경로입니다.");
        }

        return projectRootPath;
    }

    private String removeLeadingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        if (value.startsWith("/")) {
            return value.substring(1);
        }

        return value;
    }
}
