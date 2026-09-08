/* eslint-disable no-unused-vars */

import { Client, type IMessage } from '@stomp/stompjs';

import type { WorkspacePresenceMessage } from '@/shared/api/types';
import { REALTIME_WS_URL } from '@/shared/config/api';

type WorkspacePresenceClientOptions = {
  clientId: string;
  currentFile: string;
  displayName: string;
  onPresenceChange: (message: WorkspacePresenceMessage) => void;
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
  role,
}: Pick<
  WorkspacePresenceClientOptions,
  'clientId' | 'currentFile' | 'displayName' | 'role'
>) =>
  JSON.stringify({
    clientId,
    currentFile,
    displayName,
    role,
    status: 'online',
  });

export const createWorkspacePresenceClient = ({
  clientId,
  currentFile,
  displayName,
  onPresenceChange,
  projectId,
  role,
}: WorkspacePresenceClientOptions): WorkspacePresenceClientConnection => {
  let latestCurrentFile = currentFile;
  const client = new Client({
    brokerURL: REALTIME_WS_URL,
    reconnectDelay: 5000,
    debug: () => undefined,
    onConnect: () => {
      client.subscribe(
        `/topic/projects/${projectId}/presence`,
        (message: IMessage) => {
          onPresenceChange(
            JSON.parse(message.body) as WorkspacePresenceMessage,
          );
        },
      );
      client.publish({
        destination: `/app/projects/${projectId}/presence/join`,
        body: createPresenceBody({
          clientId,
          currentFile: latestCurrentFile,
          displayName,
          role,
        }),
      });
      client.publish({
        destination: `/app/projects/${projectId}/presence/snapshot`,
        body: JSON.stringify({ clientId }),
      });
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
