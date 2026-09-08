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
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<TeamChatMessage[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [draft, setDraft] = useState('');
  const connectionRef = useRef<TeamChatClientConnection | null>(null);
  const isOpenRef = useRef(false);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);
  const normalizedSenderName = useMemo(
    () => senderName.trim() || '익명',
    [senderName],
  );

  useEffect(() => {
    isOpenRef.current = isOpen;
  }, [isOpen]);

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

        if (!isOpenRef.current && message.clientId !== clientId) {
          setUnreadCount((currentCount) => Math.min(currentCount + 1, 99));
        }
      },
    });

    connectionRef.current = connection;

    return () => {
      connection.disconnect();
      connectionRef.current = null;
    };
  }, [clientId, projectId]);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    messagesEndRef.current?.scrollIntoView({ block: 'end' });
  }, [isOpen, messages]);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextMessage = draft.trim();

    if (!nextMessage) {
      return;
    }

    connectionRef.current?.sendMessage(normalizedSenderName, nextMessage);
    setDraft('');
  };

  const handleChatToggle = () => {
    if (!isOpen) {
      setUnreadCount(0);
    }

    setIsOpen((currentIsOpen) => !currentIsOpen);
  };

  return (
    <div className="fixed right-4 bottom-4 z-40 flex max-w-[calc(100vw-2rem)] flex-col items-end">
      {isOpen ? (
        <section className="mb-3 flex h-[min(32rem,calc(100dvh-6rem))] w-[min(24rem,calc(100vw-2rem))] min-w-0 flex-col overflow-hidden rounded-xl border border-[var(--ws-border)] bg-[linear-gradient(180deg,rgba(37,37,38,0.98)_0%,rgba(20,20,20,0.99)_100%)] shadow-[0_22px_70px_rgba(0,0,0,0.48)]">
          <div className="flex items-center justify-between border-b border-[var(--ws-border)] bg-[rgba(18,18,18,0.92)] px-3 py-2">
            <div className="rounded-lg bg-[#1f1f1f] px-3 py-2 text-sm font-semibold text-white">
              Team Chat
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs text-[var(--ws-muted)]">Live</span>
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                className="inline-flex h-8 w-8 items-center justify-center rounded-md text-[#9da1a6] transition hover:bg-[#2a2d2e] hover:text-white"
                aria-label="팀 채팅 닫기"
              >
                x
              </button>
            </div>
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
                <div ref={messagesEndRef} />
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
      ) : null}

      <button
        type="button"
        onClick={handleChatToggle}
        className="relative inline-flex h-12 w-12 items-center justify-center rounded-full border border-[#3c3c3c] bg-[#0e639c] text-white shadow-[0_14px_34px_rgba(0,0,0,0.42)] transition hover:bg-[#1177bb] focus:ring-2 focus:ring-[#4fc1ff]/70 focus:outline-none"
        aria-label={isOpen ? '팀 채팅 닫기' : '팀 채팅 열기'}
      >
        <svg
          className="h-5 w-5"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M4.75 5.75A2.75 2.75 0 0 1 7.5 3h9A2.75 2.75 0 0 1 19.25 5.75v6.5A2.75 2.75 0 0 1 16.5 15H11l-4.75 4v-4A2.75 2.75 0 0 1 3.5 12.25v-6.5Z" />
          <path d="M8 7.75h8" />
          <path d="M8 11h5.5" />
        </svg>
        {unreadCount > 0 ? (
          <span className="absolute -top-1 -right-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-[#e51400] px-1.5 text-[11px] leading-none font-bold text-white ring-2 ring-[#111315]">
            {unreadCount}
          </span>
        ) : null}
      </button>
    </div>
  );
};
