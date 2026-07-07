package com.yuhyeon.devwebide.execution.controller;

import com.yuhyeon.devwebide.execution.dto.WorkspaceSessionStopResponse;
import com.yuhyeon.devwebide.execution.service.WorkspaceSessionStopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workspace-sessions")
public class WorkspaceSessionStopController {

    private final WorkspaceSessionStopService workspaceSessionStopService;

    @PostMapping("/{workspaceSessionId}/stop")
    public ResponseEntity<WorkspaceSessionStopResponse> stopWorkspaceSession(
            @PathVariable Long workspaceSessionId
    ) {
        WorkspaceSessionStopResponse response =
                workspaceSessionStopService.stopWorkspaceSession(workspaceSessionId);

        return ResponseEntity.ok(response);
    }
}
