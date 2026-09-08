/* eslint-disable no-unused-vars */

import { Client, type IMessage } from '@stomp/stompjs';

import type { WorkspacePresenceMessage } from '@/shared/api/types';
import { REALTIME_WS_URL } from '@/shared/config/api';

type WorkspacePresenceClientOptions = {
  clientId: string;
  currentFile: string;
  displayName: string;
  onPresenceChange: (message: WorkspacePresenceMessage) => void;
  presenceKey: string;
  projectId: number;
  role: string;
};

export type WorkspacePresenceClientConnection = {
  disconnect: () => void;
  update: (currentFile: string) => void;
};

const createPresenceBody = ({
  clientId,
  currentFile,
  displayName,
  presenceKey,
  role,
}: Pick<
  WorkspacePresenceClientOptions,
  'clientId' | 'currentFile' | 'displayName' | 'presenceKey' | 'role'
>) =>
  JSON.stringify({
    clientId,
    currentFile,
    displayName,
    presenceKey,
    role,
    status: 'online',
  });

export const createWorkspacePresenceClient = ({
  clientId,
  currentFile,
  displayName,
  onPresenceChange,
  presenceKey,
  projectId,
  role,
}: WorkspacePresenceClientOptions): WorkspacePresenceClientConnection => {
  let latestCurrentFile = currentFile;
  let didPublishInitialPresence = false;
  const createCurrentPresenceMessage = (): WorkspacePresenceMessage => ({
    projectId,
    users: [
      {
        clientId,
        presenceKey,
        currentFile: latestCurrentFile,
        displayName: displayName.trim() || 'Guest',
        joinedAt: new Date().toISOString(),
        lastSeenAt: new Date().toISOString(),
        role: role.trim() || 'Editor',
        status: 'online',
      },
    ],
    occurredAt: new Date().toISOString(),
  });
  const publishJoinAndSnapshot = () => {
    if (didPublishInitialPresence || !client.connected) {
      return;
    }

    didPublishInitialPresence = true;
    client.publish({
      destination: `/app/projects/${projectId}/presence/join`,
      body: createPresenceBody({
        clientId,
        currentFile: latestCurrentFile,
        displayName,
        presenceKey,
        role,
      }),
    });
    client.publish({
      destination: `/app/projects/${projectId}/presence/snapshot`,
      body: JSON.stringify({ clientId }),
    });
  };
  const client = new Client({
    brokerURL: REALTIME_WS_URL,
    reconnectDelay: 5000,
    debug: () => undefined,
    onConnect: () => {
      const subscriptionReceiptId = `presence-subscribe-${projectId}-${clientId}`;

      didPublishInitialPresence = false;
      client.watchForReceipt(subscriptionReceiptId, publishJoinAndSnapshot);
      client.subscribe(
        `/topic/projects/${projectId}/presence`,
        (message: IMessage) => {
          onPresenceChange(
            JSON.parse(message.body) as WorkspacePresenceMessage,
          );
        },
        { receipt: subscriptionReceiptId },
      );
      window.setTimeout(publishJoinAndSnapshot, 250);
      onPresenceChange(createCurrentPresenceMessage());
    },
  });
  const publishPresence = (destination: string) => {
    if (!client.connected) {
      return;
    }

    client.publish({
      destination,
      body: createPresenceBody({
        clientId,
        currentFile: latestCurrentFile,
        displayName,
        presenceKey,
        role,
      }),
    });
  };
  const heartbeatInterval = window.setInterval(() => {
    publishPresence(`/app/projects/${projectId}/presence/heartbeat`);
  }, 15000);

  client.activate();

  return {
    disconnect: () => {
      window.clearInterval(heartbeatInterval);
      publishPresence(`/app/projects/${projectId}/presence/leave`);
      void client.deactivate();
    },
    update: (nextCurrentFile) => {
      latestCurrentFile = nextCurrentFile;
      publishPresence(`/app/projects/${projectId}/presence/heartbeat`);
    },
  };
};
