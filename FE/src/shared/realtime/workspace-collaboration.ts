/* eslint-disable no-unused-vars */

import { Client, type IMessage } from '@stomp/stompjs';

import type { CrdtUpdateMessage, TeamChatMessage } from '@/shared/api/types';
import { REALTIME_WS_URL } from '@/shared/config/api';

import { base64ToUint8Array, uint8ArrayToBase64 } from './encoding';

type CrdtClientOptions = {
  clientId: string;
  fileId: number;
  onRemoteUpdate: (update: Uint8Array) => void;
  projectId: number;
};

type TeamChatClientOptions = {
  clientId: string;
  onMessage: (message: TeamChatMessage) => void;
  projectId: number;
};

export type CrdtClientConnection = {
  disconnect: () => void;
  sendUpdate: (update: Uint8Array) => void;
};

export type TeamChatClientConnection = {
  disconnect: () => void;
  sendMessage: (senderName: string, message: string) => void;
};

const parseMessage = <T>(message: IMessage) => JSON.parse(message.body) as T;

export const createCrdtClient = ({
  clientId,
  fileId,
  onRemoteUpdate,
  projectId,
}: CrdtClientOptions): CrdtClientConnection => {
  const client = new Client({
    brokerURL: REALTIME_WS_URL,
    reconnectDelay: 5000,
    debug: () => undefined,
    onConnect: () => {
      client.subscribe(
        `/topic/projects/${projectId}/files/${fileId}/crdt`,
        (message) => {
          const event = parseMessage<CrdtUpdateMessage>(message);

          if (event.clientId === clientId || !event.updateBase64) {
            return;
          }

          onRemoteUpdate(base64ToUint8Array(event.updateBase64));
        },
      );
    },
  });

  client.activate();

  return {
    disconnect: () => {
      void client.deactivate();
    },
    sendUpdate: (update) => {
      if (!client.connected) {
        return;
      }

      client.publish({
        destination: `/app/projects/${projectId}/files/${fileId}/crdt`,
        body: JSON.stringify({
          clientId,
          updateBase64: uint8ArrayToBase64(update),
        }),
      });
    },
  };
};

export const createTeamChatClient = ({
  clientId,
  onMessage,
  projectId,
}: TeamChatClientOptions): TeamChatClientConnection => {
  const client = new Client({
    brokerURL: REALTIME_WS_URL,
    reconnectDelay: 5000,
    debug: () => undefined,
    onConnect: () => {
      client.subscribe(`/topic/projects/${projectId}/chat`, (message) => {
        onMessage(parseMessage<TeamChatMessage>(message));
      });
    },
  });

  client.activate();

  return {
    disconnect: () => {
      void client.deactivate();
    },
    sendMessage: (senderName, message) => {
      const trimmedMessage = message.trim();

      if (!client.connected || !trimmedMessage) {
        return;
      }

      client.publish({
        destination: `/app/projects/${projectId}/chat`,
        body: JSON.stringify({
          clientId,
          senderName,
          message: trimmedMessage,
        }),
      });
    },
  };
};
