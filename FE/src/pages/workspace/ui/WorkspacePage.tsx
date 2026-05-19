import { startTransition, type CSSProperties, useState } from 'react';
import { Editor } from './Editor';
import { Sidebar } from './Sidebar';
import { Terminal } from './Terminal';
import { WorkspaceHeader } from './WorkspaceHeader';
import {
  activityItems,
  editorTabs,
  presenceUsers,
  searchResults,
  settingSections,
  terminalSessions,
  workspaceProjects,
  workspaceTree,
  type ActivityId,
} from '../model';

// CSS 변수 정의
const workspaceTheme = {
  '--ws-bg': '#111315',
  '--ws-panel': '#252526',
  '--ws-panel-strong': '#1f1f1f',
  '--ws-border': '#313135',
  '--ws-text': '#d4d4d4',
  '--ws-muted': '#8b949e',
} as CSSProperties;

export const WorkspacePage = () => {
  const [activeProjectId, setActiveProjectId] = useState(
    workspaceProjects[0]?.id ?? '',
  );
  const [activeActivity, setActiveActivity] = useState<ActivityId>('explorer');
  const [activeTabId, setActiveTabId] = useState(
    editorTabs[1]?.id ?? editorTabs[0]?.id ?? '',
  );

  const activeProject =
    workspaceProjects.find((project) => project.id === activeProjectId) ??
    workspaceProjects[0];
  const activeTab =
    editorTabs.find((tab) => tab.id === activeTabId) ?? editorTabs[0];
  const dirtyCount = editorTabs.filter((tab) => tab.dirty).length;

  if (!activeProject || !activeTab) {
    return null;
  }

  return (
    <div
      className="relative h-[calc(100vh-2.75rem)] overflow-hidden bg-[var(--ws-bg)] text-[var(--ws-text)]"
      style={workspaceTheme}
    >
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(79,193,255,0.14),transparent_30%),radial-gradient(circle_at_bottom_right,rgba(14,99,156,0.18),transparent_34%)]" />
      <div className="pointer-events-none absolute inset-x-0 top-0 h-40 bg-[linear-gradient(180deg,rgba(255,255,255,0.04),transparent)]" />

      <div className="relative flex h-full flex-col">
        <WorkspaceHeader
          projects={workspaceProjects}
          activeProjectId={activeProjectId}
          dirtyCount={dirtyCount}
          onProjectChange={(projectId) =>
            startTransition(() => {
              setActiveProjectId(projectId);
            })
          }
          users={presenceUsers}
        />

        <div className="flex min-h-0 flex-1 flex-col gap-3 px-3 pb-3 lg:px-4 lg:pb-4">
          <div className="grid min-h-0 flex-1 gap-3 xl:grid-cols-[21rem_minmax(0,1fr)]">
            <Sidebar
              activities={activityItems}
              activeActivity={activeActivity}
              activeFilePath={activeTab.path}
              collaborators={presenceUsers}
              fileTree={workspaceTree}
              onActivityChange={(activityId) =>
                startTransition(() => {
                  setActiveActivity(activityId);
                })
              }
              onSelectTab={(tabId) =>
                startTransition(() => {
                  setActiveTabId(tabId);
                })
              }
              searchResults={searchResults}
              settingsSections={settingSections}
              tabs={editorTabs}
            />

            <Editor
              activeTabId={activeTabId}
              collaborators={presenceUsers}
              onTabChange={(tabId) =>
                startTransition(() => {
                  setActiveTabId(tabId);
                })
              }
              tabs={editorTabs}
            />
          </div>

          <Terminal sessions={terminalSessions} />
        </div>
      </div>
    </div>
  );
};
