export type RuntimeLanguage = 'NODE' | 'PYTHON' | 'JAVA' | 'CPP';

export type RuntimeResponse = {
  id: number;
  name: string;
  displayName: string;
  version: string;
  dockerImage: string;
  language: RuntimeLanguage;
};

export type GuestSessionCreateResponse = {
  guestSessionId: number;
  guestToken: string;
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  accessTokenExpiresAt: string;
  expiresAt: string;
  createdAt: string;
};

export type ProjectType = 'PERSONAL' | 'TEAM' | 'GUEST';
export type ProjectVisibility = 'PRIVATE' | 'TEAM';
export type ProjectStatus = 'ACTIVE' | 'DELETED';
export type ProjectFileType = 'FILE' | 'DIRECTORY';

export type ProjectCreateRequest = {
  name: string;
  description: string;
  runtimeId: number;
  projectType: ProjectType;
  visibility: ProjectVisibility;
  memberUserIds: number[];
};

export type ProjectSummaryResponse = {
  id: number;
  name: string;
  description: string | null;
  projectType: ProjectType;
  visibility: ProjectVisibility;
  status: ProjectStatus;
  runtimeId: number;
  runtimeName: string;
  runtimeDisplayName: string;
  runtimeLanguage: RuntimeLanguage;
  createdAt: string;
  updatedAt: string;
};

export type ProjectCreateResponse = Omit<
  ProjectSummaryResponse,
  'updatedAt'
> & {
  createdAt: string;
};

export type ProjectDetailResponse = {
  id: number;
  name: string;
  description: string | null;
  projectType: ProjectType;
  visibility: ProjectVisibility;
  status: ProjectStatus;
  storagePath: string;
  runtime: RuntimeResponse;
  settings: {
    autoSaveEnabled: boolean;
    formatOnSaveEnabled: boolean;
    guestCanEdit: boolean;
    shareCursorPosition: boolean;
  };
  members: unknown[];
  createdAt: string;
  updatedAt: string;
};

export type ProjectFileTreeResponse = {
  id: number;
  parentFileId: number | null;
  name: string;
  path: string;
  fileType: ProjectFileType;
  mimeType: string | null;
  sizeBytes: number;
  status: 'ACTIVE' | 'DELETED';
  createdAt: string;
  updatedAt: string;
  children: ProjectFileTreeResponse[];
};

export type ProjectFileContentResponse = {
  projectFileId: number;
  name: string;
  path: string;
  mimeType: string | null;
  sizeBytes: number;
  content: string;
  updatedAt: string;
};

export type ProjectFileCreateResponse = {
  projectFileId: number;
  parentFileId: number | null;
  name: string;
  path: string;
  fileType: ProjectFileType;
  mimeType: string | null;
  sizeBytes: number;
  status: 'ACTIVE' | 'DELETED';
  createdAt: string;
  updatedAt: string;
};
