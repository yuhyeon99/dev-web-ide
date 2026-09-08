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
export type ProjectMemberRole = 'OWNER' | 'MAINTAINER' | 'EDITOR' | 'VIEWER';
export type ProjectMemberStatus = 'INVITED' | 'ACTIVE' | 'REMOVED';
export type ProjectFileType = 'FILE' | 'DIRECTORY';
export type WorkspaceSessionStatus =
  | 'STARTING'
  | 'RUNNING'
  | 'STOPPED'
  | 'FAILED';
export type ContainerInstanceStatus =
  | 'STARTING'
  | 'RUNNING'
  | 'STOPPED'
  | 'FAILED';

export type ProjectCreateRequest = {
  name: string;
  description: string;
  runtimeId: number;
  projectType: ProjectType;
  visibility: ProjectVisibility;
  memberUserIds: number[];
};

export type UserSearchResponse = {
  userId: number;
  email: string;
  nickname: string;
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
  members: ProjectMemberResponse[];
  createdAt: string;
  updatedAt: string;
};

export type ProjectMemberResponse = {
  projectMemberId: number;
  userId: number;
  nickname: string;
  role: ProjectMemberRole;
  status: ProjectMemberStatus;
  joinedAt: string | null;
};

export type ProjectMemberManageResponse = {
  projectMemberId: number;
  userId: number;
  nickname: string;
  role: ProjectMemberRole;
  status: ProjectMemberStatus;
  invitedAt: string | null;
  joinedAt: string | null;
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

export type ProjectFileSaveRequest = {
  userId: number | null;
  guestSessionId: number | null;
  files: Array<{
    projectFileId: number;
    content: string;
  }>;
};

export type ProjectFileSaveResponse = {
  projectId: number;
  saveBatchId: number;
  status: 'SUCCESS' | 'FAILED';
  savedFileCount: number;
  savedFiles: Array<{
    fileVersionId: number;
    projectFileId: number;
    versionNo: number;
    storagePath: string;
    contentHash: string;
    sizeBytes: number;
    createdAt: string;
  }>;
};

export type ProjectRunRequest = {
  userId: number | null;
  guestSessionId: number | null;
};

export type ProjectRunResponse = {
  projectId: number;
  workspaceSessionId: number;
  containerInstanceId: number;
  runtimeName: string;
  runtimeDisplayName: string;
  runtimeLanguage: RuntimeLanguage;
  dockerImage: string;
  workspaceStatus: WorkspaceSessionStatus;
  efsMountPath: string;
  container: {
    id: number;
    provider: string;
    taskArn: string | null;
    containerId: string;
    dockerImage: string;
    status: ContainerInstanceStatus;
    efsMountPath: string;
  };
};

export type ProjectRealtimeEventType =
  | 'FILE_CREATED'
  | 'FILE_RENAMED'
  | 'FILE_DELETED'
  | 'FILE_SAVED';

export type ProjectRealtimeEvent = {
  type: ProjectRealtimeEventType;
  projectId: number;
  fileId: number | null;
  fileIds: number[];
  path: string | null;
  actorType: 'USER' | 'GUEST' | 'SYSTEM';
  actorId: number | null;
  occurredAt: string;
};

export type CrdtUpdateMessage = {
  projectId: number;
  fileId: number;
  roomId: string;
  clientId: string;
  updateBase64: string;
  occurredAt: string;
};

export type LiveFileContentMessage = {
  projectId: number;
  fileId: number;
  roomId: string;
  clientId: string;
  content: string;
  occurredAt: string;
};

export type TeamChatMessage = {
  id: string;
  projectId: number;
  clientId: string;
  senderName: string;
  message: string;
  occurredAt: string;
};

export type WorkspacePresenceUser = {
  clientId: string;
  presenceKey: string;
  displayName: string;
  role: string;
  currentFile: string;
  status: string;
  joinedAt: string;
  lastSeenAt: string;
};

export type WorkspacePresenceMessage = {
  projectId: number;
  users: WorkspacePresenceUser[];
  occurredAt: string;
};
