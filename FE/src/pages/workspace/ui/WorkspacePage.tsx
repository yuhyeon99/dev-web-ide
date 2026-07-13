import { startTransition, type CSSProperties, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate, useSearchParams } from 'react-router';

import {
  createProjectFile,
  getMyProjects,
  getProjectDetail,
  getProjectFileContent,
  getProjectFileTree,
  runProject,
  saveProjectFiles,
} from '@/shared/api/projects';
import { resolveProjectApiSession } from '@/shared/api/session';
import type {
  ProjectFileContentResponse,
  ProjectFileTreeResponse,
  ProjectSummaryResponse,
} from '@/shared/api/types';

import { Editor } from './Editor';
import { Sidebar } from './Sidebar';
import { Terminal } from './Terminal';
import { WorkspaceHeader } from './WorkspaceHeader';
import {
  type ActivityId,
  type ActivityItem,
  type WorkspaceTab,
  type WorkspaceTreeNode,
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

const workspaceActivityItems: ActivityItem[] = [
  {
    id: 'explorer',
    label: 'Explorer',
    description: '프로젝트 파일 탐색',
  },
];

const getLanguageByFileName = (fileName: string) => {
  if (fileName.endsWith('.tsx') || fileName.endsWith('.jsx')) {
    return 'typescript';
  }

  if (fileName.endsWith('.ts')) {
    return 'typescript';
  }

  if (fileName.endsWith('.css')) {
    return 'css';
  }

  if (fileName.endsWith('.json')) {
    return 'json';
  }

  if (fileName.endsWith('.md')) {
    return 'markdown';
  }

  if (fileName.endsWith('.py')) {
    return 'python';
  }

  if (fileName.endsWith('.java')) {
    return 'java';
  }

  if (fileName.endsWith('.cpp') || fileName.endsWith('.cc')) {
    return 'cpp';
  }

  return 'plaintext';
};

const toWorkspaceTreeNode = (
  fileNode: ProjectFileTreeResponse,
): WorkspaceTreeNode => {
  return {
    id: String(fileNode.id),
    label: fileNode.name,
    kind: fileNode.fileType === 'DIRECTORY' ? 'folder' : 'file',
    path: fileNode.path,
    children: fileNode.children.map(toWorkspaceTreeNode),
  };
};

const flattenFiles = (fileNodes: ProjectFileTreeResponse[]) => {
  const files: ProjectFileTreeResponse[] = [];

  const visit = (node: ProjectFileTreeResponse) => {
    if (node.fileType === 'FILE') {
      files.push(node);
      return;
    }

    node.children.forEach(visit);
  };

  fileNodes.forEach(visit);

  return files;
};

const toWorkspaceProject = (project: ProjectSummaryResponse) => {
  return {
    id: String(project.id),
    label: project.name,
    subtitle: project.runtimeDisplayName,
    lastOpened: '최근 열람',
  };
};

type FileDraft = {
  content: string;
  savedContent: string;
};

type SavedDraft = {
  content: string;
  fileId: number;
};

export const WorkspacePage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const projectId = Number(searchParams.get('projectId'));
  const [activeActivity, setActiveActivity] = useState<ActivityId>('explorer');
  const [activeTabId, setActiveTabId] = useState('');
  const [fileDrafts, setFileDrafts] = useState<Record<string, FileDraft>>({});
  const [saveMessage, setSaveMessage] = useState<string | null>(null);
  const [fileCreateMessage, setFileCreateMessage] = useState<string | null>(
    null,
  );
  const [terminalLines, setTerminalLines] = useState<string[]>([]);
  const [terminalStatus, setTerminalStatus] = useState('status: idle');
  const projectsQuery = useQuery({
    queryKey: ['workspace-projects'],
    queryFn: async () => {
      const projectSession = await resolveProjectApiSession();

      return getMyProjects(projectSession.accessToken);
    },
  });
  const projectDetailQuery = useQuery({
    queryKey: ['project-detail', projectId],
    queryFn: async () => {
      const projectSession = await resolveProjectApiSession(
        undefined,
        projectId,
      );

      return getProjectDetail(projectSession.accessToken, projectId);
    },
    enabled: Number.isFinite(projectId) && projectId > 0,
  });
  const fileTreeQuery = useQuery({
    queryKey: ['project-file-tree', projectId],
    queryFn: () => getProjectFileTree(projectId),
    enabled: Number.isFinite(projectId) && projectId > 0,
  });
  const fileNodes = useMemo(
    () => fileTreeQuery.data ?? [],
    [fileTreeQuery.data],
  );
  const rootDirectory = useMemo(() => {
    const directories = fileNodes.filter(
      (fileNode) => fileNode.fileType === 'DIRECTORY',
    );

    return (
      directories.find((fileNode) => fileNode.parentFileId === null) ??
      directories[0] ??
      null
    );
  }, [fileNodes]);
  const files = useMemo(() => flattenFiles(fileNodes), [fileNodes]);
  const editorTabs = useMemo<WorkspaceTab[]>(
    () =>
      files.map((file, index) => ({
        dirty:
          fileDrafts[String(file.id)] !== undefined &&
          fileDrafts[String(file.id)].content !==
            fileDrafts[String(file.id)].savedContent,
        id: String(file.id),
        label: file.name,
        path: file.path,
        language: getLanguageByFileName(file.name),
        pinned: index === 0,
      })),
    [fileDrafts, files],
  );
  const effectiveActiveTabId = editorTabs.some((tab) => tab.id === activeTabId)
    ? activeTabId
    : (editorTabs[0]?.id ?? '');
  const activeTab =
    editorTabs.find((tab) => tab.id === effectiveActiveTabId) ?? editorTabs[0];
  const activeFileId = activeTab ? Number(activeTab.id) : null;
  const fileContentQuery = useQuery({
    queryKey: ['project-file-content', projectId, activeFileId],
    queryFn: () => getProjectFileContent(projectId, activeFileId ?? 0),
    enabled:
      Number.isFinite(projectId) && projectId > 0 && activeFileId !== null,
  });
  const activeFileContent =
    activeFileId !== null
      ? (fileDrafts[String(activeFileId)]?.content ??
        fileContentQuery.data?.content ??
        '')
      : '';
  const workspaceProjects =
    projectsQuery.data?.map(toWorkspaceProject) ??
    (projectDetailQuery.data
      ? [
          {
            id: String(projectDetailQuery.data.id),
            label: projectDetailQuery.data.name,
            subtitle: projectDetailQuery.data.runtime.displayName,
            lastOpened: '현재 프로젝트',
          },
        ]
      : []);
  const activeProject =
    workspaceProjects.find((project) => project.id === String(projectId)) ??
    workspaceProjects[0];
  const workspaceTree = fileNodes.map(toWorkspaceTreeNode);
  const dirtyCount = editorTabs.filter((tab) => tab.dirty).length;
  const dirtyDrafts = useMemo<SavedDraft[]>(
    () =>
      Object.entries(fileDrafts)
        .filter(([, draft]) => draft.content !== draft.savedContent)
        .map(([fileId, draft]) => ({
          content: draft.content,
          fileId: Number(fileId),
        })),
    [fileDrafts],
  );
  const saveFilesMutation = useMutation({
    mutationFn: async () => {
      const projectSession = await resolveProjectApiSession(
        undefined,
        projectId,
      );

      await saveProjectFiles(projectId, {
        files: dirtyDrafts.map((draft) => ({
          content: draft.content,
          projectFileId: draft.fileId,
        })),
        guestSessionId: projectSession.guestSessionId,
        userId: projectSession.userId,
      });

      return dirtyDrafts;
    },
    onError: () => {
      setSaveMessage('저장 실패');
    },
    onSuccess: (savedDrafts) => {
      setFileDrafts((currentDrafts) => {
        const nextDrafts = { ...currentDrafts };

        savedDrafts.forEach((savedDraft) => {
          const currentDraft = currentDrafts[String(savedDraft.fileId)];

          nextDrafts[String(savedDraft.fileId)] = {
            content: currentDraft?.content ?? savedDraft.content,
            savedContent: savedDraft.content,
          };
          queryClient.setQueryData<ProjectFileContentResponse>(
            ['project-file-content', projectId, savedDraft.fileId],
            (currentContent) =>
              currentContent
                ? {
                    ...currentContent,
                    content: savedDraft.content,
                  }
                : currentContent,
          );
        });

        return nextDrafts;
      });
      setSaveMessage('저장됨');
    },
  });
  const createFileMutation = useMutation({
    mutationFn: async (request: {
      fileType: 'FILE' | 'DIRECTORY';
      name: string;
    }) => {
      if (!rootDirectory) {
        throw new Error('루트 디렉터리를 찾을 수 없습니다.');
      }

      return createProjectFile(projectId, {
        parentFileId: rootDirectory.id,
        name: request.name,
        fileType: request.fileType,
      });
    },
    onError: () => {
      setFileCreateMessage('파일을 생성하지 못했습니다.');
    },
    onSuccess: async (createdFile) => {
      setFileCreateMessage(null);
      await queryClient.invalidateQueries({
        queryKey: ['project-file-tree', projectId],
      });

      if (createdFile.fileType === 'FILE') {
        setActiveTabId(String(createdFile.projectFileId));
      }
    },
  });
  const runProjectMutation = useMutation({
    mutationFn: async () => {
      const projectSession = await resolveProjectApiSession(
        undefined,
        projectId,
      );

      return runProject(projectId, {
        guestSessionId: projectSession.guestSessionId,
        userId: projectSession.userId,
      });
    },
    onMutate: () => {
      setTerminalStatus('status: starting');
      setTerminalLines(['$ run project', '프로젝트 실행 세션을 시작합니다.']);
    },
    onError: () => {
      setTerminalStatus('status: failed');
      setTerminalLines((currentLines) => [
        ...currentLines,
        '프로젝트 실행에 실패했습니다.',
      ]);
    },
    onSuccess: (response) => {
      setTerminalStatus(`status: ${response.container.status.toLowerCase()}`);
      setTerminalLines([
        '$ run project',
        `workspace session #${response.workspaceSessionId}`,
        `container #${response.containerInstanceId} ${response.container.status}`,
        `runtime ${response.runtimeDisplayName} (${response.runtimeLanguage})`,
        `image ${response.dockerImage}`,
        `efs ${response.efsMountPath}`,
      ]);
    },
  });

  const handleActiveFileContentChange = (nextContent: string) => {
    if (activeFileId === null) {
      return;
    }

    const fileId = String(activeFileId);

    setFileDrafts((currentDrafts) => {
      const currentDraft = currentDrafts[fileId];

      return {
        ...currentDrafts,
        [fileId]: {
          content: nextContent,
          savedContent:
            currentDraft?.savedContent ?? fileContentQuery.data?.content ?? '',
        },
      };
    });
    setSaveMessage(null);
  };

  const handleSave = () => {
    if (dirtyDrafts.length === 0 || saveFilesMutation.isPending) {
      return;
    }

    saveFilesMutation.mutate();
  };

  const handleCreateFile = (fileType: 'FILE' | 'DIRECTORY') => {
    const defaultName = fileType === 'FILE' ? 'new-file.txt' : 'new-folder';
    const name = window.prompt(
      fileType === 'FILE' ? '새 파일 이름' : '새 폴더 이름',
      defaultName,
    );
    const trimmedName = name?.trim();

    if (!trimmedName || createFileMutation.isPending) {
      return;
    }

    createFileMutation.mutate({ fileType, name: trimmedName });
  };

  if (!Number.isFinite(projectId) || projectId <= 0) {
    return (
      <div className="flex h-[calc(100vh-2.75rem)] items-center justify-center bg-[#1e1e1e] px-4 text-[#d4d4d4]">
        <div className="rounded-xl border border-[#313131] bg-[#252526] p-6 text-center">
          <p className="text-sm text-[#858585]">열 프로젝트를 선택하세요.</p>
          <Link
            to="/"
            className="mt-4 inline-flex rounded-md bg-[#0e639c] px-4 py-2 text-sm font-semibold text-white"
          >
            대시보드로 이동
          </Link>
        </div>
      </div>
    );
  }

  if (projectDetailQuery.isLoading || fileTreeQuery.isLoading) {
    return (
      <div className="flex h-[calc(100vh-2.75rem)] items-center justify-center bg-[#111315] text-sm text-[#8b949e]">
        워크스페이스를 불러오는 중입니다.
      </div>
    );
  }

  if (!activeProject || projectDetailQuery.isError || fileTreeQuery.isError) {
    return (
      <div className="flex h-[calc(100vh-2.75rem)] items-center justify-center bg-[#1e1e1e] px-4 text-[#d4d4d4]">
        <div className="rounded-xl border border-[#313131] bg-[#252526] p-6 text-center">
          <p className="text-sm text-[#858585]">
            프로젝트를 불러오지 못했습니다.
          </p>
          <Link
            to="/"
            className="mt-4 inline-flex rounded-md bg-[#0e639c] px-4 py-2 text-sm font-semibold text-white"
          >
            대시보드로 이동
          </Link>
        </div>
      </div>
    );
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
          activeProjectId={String(projectId)}
          dirtyCount={dirtyCount}
          isSaving={saveFilesMutation.isPending}
          onSave={handleSave}
          onProjectChange={(projectId) =>
            startTransition(() => {
              navigate(`/workspace?projectId=${projectId}`);
            })
          }
          saveMessage={saveMessage}
          users={[]}
        />

        <div className="flex min-h-0 flex-1 flex-col gap-3 px-3 pb-3 lg:px-4 lg:pb-4">
          <div className="grid min-h-0 flex-1 gap-3 xl:grid-cols-[21rem_minmax(0,1fr)]">
            <Sidebar
              activities={workspaceActivityItems}
              activeActivity={activeActivity}
              activeFilePath={activeTab?.path ?? ''}
              collaborators={[]}
              fileTree={workspaceTree}
              isCreatingFile={createFileMutation.isPending}
              onActivityChange={(activityId) =>
                startTransition(() => {
                  setActiveActivity(activityId);
                })
              }
              onCreateDirectory={() => handleCreateFile('DIRECTORY')}
              onCreateFile={() => handleCreateFile('FILE')}
              onSelectTab={(tabId) =>
                startTransition(() => {
                  setActiveTabId(tabId);
                })
              }
              projectLabel={activeProject.label}
              tabs={editorTabs}
            />

            <Editor
              activeTabId={effectiveActiveTabId}
              content={activeFileContent}
              isContentLoading={fileContentQuery.isLoading}
              onContentChange={handleActiveFileContentChange}
              onTabChange={(tabId) =>
                startTransition(() => {
                  setActiveTabId(tabId);
                })
              }
              tabs={editorTabs}
            />
          </div>

          {fileCreateMessage ? (
            <div className="rounded-md border border-[#5a2c2c] bg-[#2b1d1d] px-3 py-2 text-sm text-[#f3b7b7]">
              {fileCreateMessage}
            </div>
          ) : null}

          <Terminal
            isRunning={runProjectMutation.isPending}
            lines={terminalLines}
            onRun={() => runProjectMutation.mutate()}
            runtimeLabel={
              projectDetailQuery.data
                ? `runtime: ${projectDetailQuery.data.runtime.displayName}`
                : undefined
            }
            statusLabel={terminalStatus}
          />
        </div>
      </div>
    </div>
  );
};
