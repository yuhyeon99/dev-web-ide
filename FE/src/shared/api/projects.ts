import { apiRequest } from './client';
import type {
  ProjectCreateRequest,
  ProjectCreateResponse,
  ProjectDetailResponse,
  ProjectFileCreateResponse,
  ProjectFileContentResponse,
  ProjectFileSaveRequest,
  ProjectFileSaveResponse,
  ProjectFileTreeResponse,
  ProjectRunRequest,
  ProjectRunResponse,
  ProjectSummaryResponse,
} from './types';

export const getMyProjects = (accessToken: string) => {
  return apiRequest<ProjectSummaryResponse[]>('/api/projects/my', {
    accessToken,
  });
};

export const getSharedProjects = (accessToken: string) => {
  return apiRequest<ProjectSummaryResponse[]>('/api/projects/shared', {
    accessToken,
  });
};

export const createProject = (
  accessToken: string,
  request: ProjectCreateRequest,
) => {
  return apiRequest<ProjectCreateResponse>('/api/projects', {
    accessToken,
    method: 'POST',
    body: JSON.stringify(request),
  });
};

export const openProject = (accessToken: string, projectId: number) => {
  return apiRequest(`/api/projects/${projectId}/open`, {
    accessToken,
    method: 'POST',
  });
};

export const getProjectDetail = (accessToken: string, projectId: number) => {
  return apiRequest<ProjectDetailResponse>(`/api/projects/${projectId}`, {
    accessToken,
  });
};

export const getProjectFileTree = (projectId: number) => {
  return apiRequest<ProjectFileTreeResponse[]>(
    `/api/projects/${projectId}/files/tree`,
  );
};

export const getProjectFileContent = (projectId: number, fileId: number) => {
  return apiRequest<ProjectFileContentResponse>(
    `/api/projects/${projectId}/files/${fileId}/content`,
  );
};

export const createProjectFile = (
  projectId: number,
  request: {
    parentFileId: number;
    name: string;
    fileType: 'FILE' | 'DIRECTORY';
  },
) => {
  return apiRequest<ProjectFileCreateResponse>(
    `/api/projects/${projectId}/files`,
    {
      method: 'POST',
      body: JSON.stringify(request),
    },
  );
};

export const saveProjectFiles = (
  projectId: number,
  request: ProjectFileSaveRequest,
) => {
  return apiRequest<ProjectFileSaveResponse>(
    `/api/projects/${projectId}/save`,
    {
      method: 'POST',
      body: JSON.stringify(request),
    },
  );
};

export const runProject = (projectId: number, request: ProjectRunRequest) => {
  return apiRequest<ProjectRunResponse>(`/api/projects/${projectId}/run`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
};
