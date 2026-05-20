import type {
  ActivityItem,
  PresenceUser,
  SettingSection,
  TerminalSession,
  WorkspaceProject,
  WorkspaceTab,
  WorkspaceTreeNode,
} from './types';

export const workspaceProjects: WorkspaceProject[] = [
  {
    id: 'project-a',
    label: 'Project A',
    subtitle: 'React + Vite collaborative workspace',
    lastOpened: '방금 전',
  },
  {
    id: 'pairing-lab',
    label: 'Pairing Lab',
    subtitle: 'Shared preview for UI pairing sessions',
    lastOpened: '32분 전',
  },
  {
    id: 'release-room',
    label: 'Release Room',
    subtitle: 'Read-only production validation room',
    lastOpened: '어제',
  },
];

export const presenceUsers: PresenceUser[] = [
  {
    id: 'kim',
    name: '김유현',
    role: 'Owner',
    file: 'src/pages/workspace/ui/WorkspacePage.tsx',
    location: 'line 28',
    lastSeen: '방금 전',
    accent: '#4fc1ff',
    accentSoft: 'rgba(79,193,255,0.16)',
    status: '편집 중',
  },
  {
    id: 'guest',
    name: 'Guest',
    role: 'Reviewer',
    file: 'src/shared/terminal.service.ts',
    location: 'line 14',
    lastSeen: '2분 전',
    accent: '#d7ba7d',
    accentSoft: 'rgba(215,186,125,0.16)',
    status: '검토 중',
  },
  {
    id: 'team-user',
    name: 'TeamUser',
    role: 'Editor',
    file: 'src/pages/workspace/ui/Terminal.tsx',
    location: 'Terminal 2',
    lastSeen: '5분 전',
    accent: '#73c991',
    accentSoft: 'rgba(115,201,145,0.16)',
    status: '실행 확인',
  },
];

export const activityItems: ActivityItem[] = [
  {
    id: 'explorer',
    label: 'Explorer',
    description: '파일 트리와 열려 있는 에디터 목록',
  },
  {
    id: 'collaboration',
    label: 'Collaboration',
    description: '현재 접속 사용자와 위치',
  },
  {
    id: 'settings',
    label: 'Settings',
    description: '자동 저장, 포맷, 권한 설정',
  },
];

export const workspaceTree: WorkspaceTreeNode[] = [
  {
    id: 'src',
    label: 'src',
    kind: 'folder',
    path: 'src',
    children: [
      {
        id: 'src-app',
        label: 'app',
        kind: 'folder',
        path: 'src/app',
        children: [
          {
            id: 'src-app-routes',
            label: 'routes',
            kind: 'folder',
            path: 'src/app/routes',
            children: [
              {
                id: 'src-app-routes-index',
                label: 'index.tsx',
                kind: 'file',
                path: 'src/app/routes/index.tsx',
              },
            ],
          },
        ],
      },
      {
        id: 'src-pages',
        label: 'pages',
        kind: 'folder',
        path: 'src/pages',
        children: [
          {
            id: 'src-pages-workspace',
            label: 'workspace',
            kind: 'folder',
            path: 'src/pages/workspace',
            children: [
              {
                id: 'src-pages-workspace-index',
                label: 'index.ts',
                kind: 'file',
                path: 'src/pages/workspace/index.ts',
              },
              {
                id: 'src-pages-workspace-model',
                label: 'model',
                kind: 'folder',
                path: 'src/pages/workspace/model',
                children: [
                  {
                    id: 'src-pages-workspace-model-index',
                    label: 'index.ts',
                    kind: 'file',
                    path: 'src/pages/workspace/model/index.ts',
                  },
                  {
                    id: 'src-pages-workspace-model-mock',
                    label: 'mock.ts',
                    kind: 'file',
                    path: 'src/pages/workspace/model/mock.ts',
                  },
                  {
                    id: 'src-pages-workspace-model-types',
                    label: 'types.ts',
                    kind: 'file',
                    path: 'src/pages/workspace/model/types.ts',
                  },
                ],
              },
              {
                id: 'src-pages-workspace-ui',
                label: 'ui',
                kind: 'folder',
                path: 'src/pages/workspace/ui',
                children: [
                  {
                    id: 'src-pages-workspace-ui-index',
                    label: 'index.ts',
                    kind: 'file',
                    path: 'src/pages/workspace/ui/index.ts',
                  },
                  {
                    id: 'src-pages-workspace-ui-page',
                    label: 'WorkspacePage.tsx',
                    kind: 'file',
                    path: 'src/pages/workspace/ui/WorkspacePage.tsx',
                  },
                  {
                    id: 'src-pages-workspace-ui-header',
                    label: 'WorkspaceHeader.tsx',
                    kind: 'file',
                    path: 'src/pages/workspace/ui/WorkspaceHeader.tsx',
                  },
                  {
                    id: 'src-pages-workspace-ui-editor',
                    label: 'Editor.tsx',
                    kind: 'file',
                    path: 'src/pages/workspace/ui/Editor.tsx',
                  },
                  {
                    id: 'src-pages-workspace-ui-sidebar',
                    label: 'Sidebar.tsx',
                    kind: 'file',
                    path: 'src/pages/workspace/ui/Sidebar.tsx',
                  },
                  {
                    id: 'src-pages-workspace-ui-terminal',
                    label: 'Terminal.tsx',
                    kind: 'file',
                    path: 'src/pages/workspace/ui/Terminal.tsx',
                  },
                  {
                    id: 'src-pages-workspace-ui-icons',
                    label: 'WorkspaceIcons.tsx',
                    kind: 'file',
                    path: 'src/pages/workspace/ui/WorkspaceIcons.tsx',
                  },
                ],
              },
            ],
          },
        ],
      },
      {
        id: 'src-shared',
        label: 'shared',
        kind: 'folder',
        path: 'src/shared',
        children: [
          {
            id: 'src-shared-terminal',
            label: 'terminal.service.ts',
            kind: 'file',
            path: 'src/shared/terminal.service.ts',
          },
        ],
      },
      {
        id: 'src-app-file',
        label: 'App.tsx',
        kind: 'file',
        path: 'src/App.tsx',
      },
      {
        id: 'src-index-css',
        label: 'index.css',
        kind: 'file',
        path: 'src/index.css',
      },
    ],
  },
  {
    id: 'package-json',
    label: 'package.json',
    kind: 'file',
    path: 'package.json',
  },
  {
    id: 'vite-config',
    label: 'vite.config.ts',
    kind: 'file',
    path: 'vite.config.ts',
  },
];

export const settingSections: SettingSection[] = [
  {
    id: 'editor',
    title: 'Editor',
    items: [
      {
        id: 'autosave',
        label: 'Auto Save',
        description: '수동 저장 전에도 변경 사항을 임시 보관',
        enabled: false,
      },
      {
        id: 'format',
        label: 'Format on Save',
        description: '저장 시 포매터 자동 실행',
        enabled: true,
      },
    ],
  },
  {
    id: 'collaboration',
    title: 'Collaboration',
    items: [
      {
        id: 'guest-edit',
        label: 'Guest Can Edit',
        description: '게스트에게 편집 권한 허용',
        enabled: true,
      },
      {
        id: 'cursor-share',
        label: 'Share Cursor Position',
        description: '현재 포커스된 라인 위치를 팀에 공유',
        enabled: true,
      },
    ],
  },
];

export const editorTabs: WorkspaceTab[] = [
  {
    id: 'tab-app',
    label: 'App.tsx',
    path: 'src/App.tsx',
    language: 'tsx',
    pinned: true,
    dirty: true,
  },
  {
    id: 'tab-header',
    label: 'WorkspaceHeader.tsx',
    path: 'src/pages/workspace/ui/WorkspaceHeader.tsx',
    language: 'tsx',
    pinned: false,
    dirty: false,
  },
  {
    id: 'tab-css',
    label: 'index.css',
    path: 'src/index.css',
    language: 'css',
    pinned: false,
    dirty: true,
  },
  {
    id: 'tab-terminal-service',
    label: 'terminal.service.ts',
    path: 'src/shared/terminal.service.ts',
    language: 'ts',
    pinned: false,
    dirty: false,
  },
];

export const terminalSessions: TerminalSession[] = [
  {
    id: 'terminal-1',
    label: 'Terminal 1',
    cwd: '~/project-a',
    status: 'running',
    lines: [
      '$ node scripts/demo.js',
      '',
      'Program started.',
      'Input file: samples/demo.json',
      'Processed 24 records in 118ms.',
      '',
      'Result: success',
      '[workspace] collaborative cursors synced',
    ],
  },
  {
    id: 'terminal-2',
    label: 'Terminal 2',
    cwd: '~/project-a',
    status: 'idle',
    lines: [
      '$ npm run build',
      '',
      'TypeScript: no type errors found',
      'vite v7.0 building for production...',
      'dist/index.html  0.48 kB',
      'dist/assets/index.js  133.72 kB',
    ],
  },
  {
    id: 'terminal-3',
    label: 'Terminal 3',
    cwd: '~/project-a',
    status: 'idle',
    lines: [
      '$ python tools/report.py',
      '',
      'Usage: report.py <input-path>',
      'Tip: pass --format json to capture machine-readable output.',
    ],
  },
];
