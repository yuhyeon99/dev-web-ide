/* eslint-disable no-unused-vars */

import { useState } from 'react';
import type { PresenceUser, WorkspaceProject } from '../model';
import { ChevronDownIcon, SaveIcon } from './WorkspaceIcons';

type WorkspaceHeaderProps = {
  projects: WorkspaceProject[];
  activeProjectId: string;
  dirtyCount: number;
  isSaving?: boolean;
  onSave: () => void;
  onProjectChange: (projectId: string) => void;
  saveMessage?: string | null;
  users: PresenceUser[];
};

const initials = (name: string) => {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((value) => value[0]?.toUpperCase() ?? '')
    .join('');
};

export const WorkspaceHeader = ({
  projects,
  activeProjectId,
  dirtyCount,
  isSaving = false,
  onSave,
  onProjectChange,
  saveMessage,
  users,
}: WorkspaceHeaderProps) => {
  const [isProjectMenuOpen, setIsProjectMenuOpen] = useState(false);

  const activeProject =
    projects.find((project) => project.id === activeProjectId) ?? projects[0];

  if (!activeProject) {
    return null;
  }

  return (
    <div className="px-3 py-3 lg:px-4 lg:py-4">
      <section className="rounded-[24px] border border-[var(--ws-border)] bg-[linear-gradient(180deg,rgba(37,37,38,0.96)_0%,rgba(30,30,30,0.94)_100%)] px-4 py-3 shadow-[0_18px_50px_rgba(0,0,0,0.28)] backdrop-blur">
        <div className="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
          <div className="flex flex-wrap items-center gap-2.5">
            <div className="relative">
              <button
                type="button"
                onClick={() => setIsProjectMenuOpen((open) => !open)}
                className="inline-flex min-w-[240px] items-center justify-between gap-4 rounded-2xl border border-[var(--ws-border)] bg-[rgba(15,15,15,0.75)] px-3.5 py-3 text-left transition hover:border-[#3c4858] hover:bg-[rgba(26,26,26,0.95)]"
              >
                <div>
                  <p className="text-sm font-semibold text-[var(--ws-text)]">
                    {activeProject.label}
                  </p>
                  <p className="mt-1 text-[11px] text-[var(--ws-muted)]">
                    {activeProject.subtitle}
                  </p>
                </div>
                <ChevronDownIcon
                  className={`h-4 w-4 text-[var(--ws-muted)] transition ${
                    isProjectMenuOpen ? 'rotate-180' : ''
                  }`}
                />
              </button>

              {isProjectMenuOpen ? (
                <div className="absolute left-0 z-20 mt-2 w-full overflow-hidden rounded-2xl border border-[var(--ws-border)] bg-[#1f1f1f] shadow-[0_20px_40px_rgba(0,0,0,0.35)]">
                  <div className="border-b border-[var(--ws-border)] px-3 py-2 text-[11px] font-semibold tracking-[0.18em] text-[var(--ws-muted)] uppercase">
                    Recent Projects
                  </div>
                  <div className="p-2">
                    {projects.map((project) => {
                      const isActive = project.id === activeProjectId;

                      return (
                        <button
                          key={project.id}
                          type="button"
                          onClick={() => {
                            onProjectChange(project.id);
                            setIsProjectMenuOpen(false);
                          }}
                          className={`flex w-full items-start justify-between rounded-xl px-3 py-2.5 text-left transition ${
                            isActive
                              ? 'bg-[rgba(14,99,156,0.2)] text-white'
                              : 'text-[var(--ws-text)] hover:bg-[#2a2d2e]'
                          }`}
                        >
                          <div>
                            <p className="text-sm font-medium">
                              {project.label}
                            </p>
                            <p className="mt-1 text-[11px] text-[var(--ws-muted)]">
                              {project.subtitle}
                            </p>
                          </div>
                          <div className="text-right text-[11px] text-[var(--ws-muted)]">
                            <p>최근 열람</p>
                            <p className="mt-1">{project.lastOpened}</p>
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>
              ) : null}
            </div>

            <button
              type="button"
              disabled={dirtyCount === 0 || isSaving}
              onClick={onSave}
              className="inline-flex items-center gap-2 rounded-xl border border-[var(--ws-border)] bg-[#2a2d2e] px-3.5 py-2.5 text-sm font-medium text-[var(--ws-text)] transition hover:border-[#3c4858] hover:bg-[#303336]"
            >
              <SaveIcon className="h-4 w-4" />
              <span>{isSaving ? 'Saving' : 'Save'}</span>
              {dirtyCount > 0 ? (
                <span className="rounded-full bg-[#3b82f6]/20 px-2 py-0.5 text-[11px] font-semibold text-[#9cdcfe]">
                  {dirtyCount}
                </span>
              ) : null}
            </button>

            {saveMessage ? (
              <span className="text-xs text-[var(--ws-muted)]">
                {saveMessage}
              </span>
            ) : null}
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <span className="mr-1 text-[11px] font-semibold tracking-[0.18em] text-[var(--ws-muted)] uppercase">
              Presence
            </span>
            {users.length > 0 ? (
              users.map((user) => (
                <div key={user.id} className="group relative">
                  <div
                    className="inline-flex items-center gap-2 rounded-full border px-2.5 py-1.5"
                    style={{
                      borderColor: user.accent,
                      backgroundColor: user.accentSoft,
                    }}
                  >
                    <span
                      className="h-2.5 w-2.5 rounded-full"
                      style={{ backgroundColor: user.accent }}
                    />
                    <span className="text-sm font-medium text-[var(--ws-text)]">
                      {user.name}
                    </span>
                    <span className="flex h-6 w-6 items-center justify-center rounded-full bg-[rgba(255,255,255,0.08)] text-[10px] font-semibold text-white">
                      {initials(user.name)}
                    </span>
                  </div>

                  <div className="pointer-events-none absolute right-0 z-20 mt-2 hidden w-64 rounded-2xl border border-[var(--ws-border)] bg-[#1f1f1f] p-3 opacity-0 shadow-[0_18px_36px_rgba(0,0,0,0.35)] transition md:block md:translate-y-1 md:group-hover:translate-y-0 md:group-hover:opacity-100">
                    <div className="flex items-center justify-between gap-3">
                      <div>
                        <p className="text-sm font-semibold text-[var(--ws-text)]">
                          {user.name}
                        </p>
                        <p className="mt-1 text-[11px] text-[var(--ws-muted)]">
                          {user.role} · {user.status}
                        </p>
                      </div>
                      <span
                        className="h-2.5 w-2.5 rounded-full"
                        style={{ backgroundColor: user.accent }}
                      />
                    </div>
                    <dl className="mt-3 space-y-2 text-[11px] text-[var(--ws-muted)]">
                      <div className="flex items-start justify-between gap-3">
                        <dt>현재 파일</dt>
                        <dd className="text-right text-[var(--ws-text)]">
                          {user.file}
                        </dd>
                      </div>
                      <div className="flex items-start justify-between gap-3">
                        <dt>현재 위치</dt>
                        <dd className="text-right text-[var(--ws-text)]">
                          {user.location}
                        </dd>
                      </div>
                      <div className="flex items-start justify-between gap-3">
                        <dt>마지막 활동</dt>
                        <dd className="text-right text-[var(--ws-text)]">
                          {user.lastSeen}
                        </dd>
                      </div>
                    </dl>
                  </div>
                </div>
              ))
            ) : (
              <span className="rounded-full border border-[#3c3c3c] bg-[#1e1e1e] px-3 py-1.5 text-xs text-[var(--ws-muted)]">
                접속 중인 사용자가 없습니다.
              </span>
            )}
          </div>
        </div>
      </section>
    </div>
  );
};
