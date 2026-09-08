import { type FormEvent, useEffect, useMemo, useRef, useState } from 'react';

import type { TeamChatMessage } from '@/shared/api/types';
import {
  createTeamChatClient,
  type TeamChatClientConnection,
} from '@/shared/realtime/workspace-collaboration';

type TeamChatProps = {
  clientId: string;
  projectId: number;
  senderName: string;
};

const formatChatTime = (value: string) => {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return '';
  }

  return new Intl.DateTimeFormat('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

export const TeamChat = ({
  clientId,
  projectId,
  senderName,
}: TeamChatProps) => {
  const [messages, setMessages] = useState<TeamChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const connectionRef = useRef<TeamChatClientConnection | null>(null);
  const normalizedSenderName = useMemo(
    () => senderName.trim() || '익명',
    [senderName],
  );

  useEffect(() => {
    const connection = createTeamChatClient({
      clientId,
      projectId,
      onMessage: (message) => {
        setMessages((currentMessages) => {
          if (currentMessages.some((item) => item.id === message.id)) {
            return currentMessages;
          }

          return [...currentMessages, message].slice(-100);
        });
      },
    });

    connectionRef.current = connection;

    return () => {
      connection.disconnect();
      connectionRef.current = null;
    };
  }, [clientId, projectId]);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextMessage = draft.trim();

    if (!nextMessage) {
      return;
    }

    connectionRef.current?.sendMessage(normalizedSenderName, nextMessage);
    setDraft('');
  };

  return (
    <section className="flex h-[17.5rem] min-h-[17.5rem] min-w-0 flex-col overflow-hidden rounded-[24px] border border-[var(--ws-border)] bg-[linear-gradient(180deg,rgba(37,37,38,0.96)_0%,rgba(20,20,20,0.98)_100%)] shadow-[0_18px_40px_rgba(0,0,0,0.26)]">
      <div className="flex items-center justify-between border-b border-[var(--ws-border)] bg-[rgba(18,18,18,0.88)] px-3 py-2">
        <div className="rounded-lg bg-[#1f1f1f] px-3 py-2 text-sm font-semibold text-white">
          Team Chat
        </div>
        <span className="text-xs text-[var(--ws-muted)]">Live</span>
      </div>

      <div className="min-h-0 flex-1 overflow-auto px-3 py-3">
        {messages.length > 0 ? (
          <div className="space-y-2">
            {messages.map((message) => {
              const isMine = message.clientId === clientId;

              return (
                <div
                  key={message.id}
                  className={`rounded-lg border px-3 py-2 text-sm ${
                    isMine
                      ? 'ml-6 border-[#24533d] bg-[#163626]'
                      : 'mr-6 border-[#35373b] bg-[#1f1f1f]'
                  }`}
                >
                  <div className="mb-1 flex items-center justify-between gap-2 text-[11px] text-[var(--ws-muted)]">
                    <span className="truncate">{message.senderName}</span>
                    <span>{formatChatTime(message.occurredAt)}</span>
                  </div>
                  <p className="break-words whitespace-pre-wrap text-[#d4d4d4]">
                    {message.message}
                  </p>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="rounded-md border border-dashed border-[#3c3c3c] bg-[#1e1e1e] px-3 py-2 text-sm text-[var(--ws-muted)]">
            아직 채팅 메시지가 없습니다.
          </div>
        )}
      </div>

      <form
        onSubmit={handleSubmit}
        className="flex gap-2 border-t border-[var(--ws-border)] bg-[#1f1f1f] p-2"
      >
        <input
          type="text"
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
          placeholder="메시지 입력"
          className="min-w-0 flex-1 rounded-md border border-[#3c3c3c] bg-[#161616] px-3 py-2 text-sm text-[#d4d4d4] transition outline-none placeholder:text-[var(--ws-muted)] focus:border-[#4fc1ff]"
        />
        <button
          type="submit"
          className="rounded-md border border-[#3c3c3c] bg-[#1e1e1e] px-3 py-2 text-sm font-semibold text-[#d4d4d4] transition hover:border-[#4a4a4a] hover:bg-[#252526]"
        >
          Send
        </button>
      </form>
    </section>
  );
};
