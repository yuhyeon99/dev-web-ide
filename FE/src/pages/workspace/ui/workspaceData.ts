export type ActivityId =
  | 'explorer'
  | 'search'
  | 'git'
  | 'collaboration'
  | 'run'
  | 'settings';

export type BottomPanelId = 'terminal' | 'problems' | 'output' | 'ports';

export type WorkspaceProject = {
  id: string;
  label: string;
  subtitle: string;
  branch: string;
  lastOpened: string;
};

export type PresenceUser = {
  id: string;
  name: string;
  role: string;
  file: string;
  location: string;
  lastSeen: string;
  accent: string;
  accentSoft: string;
  status: string;
};

export type ActivityItem = {
  id: ActivityId;
  label: string;
  description: string;
};

export type BottomPanelItem = {
  id: BottomPanelId;
  label: string;
};

export type WorkspaceTreeNode = {
  id: string;
  label: string;
  kind: 'folder' | 'file';
  path: string;
  children?: WorkspaceTreeNode[];
};

export type SearchResult = {
  id: string;
  file: string;
  path: string;
  matches: {
    line: number;
    text: string;
  }[];
};

export type GitChange = {
  path: string;
  status: 'M' | 'A' | 'D';
  summary: string;
};

export type RunProfile = {
  id: string;
  label: string;
  description: string;
  command: string;
  status: 'Running' | 'Ready' | 'Queued';
};

export type SettingSection = {
  id: string;
  title: string;
  items: {
    id: string;
    label: string;
    description: string;
    enabled: boolean;
  }[];
};

export type EditorTokenTone =
  | 'text'
  | 'keyword'
  | 'string'
  | 'comment'
  | 'function'
  | 'type'
  | 'property'
  | 'tag'
  | 'accent';

export type EditorToken = {
  text: string;
  tone?: EditorTokenTone;
};

export type EditorLine = {
  tokens: EditorToken[];
  marker?: {
    tone: 'warning' | 'info';
    label: string;
  };
};

export type WorkspaceTab = {
  id: string;
  label: string;
  path: string;
  language: 'tsx' | 'css' | 'ts';
  pinned: boolean;
  dirty: boolean;
  description: string;
  focusLine: number;
  code: EditorLine[];
  cursors: {
    id: string;
    label: string;
    line: number;
    accent: string;
  }[];
};

export type TerminalSession = {
  id: string;
  label: string;
  cwd: string;
  status: 'running' | 'idle';
  lines: string[];
};

export type ProblemItem = {
  id: string;
  severity: 'warning' | 'info' | 'error';
  message: string;
  file: string;
  line: number;
};

export type PortItem = {
  port: number;
  label: string;
  status: string;
  visibility: string;
};

const token = (text: string, tone: EditorTokenTone = 'text'): EditorToken => ({
  text,
  tone,
});

const line = (...tokens: EditorToken[]): EditorLine => ({
  tokens,
});

export const workspaceProjects: WorkspaceProject[] = [
  {
    id: 'project-a',
    label: 'Project A',
    subtitle: 'React + Vite collaborative workspace',
    branch: 'feature/workspace-ui',
    lastOpened: '방금 전',
  },
  {
    id: 'pairing-lab',
    label: 'Pairing Lab',
    subtitle: 'Shared preview for UI pairing sessions',
    branch: 'main',
    lastOpened: '32분 전',
  },
  {
    id: 'release-room',
    label: 'Release Room',
    subtitle: 'Read-only production validation room',
    branch: 'release/2026-05',
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
    id: 'search',
    label: 'Search',
    description: '프로젝트 전체 문자열 검색',
  },
  {
    id: 'git',
    label: 'Git',
    description: '변경 파일과 브랜치 상태',
  },
  {
    id: 'collaboration',
    label: 'Collaboration',
    description: '현재 접속 사용자와 위치',
  },
  {
    id: 'run',
    label: 'Run',
    description: '실행 프로필과 컨테이너 상태',
  },
  {
    id: 'settings',
    label: 'Settings',
    description: '자동 저장, 포맷, 권한 설정',
  },
];

export const bottomPanelItems: BottomPanelItem[] = [
  { id: 'terminal', label: 'Terminal' },
  { id: 'problems', label: 'Problems' },
  { id: 'output', label: 'Output' },
  { id: 'ports', label: 'Ports' },
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
                id: 'src-pages-workspace-ui',
                label: 'ui',
                kind: 'folder',
                path: 'src/pages/workspace/ui',
                children: [
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
                    id: 'src-pages-workspace-ui-terminal',
                    label: 'Terminal.tsx',
                    kind: 'file',
                    path: 'src/pages/workspace/ui/Terminal.tsx',
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

export const searchResults: SearchResult[] = [
  {
    id: 'search-1',
    file: 'WorkspacePage.tsx',
    path: 'src/pages/workspace/ui/WorkspacePage.tsx',
    matches: [
      {
        line: 23,
        text: 'const [activeActivity, setActiveActivity] = useState<ActivityId>(...)',
      },
      {
        line: 46,
        text: '<WorkspaceHeader users={presenceUsers} />',
      },
    ],
  },
  {
    id: 'search-2',
    file: 'WorkspaceHeader.tsx',
    path: 'src/pages/workspace/ui/WorkspaceHeader.tsx',
    matches: [
      {
        line: 34,
        text: '<button type="button">Project A</button>',
      },
      {
        line: 81,
        text: '<span className="text-[11px] uppercase">Presence</span>',
      },
    ],
  },
  {
    id: 'search-3',
    file: 'terminal.service.ts',
    path: 'src/shared/terminal.service.ts',
    matches: [
      {
        line: 12,
        text: 'return response.json() as Promise<RunResponse>',
      },
    ],
  },
];

export const gitChanges: GitChange[] = [
  {
    path: 'src/pages/workspace/ui/WorkspacePage.tsx',
    status: 'M',
    summary: '워크스페이스 레이아웃과 상태 구성 추가',
  },
  {
    path: 'src/pages/workspace/ui/Editor.tsx',
    status: 'A',
    summary: 'Monaco 스타일 편집기 퍼블리싱',
  },
  {
    path: 'src/pages/workspace/ui/Terminal.tsx',
    status: 'A',
    summary: '하단 실행 패널과 세션 탭 구현',
  },
];

export const runProfiles: RunProfile[] = [
  {
    id: 'dev',
    label: 'npm run dev',
    description: '프론트엔드 개발 서버를 컨테이너에서 기동',
    command: 'container exec npm run dev',
    status: 'Running',
  },
  {
    id: 'build',
    label: 'npm run build',
    description: '타입 체크와 프로덕션 번들 검증',
    command: 'container exec npm run build',
    status: 'Ready',
  },
  {
    id: 'tests',
    label: 'npm run test',
    description: '향후 테스트 스위트 연결용 슬롯',
    command: 'container exec npm run test',
    status: 'Queued',
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
    description: '워크스페이스 셸을 감싸는 진입점',
    focusLine: 7,
    code: [
      line(
        token('import ', 'keyword'),
        token('{ WorkspaceShell }', 'type'),
        token(' from ', 'keyword'),
        token("'@/pages/workspace'", 'string'),
        token(';'),
      ),
      line(
        token('const ', 'keyword'),
        token('participants', 'property'),
        token(' = ', 'text'),
        token("['김유현', 'Guest', 'TeamUser']", 'string'),
        token(';'),
      ),
      line(),
      line(
        token('export default function ', 'keyword'),
        token('App', 'function'),
        token('() {'),
      ),
      line(token('  return ', 'keyword'), token('(')),
      line(
        token('    <', 'tag'),
        token('WorkspaceShell', 'type'),
        token(' project=', 'property'),
        token('"Project A"', 'string'),
      ),
      line(
        token('      collaborators=', 'property'),
        token('{participants}', 'accent'),
      ),
      line(
        token('      defaultCommand=', 'property'),
        token('"npm run dev"', 'string'),
      ),
      line(token('    />', 'tag')),
      line(token('  );')),
      line(token('}')),
    ],
    cursors: [
      {
        id: 'guest',
        label: 'Guest',
        line: 7,
        accent: '#d7ba7d',
      },
      {
        id: 'team-user',
        label: 'TeamUser',
        line: 8,
        accent: '#73c991',
      },
    ],
  },
  {
    id: 'tab-header',
    label: 'WorkspaceHeader.tsx',
    path: 'src/pages/workspace/ui/WorkspaceHeader.tsx',
    language: 'tsx',
    pinned: false,
    dirty: false,
    description: '프로젝트 전환과 Presence UI를 담당하는 헤더',
    focusLine: 10,
    code: [
      line(
        token('type ', 'keyword'),
        token('WorkspaceHeaderProps', 'type'),
        token(' = {'),
      ),
      line(
        token('  project: ', 'property'),
        token('string', 'type'),
        token(';'),
      ),
      line(
        token('  onRun: ', 'property'),
        token('() => void', 'type'),
        token(';'),
      ),
      line(
        token('  users: ', 'property'),
        token('PresenceUser[]', 'type'),
        token(';'),
      ),
      line(token('};')),
      line(),
      line(
        token('export function ', 'keyword'),
        token('WorkspaceHeader', 'function'),
        token('({ project, users }: WorkspaceHeaderProps) {'),
      ),
      line(token('  return ', 'keyword'), token('(')),
      line(
        token('    <', 'tag'),
        token('header', 'tag'),
        token(' className=', 'property'),
        token('"workspace-header"', 'string'),
        token('>', 'tag'),
      ),
      line(
        token('      <', 'tag'),
        token('button', 'tag'),
        token('>{project}</', 'tag'),
        token('button', 'tag'),
        token('>', 'tag'),
      ),
      line(
        token('      <', 'tag'),
        token('PresenceStack', 'type'),
        token(' users={users} />', 'accent'),
      ),
      line(token('    </', 'tag'), token('header', 'tag'), token('>', 'tag')),
      line(token('  );')),
      line(token('}')),
    ],
    cursors: [
      {
        id: 'kim',
        label: '김유현',
        line: 10,
        accent: '#4fc1ff',
      },
    ],
  },
  {
    id: 'tab-css',
    label: 'index.css',
    path: 'src/index.css',
    language: 'css',
    pinned: false,
    dirty: true,
    description: '다크 테마 토큰과 전체 레이아웃 기반 스타일',
    focusLine: 4,
    code: [
      line(token(':root ', 'tag'), token('{')),
      line(
        token('  --workspace-accent', 'property'),
        token(': ', 'text'),
        token('#0e639c', 'string'),
        token(';'),
      ),
      line(
        token('  --workspace-surface', 'property'),
        token(': ', 'text'),
        token('#252526', 'string'),
        token(';'),
      ),
      line(
        token('  --workspace-border', 'property'),
        token(': ', 'text'),
        token('#313135', 'string'),
        token(';'),
      ),
      line(token('}')),
      line(),
      line(token('.workspace-shell ', 'tag'), token('{')),
      line(
        token('  display', 'property'),
        token(': ', 'text'),
        token('grid', 'string'),
        token(';'),
      ),
      line(
        token('  grid-template-rows', 'property'),
        token(': ', 'text'),
        token('auto 1fr auto', 'string'),
        token(';'),
      ),
      line(token('}')),
    ],
    cursors: [
      {
        id: 'guest',
        label: 'Guest',
        line: 4,
        accent: '#d7ba7d',
      },
    ],
  },
  {
    id: 'tab-terminal-service',
    label: 'terminal.service.ts',
    path: 'src/shared/terminal.service.ts',
    language: 'ts',
    pinned: false,
    dirty: false,
    description: '런타임 컨테이너 실행 요청 서비스',
    focusLine: 9,
    code: [
      line(
        token('export async function ', 'keyword'),
        token('startDevServer', 'function'),
        token('(projectId: ', 'text'),
        token('string', 'type'),
        token(') {'),
      ),
      line(
        token('  const ', 'keyword'),
        token('response', 'property'),
        token(' = await ', 'keyword'),
        token('fetch', 'function'),
        token('(`'),
        token('/api/projects/${projectId}/run', 'string'),
        token('`, {'),
      ),
      line(token("    method: 'POST',", 'string')),
      line(token('  });')),
      line(),
      line(
        token('  if ', 'keyword'),
        token('(!response.ok)', 'text'),
        token(' {'),
      ),
      line(
        token('    throw new ', 'keyword'),
        token('Error', 'type'),
        token("('Failed to start container');", 'string'),
      ),
      line(token('  }')),
      {
        tokens: [
          token('  return ', 'keyword'),
          token('response', 'property'),
          token('.'),
          token('json', 'function'),
          token('();'),
        ],
        marker: {
          tone: 'warning',
          label: 'stdout/stderr 스트리밍 분리 예정',
        },
      },
      line(token('}')),
    ],
    cursors: [
      {
        id: 'team-user',
        label: 'TeamUser',
        line: 9,
        accent: '#73c991',
      },
    ],
  },
];

export const terminalSessions: TerminalSession[] = [
  {
    id: 'terminal-1',
    label: 'Terminal 1',
    cwd: '~/project-a',
    status: 'running',
    lines: [
      '$ npm run dev',
      '',
      'VITE v7.0 ready in 421ms',
      '➜ Local:   http://localhost:5173',
      '➜ Network: use --host to expose',
      '',
      '[hmr] connected.',
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
    id: 'server-logs',
    label: 'Server Logs',
    cwd: 'container://runner',
    status: 'running',
    lines: [
      '[runner] attached to workspace container',
      '[runner] port 5173 forwarded to preview gateway',
      '[runner] sync heartbeat ok',
    ],
  },
];

export const problemItems: ProblemItem[] = [
  {
    id: 'problem-1',
    severity: 'warning',
    message:
      'Monaco 패키지가 연결되지 않아 현재는 퍼블리싱용 편집 화면을 렌더링합니다.',
    file: 'src/pages/workspace/ui/Editor.tsx',
    line: 96,
  },
  {
    id: 'problem-2',
    severity: 'info',
    message: '게스트 권한은 현재 편집 가능으로 설정되어 있습니다.',
    file: 'src/pages/workspace/ui/Sidebar.tsx',
    line: 210,
  },
];

export const outputLines = [
  '[workspace] save requested by 김유현',
  '[workspace] git status: 3 changed files',
  '[runner] container status: healthy',
  '[preview] share link prepared for team members',
];

export const portItems: PortItem[] = [
  {
    port: 5173,
    label: 'Vite Dev Server',
    status: 'Live',
    visibility: 'Team',
  },
  {
    port: 24678,
    label: 'HMR WebSocket',
    status: 'Internal',
    visibility: 'Private',
  },
];
