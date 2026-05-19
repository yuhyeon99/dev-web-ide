export type ActivityId = 'explorer' | 'collaboration' | 'settings';

export type WorkspaceProject = {
  id: string;
  label: string;
  subtitle: string;
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

export type WorkspaceTreeNode = {
  id: string;
  label: string;
  kind: 'folder' | 'file';
  path: string;
  children?: WorkspaceTreeNode[];
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
