/* eslint-disable no-unused-vars */

import type { PresenceUser, WorkspaceTab } from '../model';
import { FileIcon, PinIcon } from './WorkspaceIcons';

type EditorProps = {
  activeTabId: string;
  collaborators: PresenceUser[];
  onTabChange: (tabId: string) => void;
  tabs: WorkspaceTab[];
};

const tokenToneClassName = {
  text: 'text-[#d4d4d4]',
  keyword: 'text-[#c586c0]',
  string: 'text-[#ce9178]',
  comment: 'text-[#6a9955]',
  function: 'text-[#dcdcaa]',
  type: 'text-[#4ec9b0]',
  property: 'text-[#9cdcfe]',
  tag: 'text-[#569cd6]',
  accent: 'text-[#b5cea8]',
} as const;

export const Editor = ({
  activeTabId,
  collaborators,
  onTabChange,
  tabs,
}: EditorProps) => {
  const activeTab = tabs.find((tab) => tab.id === activeTabId) ?? tabs[0];

  if (!activeTab) {
    return null;
  }

  return (
    <section className="flex min-h-[420px] flex-col overflow-hidden rounded-[24px] border border-[var(--ws-border)] bg-[linear-gradient(180deg,rgba(37,37,38,0.96)_0%,rgba(24,24,24,0.98)_100%)] shadow-[0_18px_40px_rgba(0,0,0,0.26)]">
      <div className="flex items-center overflow-x-auto border-b border-[var(--ws-border)] bg-[rgba(18,18,18,0.85)]">
        {tabs.map((tab) => {
          const isActive = tab.id === activeTab.id;

          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => onTabChange(tab.id)}
              className={`group relative flex shrink-0 items-center gap-2 border-r border-[var(--ws-border)] px-4 py-3 text-sm transition ${
                isActive
                  ? 'bg-[#1f1f1f] text-white'
                  : 'bg-transparent text-[var(--ws-muted)] hover:bg-[#202224] hover:text-[var(--ws-text)]'
              }`}
            >
              <FileIcon className="h-4 w-4 shrink-0 text-[#9cdcfe]" />
              <span className="max-w-44 truncate">{tab.label}</span>
              {tab.pinned ? (
                <PinIcon className="h-3.5 w-3.5 text-[var(--ws-muted)]" />
              ) : null}
              {tab.dirty ? (
                <span className="h-2 w-2 rounded-full bg-[#e2c08d]" />
              ) : null}
            </button>
          );
        })}
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[var(--ws-border)] bg-[#1f1f1f] px-4 py-2 text-[11px] text-[var(--ws-muted)]">
        <div className="min-w-0">
          <p className="truncate">
            src / {activeTab.path.replace(/^src\//, '')}
          </p>
          <p className="mt-1 truncate text-[#6e7681]">
            {activeTab.description}
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <span className="rounded-full border border-[#3c3c3c] bg-[#181818] px-2 py-1 text-[#9cdcfe]">
            {activeTab.language.toUpperCase()}
          </span>
          <span className="rounded-full border border-[#3c3c3c] bg-[#181818] px-2 py-1 text-[#73c991]">
            {collaborators.length} live
          </span>
        </div>
      </div>

      <div className="min-h-0 flex-1 overflow-auto bg-[linear-gradient(180deg,rgba(255,255,255,0.02)_0%,rgba(255,255,255,0)_28%)]">
        <div className="min-w-[720px]">
          {activeTab.code.map((line, index) => {
            const lineNumber = index + 1;
            const lineCursors = activeTab.cursors.filter(
              (cursor) => cursor.line === lineNumber,
            );
            const isFocusedLine = activeTab.focusLine === lineNumber;

            return (
              <div
                key={`${activeTab.id}-${lineNumber}`}
                className={`relative flex min-h-7 items-start gap-4 px-4 font-mono text-[13px] leading-7 ${
                  isFocusedLine || lineCursors.length > 0
                    ? 'bg-[rgba(255,255,255,0.04)]'
                    : ''
                }`}
              >
                <span className="pt-[1px] text-right text-[12px] text-[#6e7681] select-none">
                  {String(lineNumber).padStart(2, '0')}
                </span>
                <span className="min-w-0 flex-1 whitespace-pre">
                  {line.tokens.length > 0 ? (
                    line.tokens.map((token, tokenIndex) => (
                      <span
                        key={`${activeTab.id}-${lineNumber}-${tokenIndex}`}
                        className={
                          token.tone
                            ? tokenToneClassName[token.tone]
                            : tokenToneClassName.text
                        }
                      >
                        {token.text}
                      </span>
                    ))
                  ) : (
                    <span>&nbsp;</span>
                  )}
                </span>

                {line.marker ? (
                  <span
                    className={`mt-1 hidden rounded-full px-2 py-0.5 text-[10px] font-semibold lg:inline-flex ${
                      line.marker.tone === 'warning'
                        ? 'bg-[#d7ba7d]/20 text-[#d7ba7d]'
                        : 'bg-[#4fc1ff]/20 text-[#4fc1ff]'
                    }`}
                  >
                    {line.marker.label}
                  </span>
                ) : null}

                {lineCursors.map((cursor) => (
                  <span
                    key={`${activeTab.id}-${cursor.id}-${lineNumber}`}
                    className="absolute top-1/2 right-6 -translate-y-1/2 rounded-full px-2 py-0.5 text-[10px] font-semibold text-white shadow-lg"
                    style={{ backgroundColor: cursor.accent }}
                  >
                    {cursor.label}
                  </span>
                ))}
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};
