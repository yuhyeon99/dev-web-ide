export type ProjectFilterId = 'recent' | 'created' | 'invited' | 'running';

export type ProjectExecutionStatus = 'running' | 'stopped' | 'starting';

export type ProjectVisibility = 'personal' | 'team';

export type OpenProject = {
  id: string;
  name: string;
  updatedAt: string;
  executionStatus: ProjectExecutionStatus;
  visibility: ProjectVisibility;
  memberCount?: number;
  filters: ProjectFilterId[];
};

export const projectFilterTabs = [
  {
    id: 'recent' as const,
    label: '최근 접근 프로젝트',
  },
  {
    id: 'created' as const,
    label: '내가 생성한 프로젝트',
  },
  {
    id: 'invited' as const,
    label: '다른사람이 나를 초대한 프로젝트',
  },
  {
    id: 'running' as const,
    label: '실행 중인 프로젝트',
  },
] satisfies Array<{
  id: ProjectFilterId;
  label: string;
}>;

export const openProjects: OpenProject[] = [
  {
    id: 'frontend-redesign',
    name: 'frontend-redesign',
    updatedAt: '10분 전',
    executionStatus: 'running',
    visibility: 'team',
    memberCount: 5,
    filters: ['recent', 'created', 'running'],
  },
  {
    id: 'temp-playground',
    name: 'temp-playground',
    updatedAt: '34분 전',
    executionStatus: 'stopped',
    visibility: 'personal',
    filters: ['recent', 'created'],
  },
  {
    id: 'release-war-room',
    name: 'release-war-room',
    updatedAt: '1시간 전',
    executionStatus: 'running',
    visibility: 'team',
    memberCount: 8,
    filters: ['recent', 'invited', 'running'],
  },
  {
    id: 'api-response-check',
    name: 'api-response-check',
    updatedAt: '어제',
    executionStatus: 'starting',
    visibility: 'team',
    memberCount: 3,
    filters: ['recent', 'invited', 'running'],
  },
  {
    id: 'design-system-lab',
    name: 'design-system-lab',
    updatedAt: '2일 전',
    executionStatus: 'stopped',
    visibility: 'team',
    memberCount: 4,
    filters: ['created'],
  },
  {
    id: 'onboarding-sandbox',
    name: 'onboarding-sandbox',
    updatedAt: '3일 전',
    executionStatus: 'stopped',
    visibility: 'personal',
    filters: ['created'],
  },
  {
    id: 'pairing-lab',
    name: 'pairing-lab',
    updatedAt: '4시간 전',
    executionStatus: 'running',
    visibility: 'team',
    memberCount: 2,
    filters: ['invited', 'running'],
  },
  {
    id: 'qa-smoke-room',
    name: 'qa-smoke-room',
    updatedAt: '5시간 전',
    executionStatus: 'stopped',
    visibility: 'team',
    memberCount: 6,
    filters: ['invited'],
  },
  {
    id: 'qs-smoke-room',
    name: 'qs-smoke-room',
    updatedAt: '5시간 전',
    executionStatus: 'stopped',
    visibility: 'team',
    memberCount: 6,
    filters: ['invited'],
  },
];
