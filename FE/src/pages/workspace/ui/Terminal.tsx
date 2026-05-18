import { useState } from 'react';
import type { TerminalSession } from './workspaceData';
import { TerminalIcon } from './WorkspaceIcons';

type TerminalProps = {
  sessions: TerminalSession[];
};

export const Terminal = ({ sessions }: TerminalProps) => {
  const [activeSessionId, setActiveSessionId] = useState(sessions[0]?.id ?? '');

  const activeSession =
    sessions.find((session) => session.id === activeSessionId) ?? sessions[0];

  return (
    <section className="flex h-[17.5rem] min-h-[17.5rem] flex-col overflow-hidden rounded-[24px] border border-[var(--ws-border)] bg-[linear-gradient(180deg,rgba(37,37,38,0.96)_0%,rgba(20,20,20,0.98)_100%)] shadow-[0_18px_40px_rgba(0,0,0,0.26)]">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[var(--ws-border)] bg-[rgba(18,18,18,0.88)] px-3 py-2">
        <div className="inline-flex items-center gap-2 rounded-lg bg-[#1f1f1f] px-3 py-2 text-sm text-white">
          <TerminalIcon className="h-4 w-4" />
          <span>Terminal</span>
        </div>
        <div className="text-[11px] text-[var(--ws-muted)]">
          shell sessions ready · 1 running task
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-2 border-b border-[var(--ws-border)] bg-[#1f1f1f] px-3 py-2">
        {sessions.map((session) => {
          const isActive = session.id === activeSession?.id;

          return (
            <button
              key={session.id}
              type="button"
              onClick={() => setActiveSessionId(session.id)}
              className={`inline-flex items-center gap-2 rounded-full border px-3 py-1.5 text-[12px] transition ${
                isActive
                  ? 'border-[#3c4858] bg-[#2a2d2e] text-white'
                  : 'border-[var(--ws-border)] text-[var(--ws-muted)] hover:border-[#3c4858] hover:text-[var(--ws-text)]'
              }`}
            >
              <span>{session.label}</span>
              <span
                className={`h-2 w-2 rounded-full ${
                  session.status === 'running' ? 'bg-[#73c991]' : 'bg-[#6e7681]'
                }`}
              />
            </button>
          );
        })}
      </div>

      <div className="min-h-0 flex-1 overflow-auto px-4 py-4 font-mono text-[13px] leading-7 text-[#d4d4d4]">
        {activeSession ? (
          <>
            <div className="mb-3 flex flex-wrap items-center gap-3 text-[11px] text-[#8b949e]">
              <span>{activeSession.cwd}</span>
              <span>shell: zsh</span>
              <span>
                status:{' '}
                {activeSession.status === 'running' ? 'running' : 'idle'}
              </span>
            </div>
            <div className="space-y-0.5">
              {activeSession.lines.map((output, index) => (
                <p
                  key={`${activeSession.id}-${index + 1}`}
                  className="whitespace-pre-wrap"
                >
                  {output || ' '}
                </p>
              ))}
            </div>
          </>
        ) : null}
      </div>
    </section>
  );
};
