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

export type WorkspaceTab = {
  id: string;
  label: string;
  path: string;
  language: 'tsx' | 'css' | 'ts';
  pinned: boolean;
  dirty: boolean;
};

export type TerminalSession = {
  id: string;
  label: string;
  cwd: string;
  status: 'running' | 'idle';
  lines: string[];
};
