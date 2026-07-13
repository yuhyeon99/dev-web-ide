export type ProjectCreationPreviewMode = 'guest' | 'member';

export type ProjectType = 'personal' | 'team';

export type ProjectRole = 'owner' | 'editor' | 'viewer';

export type RuntimeId = 'nodejs' | 'python' | 'java' | 'cpp';

export type InvitableMember = {
  id: number;
  name: string;
  email: string;
  team: string;
  status: string;
};

export type SelectedMember = InvitableMember & {
  role: ProjectRole;
};

export const projectCreationPreviewModeOptions = [
  {
    id: 'guest' as const,
    label: '게스트 화면',
    description: '개인 임시 프로젝트만 생성 가능한 화면',
  },
  {
    id: 'member' as const,
    label: '회원 화면',
    description: '팀 초대와 권한 설정까지 가능한 화면',
  },
] satisfies Array<{
  id: ProjectCreationPreviewMode;
  label: string;
  description: string;
}>;

export const projectTypeOptions = [
  {
    id: 'personal' as const,
    label: '개인 프로젝트',
    description: '현재 세션에서 혼자 빠르게 실험하고 바로 실행합니다.',
  },
  {
    id: 'team' as const,
    label: '팀 프로젝트',
    description: '팀원을 초대하고 역할을 나눠서 함께 임시 작업 공간을 엽니다.',
  },
] satisfies Array<{
  id: ProjectType;
  label: string;
  description: string;
}>;

export const projectRoleOptions = [
  {
    id: 'owner' as const,
    label: 'Owner',
    description: '전체 관리',
  },
  {
    id: 'editor' as const,
    label: 'Editor',
    description: '편집 가능',
  },
  {
    id: 'viewer' as const,
    label: 'Viewer',
    description: '읽기 전용',
  },
] satisfies Array<{
  id: ProjectRole;
  label: string;
  description: string;
}>;

export const runtimeOptions = [
  {
    id: 'nodejs' as const,
    label: 'Node.js',
    description: 'JavaScript/TypeScript 서버와 스크립트 실행에 적합',
    badge: 'JS',
  },
  {
    id: 'python' as const,
    label: 'Python',
    description: '데이터 처리와 스크립트 실험에 적합',
    badge: 'PY',
  },
  {
    id: 'java' as const,
    label: 'Java',
    description: 'Spring 기반 백엔드와 JVM 환경 점검에 적합',
    badge: 'JV',
  },
  {
    id: 'cpp' as const,
    label: 'C++',
    description: '알고리즘과 네이티브 실행 환경 검증에 적합',
    badge: 'C++',
  },
] satisfies Array<{
  id: RuntimeId;
  label: string;
  description: string;
  badge: string;
}>;

export const currentProjectOwner: SelectedMember = {
  id: 0,
  name: '현재 세션 사용자',
  email: 'guest-session@webide.local',
  team: 'Current Session',
  status: '프로젝트 생성자',
  role: 'owner',
};
