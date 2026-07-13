/* eslint-disable no-unused-vars */

import type { WorkspaceTab } from '../model';
import { FileIcon, PinIcon } from './WorkspaceIcons';
import MonacoEditorComponent from './MonacoEditor';

type EditorProps = {
  activeTabId: string;
  content: string;
  isContentLoading?: boolean;
  onTabChange: (tabId: string) => void;
  tabs: WorkspaceTab[];
};

export const Editor = ({
  activeTabId,
  content,
  isContentLoading = false,
  onTabChange,
  tabs,
}: EditorProps) => {
  const activeTab = tabs.find((tab) => tab.id === activeTabId) ?? tabs[0];

  if (!activeTab) {
    return (
      <section className="flex min-h-[420px] flex-col overflow-hidden rounded-[24px] border border-[var(--ws-border)] bg-[linear-gradient(180deg,rgba(37,37,38,0.96)_0%,rgba(24,24,24,0.98)_100%)] shadow-[0_18px_40px_rgba(0,0,0,0.26)]">
        <div className="flex min-h-0 flex-1 items-center justify-center p-6 text-sm text-[var(--ws-muted)]">
          열려 있는 파일이 없습니다.
        </div>
      </section>
    );
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

      <div className="min-h-0 flex-1 bg-[linear-gradient(180deg,rgba(255,255,255,0.02)_0%,rgba(255,255,255,0)_28%)] p-5">
        <div className="flex h-full min-h-[320px] items-center justify-center rounded-[20px] border border-dashed border-[rgba(255,255,255,0.08)] bg-[rgba(12,12,12,0.34)] px-6 py-10">
          {isContentLoading ? (
            <p className="text-sm text-[var(--ws-muted)]">
              파일 내용을 불러오는 중입니다.
            </p>
          ) : (
            <MonacoEditorComponent
              language={activeTab.language}
              value={content}
            />
          )}
        </div>
      </div>
    </section>
  );
};
