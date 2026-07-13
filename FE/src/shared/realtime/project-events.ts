/* eslint-disable no-unused-vars */

import { Client } from '@stomp/stompjs';

import type { ProjectRealtimeEvent } from '@/shared/api/types';
import { REALTIME_WS_URL } from '@/shared/config/api';

type ProjectRealtimeEventHandler = (event: ProjectRealtimeEvent) => void;

export const subscribeProjectRealtimeEvents = (
  projectId: number,
  onEvent: ProjectRealtimeEventHandler,
) => {
  const client = new Client({
    brokerURL: REALTIME_WS_URL,
    reconnectDelay: 5000,
    debug: () => undefined,
    onConnect: () => {
      client.subscribe(`/topic/projects/${projectId}/events`, (message) => {
        const event = JSON.parse(message.body) as ProjectRealtimeEvent;

        onEvent(event);
      });
    },
  });

  client.activate();

  return () => {
    void client.deactivate();
  };
};
