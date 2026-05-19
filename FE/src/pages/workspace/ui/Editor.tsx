/* eslint-disable no-unused-vars */

import type { PresenceUser, WorkspaceTab } from '../model';
import { FileIcon, PinIcon } from './WorkspaceIcons';

type EditorProps = {
  activeTabId: string;
  collaborators: PresenceUser[];
  onTabChange: (tabId: string) => void;
  tabs: WorkspaceTab[];
};

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

      <div className="min-h-0 flex-1 bg-[linear-gradient(180deg,rgba(255,255,255,0.02)_0%,rgba(255,255,255,0)_28%)] p-5">
        <div className="flex h-full min-h-[320px] items-center justify-center rounded-[20px] border border-dashed border-[rgba(255,255,255,0.08)] bg-[rgba(12,12,12,0.34)] px-6 py-10">
          <div className="max-w-md text-center">
            <p className="text-[11px] font-semibold tracking-[0.18em] text-[var(--ws-muted)] uppercase">
              Editor Surface
            </p>
            <h2 className="mt-3 text-lg font-semibold text-[var(--ws-text)]">
              {activeTab.label}
            </h2>
            <p className="mt-3 text-sm leading-6 text-[var(--ws-muted)]">
              정적 토큰 렌더러를 제거했습니다. 이 영역에 Monaco나 CodeMirror
              같은 실제 에디터 엔진을 마운트하면 됩니다.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
};
