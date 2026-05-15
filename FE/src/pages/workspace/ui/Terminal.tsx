/* eslint-disable no-unused-vars */

import { useState } from 'react';
import type {
  BottomPanelId,
  BottomPanelItem,
  PortItem,
  ProblemItem,
  TerminalSession,
} from './workspaceData';
import {
  InfoIcon,
  OutputIcon,
  PortsIcon,
  TerminalIcon,
  WarningIcon,
} from './WorkspaceIcons';

type TerminalProps = {
  activePanel: BottomPanelId;
  onPanelChange: (panelId: BottomPanelId) => void;
  outputLines: string[];
  panels: BottomPanelItem[];
  ports: PortItem[];
  problems: ProblemItem[];
  sessions: TerminalSession[];
};

const renderPanelIcon = (panelId: BottomPanelId) => {
  switch (panelId) {
    case 'terminal':
      return <TerminalIcon className="h-4 w-4" />;
    case 'problems':
      return <WarningIcon className="h-4 w-4" />;
    case 'output':
      return <OutputIcon className="h-4 w-4" />;
    case 'ports':
      return <PortsIcon className="h-4 w-4" />;
  }
};

export const Terminal = ({
  activePanel,
  onPanelChange,
  outputLines,
  panels,
  ports,
  problems,
  sessions,
}: TerminalProps) => {
  const [activeSessionId, setActiveSessionId] = useState(sessions[0]?.id ?? '');

  const activeSession =
    sessions.find((session) => session.id === activeSessionId) ?? sessions[0];

  return (
    <section className="flex h-[17.5rem] min-h-[17.5rem] flex-col overflow-hidden rounded-[24px] border border-[var(--ws-border)] bg-[linear-gradient(180deg,rgba(37,37,38,0.96)_0%,rgba(20,20,20,0.98)_100%)] shadow-[0_18px_40px_rgba(0,0,0,0.26)]">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[var(--ws-border)] bg-[rgba(18,18,18,0.88)] px-3 py-2">
        <div className="flex flex-wrap items-center gap-1.5">
          {panels.map((panel) => {
            const isActive = panel.id === activePanel;

            return (
              <button
                key={panel.id}
                type="button"
                onClick={() => onPanelChange(panel.id)}
                className={`inline-flex items-center gap-2 rounded-lg px-3 py-2 text-sm transition ${
                  isActive
                    ? 'bg-[#1f1f1f] text-white'
                    : 'text-[var(--ws-muted)] hover:bg-[#2a2d2e] hover:text-[var(--ws-text)]'
                }`}
              >
                {renderPanelIcon(panel.id)}
                <span>{panel.label}</span>
              </button>
            );
          })}
        </div>
        <div className="text-[11px] text-[var(--ws-muted)]">
          preview container healthy · 1 running task
        </div>
      </div>

      {activePanel === 'terminal' ? (
        <>
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
                      session.status === 'running'
                        ? 'bg-[#73c991]'
                        : 'bg-[#6e7681]'
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
        </>
      ) : null}

      {activePanel === 'problems' ? (
        <div className="min-h-0 flex-1 overflow-auto px-4 py-4">
          <div className="space-y-3">
            {problems.map((problem) => (
              <article
                key={problem.id}
                className="rounded-2xl border border-[var(--ws-border)] bg-[#1f1f1f] p-3"
              >
                <div className="flex items-start gap-3">
                  <span
                    className={`mt-0.5 ${
                      problem.severity === 'warning'
                        ? 'text-[#d7ba7d]'
                        : problem.severity === 'error'
                          ? 'text-[#f14c4c]'
                          : 'text-[#4fc1ff]'
                    }`}
                  >
                    {problem.severity === 'warning' ? (
                      <WarningIcon className="h-4 w-4" />
                    ) : (
                      <InfoIcon className="h-4 w-4" />
                    )}
                  </span>
                  <div>
                    <p className="text-sm font-medium text-[var(--ws-text)]">
                      {problem.message}
                    </p>
                    <p className="mt-1 text-[12px] text-[var(--ws-muted)]">
                      {problem.file}:{problem.line}
                    </p>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </div>
      ) : null}

      {activePanel === 'output' ? (
        <div className="min-h-0 flex-1 overflow-auto px-4 py-4 font-mono text-[13px] leading-7 text-[#d4d4d4]">
          {outputLines.map((line, index) => (
            <p key={`output-${index + 1}`}>{line}</p>
          ))}
        </div>
      ) : null}

      {activePanel === 'ports' ? (
        <div className="min-h-0 flex-1 overflow-auto px-4 py-4">
          <div className="grid gap-3 md:grid-cols-2">
            {ports.map((port) => (
              <article
                key={port.port}
                className="rounded-2xl border border-[var(--ws-border)] bg-[#1f1f1f] p-4"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-sm font-semibold text-[var(--ws-text)]">
                      {port.label}
                    </p>
                    <p className="mt-1 text-[12px] text-[var(--ws-muted)]">
                      {port.visibility}
                    </p>
                  </div>
                  <span className="rounded-full bg-[#73c991]/20 px-2 py-0.5 text-[11px] font-semibold text-[#73c991]">
                    {port.status}
                  </span>
                </div>
                <div className="mt-3 rounded-xl border border-[#2d2d30] bg-[#181818] px-3 py-2 font-mono text-[12px] text-[#9cdcfe]">
                  localhost:{port.port}
                </div>
              </article>
            ))}
          </div>
        </div>
      ) : null}
    </section>
  );
};
