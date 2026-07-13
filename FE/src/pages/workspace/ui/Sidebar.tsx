/* eslint-disable no-unused-vars */

import { useState } from 'react';
import type {
  ActivityId,
  ActivityItem,
  PresenceUser,
  SettingSection,
  WorkspaceTab,
  WorkspaceTreeNode,
} from '../model';
import {
  ChevronDownIcon,
  ChevronRightIcon,
  ExplorerIcon,
  FileIcon,
  FolderIcon,
  SettingsIcon,
  UsersIcon,
} from './WorkspaceIcons';

type SidebarProps = {
  activities: ActivityItem[];
  activeActivity: ActivityId;
  activeFilePath: string;
  collaborators: PresenceUser[];
  fileTree: WorkspaceTreeNode[];
  onActivityChange: (activityId: ActivityId) => void;
  onSelectTab: (tabId: string) => void;
  projectLabel: string;
  settingsSections: SettingSection[];
  tabs: WorkspaceTab[];
};

const activityTitleMap: Record<
  ActivityId,
  { title: string; subtitle: string }
> = {
  explorer: {
    title: 'Explorer',
    subtitle: '열려 있는 파일과 프로젝트 트리를 빠르게 탐색합니다.',
  },
  collaboration: {
    title: 'Collaboration',
    subtitle: '현재 접속자와 편집 위치를 확인합니다.',
  },
  settings: {
    title: 'Settings',
    subtitle: '자동 저장, 포맷, 협업 권한을 조정합니다.',
  },
};

const renderActivityIcon = (id: ActivityId, className = 'h-5 w-5') => {
  switch (id) {
    case 'explorer':
      return <ExplorerIcon className={className} />;
    case 'collaboration':
      return <UsersIcon className={className} />;
    case 'settings':
      return <SettingsIcon className={className} />;
  }
};

const treeHasActiveFile = (
  node: WorkspaceTreeNode,
  activeFilePath: string,
): boolean => {
  if (node.path === activeFilePath) {
    return true;
  }

  return (
    node.children?.some((child) => treeHasActiveFile(child, activeFilePath)) ??
    false
  );
};

const matchTabByPath = (tabs: WorkspaceTab[], path: string) => {
  return tabs.find((tab) => tab.path === path);
};

type TreeNodeProps = {
  activeFilePath: string;
  depth: number;
  node: WorkspaceTreeNode;
  onSelectPath: (path: string) => void;
};

const TreeNodeRow = ({
  activeFilePath,
  depth,
  node,
  onSelectPath,
}: TreeNodeProps) => {
  const isActive = node.path === activeFilePath;
  const hasChildren = Boolean(node.children?.length);
  const isExpanded = hasChildren && treeHasActiveFile(node, activeFilePath);

  return (
    <div>
      <button
        type="button"
        onClick={() => onSelectPath(node.path)}
        className={`flex w-full items-center gap-2 rounded-lg px-2 py-1.5 text-left text-sm transition ${
          isActive
            ? 'bg-[rgba(14,99,156,0.22)] text-white'
            : 'text-[var(--ws-text)] hover:bg-[#2a2d2e]'
        }`}
        style={{ paddingLeft: `${depth * 14 + 8}px` }}
      >
        {hasChildren ? (
          isExpanded ? (
            <ChevronDownIcon className="h-3.5 w-3.5 shrink-0 text-[var(--ws-muted)]" />
          ) : (
            <ChevronRightIcon className="h-3.5 w-3.5 shrink-0 text-[var(--ws-muted)]" />
          )
        ) : (
          <span className="w-3.5 shrink-0" />
        )}
        {node.kind === 'folder' ? (
          <FolderIcon className="h-4 w-4 shrink-0 text-[#dcb67a]" />
        ) : (
          <FileIcon className="h-4 w-4 shrink-0 text-[#9cdcfe]" />
        )}
        <span className="truncate">{node.label}</span>
      </button>

      {hasChildren && isExpanded ? (
        <div className="mt-0.5">
          {node.children?.map((child) => (
            <TreeNodeRow
              key={child.id}
              activeFilePath={activeFilePath}
              depth={depth + 1}
              node={child}
              onSelectPath={onSelectPath}
            />
          ))}
        </div>
      ) : null}
    </div>
  );
};

export const Sidebar = ({
  activities,
  activeActivity,
  activeFilePath,
  collaborators,
  fileTree,
  onActivityChange,
  onSelectTab,
  projectLabel,
  settingsSections,
  tabs,
}: SidebarProps) => {
  const [settingState, setSettingState] = useState<Record<string, boolean>>(
    () =>
      Object.fromEntries(
        settingsSections.flatMap((section) =>
          section.items.map((item) => [item.id, item.enabled]),
        ),
      ),
  );

  const panelMeta = activityTitleMap[activeActivity];

  const handleSelectPath = (path: string) => {
    const matchedTab = matchTabByPath(tabs, path);

    if (matchedTab) {
      onSelectTab(matchedTab.id);
    }
  };

  return (
    <section className="grid min-h-[400px] overflow-hidden rounded-[24px] border border-[var(--ws-border)] bg-[linear-gradient(180deg,rgba(37,37,38,0.95)_0%,rgba(28,28,28,0.96)_100%)] shadow-[0_18px_40px_rgba(0,0,0,0.26)] sm:grid-cols-[3.5rem_minmax(0,1fr)]">
      <nav className="flex items-center justify-between gap-2 overflow-x-auto border-b border-[var(--ws-border)] bg-[rgba(18,18,18,0.88)] px-2 py-2 sm:flex-col sm:justify-start sm:border-r sm:border-b-0 sm:px-1.5 sm:py-3">
        {activities.map((activity) => {
          const isActive = activity.id === activeActivity;

          return (
            <button
              key={activity.id}
              type="button"
              title={activity.label}
              onClick={() => onActivityChange(activity.id)}
              className={`group relative inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border transition ${
                isActive
                  ? 'border-[#0e639c] bg-[rgba(14,99,156,0.2)] text-white shadow-[inset_0_0_0_1px_rgba(79,193,255,0.18)]'
                  : 'border-transparent text-[var(--ws-muted)] hover:border-[var(--ws-border)] hover:bg-[#2a2d2e] hover:text-[var(--ws-text)]'
              }`}
            >
              {renderActivityIcon(activity.id)}
              <span className="pointer-events-none absolute top-1/2 left-full z-20 ml-3 hidden -translate-y-1/2 rounded-md border border-[var(--ws-border)] bg-[#1f1f1f] px-2 py-1 text-[11px] font-medium whitespace-nowrap text-[var(--ws-text)] opacity-0 shadow-lg transition xl:block xl:group-hover:opacity-100">
                {activity.label}
              </span>
            </button>
          );
        })}
      </nav>

      <aside className="flex min-h-0 flex-col">
        <div className="border-b border-[var(--ws-border)] px-4 py-3">
          <p className="text-[11px] font-semibold tracking-[0.18em] text-[var(--ws-muted)] uppercase">
            {panelMeta.title}
          </p>
          <p className="mt-1.5 text-sm leading-6 text-[var(--ws-text)]">
            {panelMeta.subtitle}
          </p>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto px-3 py-3 sm:px-4">
          {activeActivity === 'explorer' ? (
            <div className="space-y-4">
              <section>
                <div className="mb-2 flex items-center justify-between">
                  <p className="text-[11px] font-semibold tracking-[0.16em] text-[var(--ws-muted)] uppercase">
                    Open Editors
                  </p>
                  <span className="text-[11px] text-[var(--ws-muted)]">
                    {tabs.length} files
                  </span>
                </div>
                <div className="space-y-1">
                  {tabs.map((tab) => {
                    const isActive = tab.path === activeFilePath;

                    return (
                      <button
                        key={tab.id}
                        type="button"
                        onClick={() => onSelectTab(tab.id)}
                        className={`flex w-full items-center gap-2 rounded-lg px-2.5 py-2 text-left text-sm transition ${
                          isActive
                            ? 'bg-[rgba(14,99,156,0.22)] text-white'
                            : 'text-[var(--ws-text)] hover:bg-[#2a2d2e]'
                        }`}
                      >
                        <FileIcon className="h-4 w-4 shrink-0 text-[#9cdcfe]" />
                        <span className="truncate">{tab.label}</span>
                        {tab.dirty ? (
                          <span className="ml-auto h-2 w-2 rounded-full bg-[#e2c08d]" />
                        ) : null}
                      </button>
                    );
                  })}
                </div>
              </section>

              <section>
                <div className="mb-2 flex items-center justify-between">
                  <p className="text-[11px] font-semibold tracking-[0.16em] text-[var(--ws-muted)] uppercase">
                    Files
                  </p>
                  <span className="text-[11px] text-[var(--ws-muted)]">
                    {projectLabel}
                  </span>
                </div>
                <div className="space-y-0.5">
                  {fileTree.map((node) => (
                    <TreeNodeRow
                      key={node.id}
                      activeFilePath={activeFilePath}
                      depth={0}
                      node={node}
                      onSelectPath={handleSelectPath}
                    />
                  ))}
                </div>
              </section>
            </div>
          ) : null}

          {activeActivity === 'collaboration' ? (
            <div className="space-y-3">
              {collaborators.map((user) => (
                <article
                  key={user.id}
                  className="rounded-2xl border p-3"
                  style={{
                    borderColor: user.accent,
                    backgroundColor: user.accentSoft,
                  }}
                >
                  <div className="flex items-start justify-between gap-3">
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
                  <div className="mt-3 space-y-1.5 text-[12px] text-[var(--ws-text)]">
                    <p>{user.file}</p>
                    <p className="text-[var(--ws-muted)]">{user.location}</p>
                    <p className="text-[var(--ws-muted)]">
                      마지막 활동 {user.lastSeen}
                    </p>
                  </div>
                </article>
              ))}
            </div>
          ) : null}

          {activeActivity === 'settings' ? (
            <div className="space-y-4">
              {settingsSections.map((section) => (
                <section key={section.id}>
                  <p className="mb-2 text-[11px] font-semibold tracking-[0.16em] text-[var(--ws-muted)] uppercase">
                    {section.title}
                  </p>
                  <div className="space-y-2">
                    {section.items.map((item) => {
                      const isEnabled = settingState[item.id];

                      return (
                        <button
                          key={item.id}
                          type="button"
                          onClick={() =>
                            setSettingState((current) => ({
                              ...current,
                              [item.id]: !current[item.id],
                            }))
                          }
                          className="flex w-full items-center justify-between gap-3 rounded-2xl border border-[var(--ws-border)] bg-[#1f1f1f] px-3 py-3 text-left transition hover:border-[#3c4858]"
                        >
                          <div>
                            <p className="text-sm font-medium text-[var(--ws-text)]">
                              {item.label}
                            </p>
                            <p className="mt-1 text-[12px] text-[var(--ws-muted)]">
                              {item.description}
                            </p>
                          </div>
                          <span
                            className={`flex h-6 w-11 shrink-0 items-center rounded-full border px-1 transition ${
                              isEnabled
                                ? 'border-[#0e639c] bg-[#0e639c]'
                                : 'border-[#3c3c3c] bg-[#2a2d2e]'
                            }`}
                          >
                            <span
                              className={`h-4 w-4 rounded-full bg-white transition ${
                                isEnabled ? 'translate-x-5' : ''
                              }`}
                            />
                          </span>
                        </button>
                      );
                    })}
                  </div>
                </section>
              ))}
            </div>
          ) : null}
        </div>
      </aside>
    </section>
  );
};
