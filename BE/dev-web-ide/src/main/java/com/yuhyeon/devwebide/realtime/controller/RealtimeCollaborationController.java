package com.yuhyeon.devwebide.realtime.controller;

import com.yuhyeon.devwebide.realtime.dto.CrdtUpdateMessage;
import com.yuhyeon.devwebide.realtime.dto.CrdtUpdateRequest;
import com.yuhyeon.devwebide.realtime.dto.LiveFileContentMessage;
import com.yuhyeon.devwebide.realtime.dto.LiveFileContentRequest;
import com.yuhyeon.devwebide.realtime.dto.LiveFileContentSnapshotRequest;
import com.yuhyeon.devwebide.realtime.dto.TeamChatMessage;
import com.yuhyeon.devwebide.realtime.dto.TeamChatMessageRequest;
import com.yuhyeon.devwebide.realtime.dto.WorkspacePresenceRequest;
import com.yuhyeon.devwebide.realtime.service.CrdtUpdatePublisher;
import com.yuhyeon.devwebide.realtime.service.LiveFileContentPublisher;
import com.yuhyeon.devwebide.realtime.service.TeamChatPublisher;
import com.yuhyeon.devwebide.realtime.service.WorkspacePresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * 실시간 공동편집과 팀 채팅 WebSocket 메시지를 처리합니다.
 */
@Controller
@RequiredArgsConstructor
public class RealtimeCollaborationController {

    private final CrdtUpdatePublisher crdtUpdatePublisher;
    private final LiveFileContentPublisher liveFileContentPublisher;
    private final TeamChatPublisher teamChatPublisher;
    private final WorkspacePresenceService workspacePresenceService;

    @MessageMapping("/projects/{projectId}/files/{fileId}/crdt")
    public void publishCrdtUpdate(
            @DestinationVariable Long projectId,
            @DestinationVariable Long fileId,
            @Payload CrdtUpdateRequest request
    ) {
        crdtUpdatePublisher.publish(CrdtUpdateMessage.of(
                projectId,
                fileId,
                request.clientId(),
                request.updateBase64()
        ));
    }

    @MessageMapping("/projects/{projectId}/files/{fileId}/live-content")
    public void publishLiveFileContent(
            @DestinationVariable Long projectId,
            @DestinationVariable Long fileId,
            @Payload LiveFileContentRequest request
    ) {
        liveFileContentPublisher.publish(LiveFileContentMessage.of(
                projectId,
                fileId,
                request.clientId(),
                request.content()
        ));
    }

    @MessageMapping("/projects/{projectId}/files/{fileId}/live-content/snapshot")
    public void publishLiveFileContentSnapshot(
            @DestinationVariable Long projectId,
            @DestinationVariable Long fileId,
            @Payload LiveFileContentSnapshotRequest request
    ) {
        liveFileContentPublisher.publishSnapshot(projectId, fileId);
    }

    @MessageMapping("/projects/{projectId}/chat")
    public void publishTeamChatMessage(
            @DestinationVariable Long projectId,
            @Payload TeamChatMessageRequest request
    ) {
        teamChatPublisher.publish(TeamChatMessage.of(
                projectId,
                request.clientId(),
                request.senderName(),
                request.message()
        ));
    }

    @MessageMapping("/projects/{projectId}/presence/join")
    public void joinWorkspacePresence(
            @DestinationVariable Long projectId,
            @Payload WorkspacePresenceRequest request
    ) {
        workspacePresenceService.join(projectId, request);
    }

    @MessageMapping("/projects/{projectId}/presence/heartbeat")
    public void heartbeatWorkspacePresence(
            @DestinationVariable Long projectId,
            @Payload WorkspacePresenceRequest request
    ) {
        workspacePresenceService.join(projectId, request);
    }

    @MessageMapping("/projects/{projectId}/presence/leave")
    public void leaveWorkspacePresence(
            @DestinationVariable Long projectId,
            @Payload WorkspacePresenceRequest request
    ) {
        workspacePresenceService.leave(projectId, request.clientId());
    }

    @MessageMapping("/projects/{projectId}/presence/snapshot")
    public void publishWorkspacePresenceSnapshot(
            @DestinationVariable Long projectId
    ) {
        workspacePresenceService.publish(projectId);
    }
}
