package com.yuhyeon.devwebide.realtime.controller;

import com.yuhyeon.devwebide.realtime.dto.CrdtUpdateMessage;
import com.yuhyeon.devwebide.realtime.dto.CrdtUpdateRequest;
import com.yuhyeon.devwebide.realtime.dto.TeamChatMessage;
import com.yuhyeon.devwebide.realtime.dto.TeamChatMessageRequest;
import com.yuhyeon.devwebide.realtime.service.CrdtUpdatePublisher;
import com.yuhyeon.devwebide.realtime.service.TeamChatPublisher;
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
    private final TeamChatPublisher teamChatPublisher;

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
}
