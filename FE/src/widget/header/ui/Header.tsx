import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useLocation, useNavigate } from 'react-router';

import logo from '@/assets/images/logo.svg';
import { AuthModal } from '@/features/auth';
import { OpenProjectsModal } from '@/features/open-projects';
import { CreateTemporaryProjectModal } from '@/features/project-creation';
import type { ProjectCreateFormValues } from '@/features/project-creation/ui/create-temporary-project-modal';
import {
  clearAuthSession,
  getStoredAuthSession,
  subscribeAuthSession,
} from '@/shared/api/auth';
import {
  acceptProjectInvitation,
  createProject,
  createProjectFile,
  getProjectDetail,
  getProjectFileTree,
  inviteProjectMember,
  openProject,
  removeProjectMember,
  updateProjectMemberRole,
} from '@/shared/api/projects';
import { getRuntimes } from '@/shared/api/runtimes';
import {
  resolveProjectApiSession,
  saveProjectApiSession,
} from '@/shared/api/session';
import type {
  ProjectDetailResponse,
  ProjectMemberRole,
  RuntimeResponse,
  UserSearchResponse,
} from '@/shared/api/types';
import { searchUsers } from '@/shared/api/users';

const getRuntimeBadge = (runtime: RuntimeResponse) => {
  switch (runtime.language) {
    case 'PYTHON':
      return 'PY';
    case 'JAVA':
      return 'JV';
    case 'CPP':
      return 'C++';
    case 'NODE':
      return 'JS';
  }
};

const getRuntimeMeta = (runtime: RuntimeResponse) => {
  switch (runtime.language) {
    case 'PYTHON':
      return '스크립트';
    case 'JAVA':
      return 'JVM';
    case 'CPP':
      return 'Native';
    case 'NODE':
      return 'JS/TS';
  }
};

type TeamModalTab = 'invite' | 'settings' | 'permissions';

type TeamManagementModalProps = {
  accessToken: string;
  initialTab: TeamModalTab;
  isOpen: boolean;
  onClose: () => void;
  onProjectRefresh: () => Promise<void>;
  project: ProjectDetailResponse;
};

const manageableRoles = ['MAINTAINER', 'EDITOR', 'VIEWER'] satisfies Exclude<
  ProjectMemberRole,
  'OWNER'
>[];

const roleLabelMap: Record<ProjectMemberRole, string> = {
  OWNER: 'Owner',
  MAINTAINER: 'Maintainer',
  EDITOR: 'Editor',
  VIEWER: 'Viewer',
};

const statusLabelMap = {
  ACTIVE: '참여 중',
  INVITED: '초대됨',
  REMOVED: '제거됨',
};

const TeamManagementModal = ({
  accessToken,
  initialTab,
  isOpen,
  onClose,
  onProjectRefresh,
  project,
}: TeamManagementModalProps) => {
  const [activeTab, setActiveTab] = useState<TeamModalTab>(initialTab);
  const [memberSearchQuery, setMemberSearchQuery] = useState('');
  const [selectedInviteRole, setSelectedInviteRole] =
    useState<Exclude<ProjectMemberRole, 'OWNER'>>('EDITOR');
  const [teamManageMessage, setTeamManageMessage] = useState<string | null>(
    null,
  );
  const queryClient = useQueryClient();
  const normalizedQuery = memberSearchQuery.trim();
  const currentUserId = getStoredAuthSession()?.userId ?? null;
  const currentMember = project.members.find(
    (member) => member.userId === currentUserId,
  );
  const isOwner = currentMember?.role === 'OWNER';
  const currentInvitationMemberId =
    currentMember?.status === 'INVITED' ? currentMember.projectMemberId : null;
  const memberSearchQueryResult = useQuery({
    queryKey: ['team-member-search', project.id, normalizedQuery],
    queryFn: () => searchUsers(accessToken, normalizedQuery),
    enabled:
      isOpen &&
      activeTab === 'invite' &&
      isOwner &&
      normalizedQuery.length >= 2,
  });
  const memberMutation = useMutation({
    mutationFn: async (action: {
      memberId?: number;
      role?: Exclude<ProjectMemberRole, 'OWNER'>;
      type: 'invite' | 'update-role' | 'remove' | 'accept';
      userId?: number;
    }) => {
      if (action.type === 'invite') {
        return inviteProjectMember(accessToken, project.id, {
          role: action.role ?? 'EDITOR',
          userId: action.userId ?? 0,
        });
      }

      if (action.type === 'update-role') {
        return updateProjectMemberRole(
          accessToken,
          project.id,
          action.memberId ?? 0,
          {
            role: action.role ?? 'EDITOR',
          },
        );
      }

      if (action.type === 'remove') {
        return removeProjectMember(
          accessToken,
          project.id,
          action.memberId ?? 0,
        );
      }

      return acceptProjectInvitation(
        accessToken,
        project.id,
        action.memberId ?? 0,
      );
    },
    onError: () => {
      setTeamManageMessage('팀 멤버 관리 요청에 실패했습니다.');
    },
    onSuccess: async () => {
      setTeamManageMessage('변경사항이 반영되었습니다.');
      setMemberSearchQuery('');
      await onProjectRefresh();
      await queryClient.invalidateQueries({ queryKey: ['workspace-projects'] });
    },
  });
  const visibleSearchResults = (memberSearchQueryResult.data ?? []).filter(
    (user: UserSearchResponse) =>
      !project.members.some((member) => member.userId === user.userId),
  );

  if (!isOpen) {
    return null;
  }

  const tabs = [
    { id: 'invite' as const, label: '멤버 초대' },
    { id: 'settings' as const, label: '팀 설정' },
    { id: 'permissions' as const, label: '권한' },
  ];

  return (
    <div
      className="fixed inset-0 z-[130] flex items-center justify-center bg-[rgba(0,0,0,0.68)] px-4"
      onClick={onClose}
      role="presentation"
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="team-management-title"
        className="flex max-h-[calc(100dvh-2rem)] w-full max-w-3xl flex-col overflow-hidden rounded-xl border border-[#313131] bg-[#1e1e1e] shadow-[0_24px_80px_rgba(0,0,0,0.58)]"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="flex items-start justify-between gap-4 border-b border-[#313131] bg-[#252526] px-5 py-4">
          <div className="min-w-0">
            <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
              Team Project
            </p>
            <h2
              id="team-management-title"
              className="mt-1 truncate text-lg font-semibold text-[#f3f3f3]"
            >
              {project.name}
            </h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-md text-[#9da1a6] hover:bg-[#2a2d2e] hover:text-white"
            aria-label="팀 관리 팝업 닫기"
          >
            x
          </button>
        </div>

        <div className="border-b border-[#313131] bg-[#1b1b1c] px-5 py-2">
          <div className="flex flex-wrap gap-2">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                type="button"
                onClick={() => setActiveTab(tab.id)}
                className={`rounded-md px-3 py-2 text-sm font-semibold ${
                  activeTab === tab.id
                    ? 'bg-[#0e639c] text-white'
                    : 'text-[#9da1a6] hover:bg-[#252526] hover:text-[#d4d4d4]'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto px-5 py-4">
          {currentInvitationMemberId ? (
            <div className="mb-4 rounded-lg border border-[#0e639c]/40 bg-[#0e639c]/12 px-3 py-3 text-sm text-[#d4d4d4]">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <span>이 팀 프로젝트 초대가 대기 중입니다.</span>
                <button
                  type="button"
                  disabled={memberMutation.isPending}
                  onClick={() =>
                    memberMutation.mutate({
                      memberId: currentInvitationMemberId,
                      type: 'accept',
                    })
                  }
                  className="rounded-md bg-[#0e639c] px-3 py-2 text-sm font-semibold text-white disabled:opacity-50"
                >
                  초대 수락
                </button>
              </div>
            </div>
          ) : null}

          {activeTab === 'invite' ? (
            <div className="space-y-4">
              {isOwner ? (
                <>
                  <div className="grid gap-3 sm:grid-cols-[minmax(0,1fr)_10rem]">
                    <input
                      type="text"
                      value={memberSearchQuery}
                      onChange={(event) =>
                        setMemberSearchQuery(event.target.value)
                      }
                      className="rounded-md border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2.5 text-sm text-[#d4d4d4] outline-none focus:border-[#007acc]"
                      placeholder="닉네임 또는 이메일 검색"
                    />
                    <select
                      value={selectedInviteRole}
                      onChange={(event) =>
                        setSelectedInviteRole(
                          event.target.value as Exclude<
                            ProjectMemberRole,
                            'OWNER'
                          >,
                        )
                      }
                      className="rounded-md border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2.5 text-sm text-[#d4d4d4] outline-none focus:border-[#007acc]"
                    >
                      {manageableRoles.map((role) => (
                        <option key={role} value={role}>
                          {roleLabelMap[role]}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="space-y-2">
                    {visibleSearchResults.length > 0 ? (
                      visibleSearchResults.map((user) => (
                        <div
                          key={user.userId}
                          className="flex items-center justify-between gap-3 rounded-lg border border-[#313131] bg-[#252526] px-3 py-3"
                        >
                          <div className="min-w-0">
                            <p className="truncate text-sm font-semibold text-[#f3f3f3]">
                              {user.nickname}
                            </p>
                            <p className="truncate text-xs text-[#858585]">
                              {user.email}
                            </p>
                          </div>
                          <button
                            type="button"
                            disabled={memberMutation.isPending}
                            onClick={() =>
                              memberMutation.mutate({
                                role: selectedInviteRole,
                                type: 'invite',
                                userId: user.userId,
                              })
                            }
                            className="shrink-0 rounded-md border border-[#0e639c]/50 bg-[#0e639c]/14 px-3 py-2 text-sm font-semibold text-[#7fd6ff] disabled:opacity-50"
                          >
                            초대
                          </button>
                        </div>
                      ))
                    ) : (
                      <div className="rounded-lg border border-dashed border-[#3c3c3c] bg-[#1b1b1c] px-4 py-8 text-center text-sm text-[#858585]">
                        {normalizedQuery.length < 2
                          ? '검색어를 2글자 이상 입력하세요.'
                          : memberSearchQueryResult.isLoading
                            ? '회원을 검색하는 중입니다.'
                            : '초대 가능한 회원이 없습니다.'}
                      </div>
                    )}
                  </div>
                </>
              ) : (
                <div className="rounded-lg border border-[#313131] bg-[#252526] px-4 py-8 text-center text-sm text-[#858585]">
                  팀 멤버 초대는 프로젝트 Owner만 사용할 수 있습니다.
                </div>
              )}
            </div>
          ) : null}

          {activeTab === 'settings' ? (
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="rounded-lg border border-[#313131] bg-[#252526] px-4 py-3">
                <p className="text-xs text-[#858585]">프로젝트 유형</p>
                <p className="mt-1 text-sm font-semibold text-[#f3f3f3]">
                  팀 프로젝트
                </p>
              </div>
              <div className="rounded-lg border border-[#313131] bg-[#252526] px-4 py-3">
                <p className="text-xs text-[#858585]">활성 멤버</p>
                <p className="mt-1 text-sm font-semibold text-[#f3f3f3]">
                  {
                    project.members.filter(
                      (member) => member.status === 'ACTIVE',
                    ).length
                  }
                  명
                </p>
              </div>
              <div className="rounded-lg border border-[#313131] bg-[#252526] px-4 py-3">
                <p className="text-xs text-[#858585]">게스트 편집</p>
                <p className="mt-1 text-sm font-semibold text-[#f3f3f3]">
                  {project.settings.guestCanEdit ? '허용' : '차단'}
                </p>
              </div>
              <div className="rounded-lg border border-[#313131] bg-[#252526] px-4 py-3">
                <p className="text-xs text-[#858585]">커서 공유</p>
                <p className="mt-1 text-sm font-semibold text-[#f3f3f3]">
                  {project.settings.shareCursorPosition ? '사용' : '미사용'}
                </p>
              </div>
            </div>
          ) : null}

          {activeTab === 'permissions' ? (
            <div className="space-y-2">
              {project.members.map((member) => {
                const canManageMember = isOwner && member.role !== 'OWNER';

                return (
                  <div
                    key={member.userId}
                    className="grid gap-3 rounded-lg border border-[#313131] bg-[#252526] px-3 py-3 sm:grid-cols-[minmax(0,1fr)_10rem_6rem]"
                  >
                    <div className="min-w-0">
                      <p className="truncate text-sm font-semibold text-[#f3f3f3]">
                        {member.nickname}
                      </p>
                      <p className="mt-1 text-xs text-[#858585]">
                        {statusLabelMap[member.status]}
                      </p>
                    </div>
                    {canManageMember ? (
                      <select
                        value={member.role}
                        disabled={memberMutation.isPending}
                        onChange={(event) =>
                          memberMutation.mutate({
                            memberId: member.projectMemberId,
                            role: event.target.value as Exclude<
                              ProjectMemberRole,
                              'OWNER'
                            >,
                            type: 'update-role',
                          })
                        }
                        className="rounded-md border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2 text-sm text-[#d4d4d4] outline-none"
                      >
                        {manageableRoles.map((role) => (
                          <option key={role} value={role}>
                            {roleLabelMap[role]}
                          </option>
                        ))}
                      </select>
                    ) : (
                      <span className="rounded-md border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2 text-sm text-[#d4d4d4]">
                        {roleLabelMap[member.role]}
                      </span>
                    )}
                    <button
                      type="button"
                      disabled={!canManageMember || memberMutation.isPending}
                      onClick={() =>
                        memberMutation.mutate({
                          memberId: member.projectMemberId,
                          type: 'remove',
                        })
                      }
                      className="rounded-md border border-[#3c3c3c] px-3 py-2 text-sm font-semibold text-[#d4d4d4] enabled:hover:bg-[#2a2d2e] disabled:cursor-not-allowed disabled:opacity-40"
                    >
                      제거
                    </button>
                  </div>
                );
              })}
            </div>
          ) : null}
        </div>

        <div className="flex items-center justify-between gap-3 border-t border-[#313131] bg-[#181818] px-5 py-3">
          <p className="text-xs text-[#858585]">
            {teamManageMessage ??
              (isOwner
                ? 'Owner 권한으로 팀을 관리합니다.'
                : '팀 멤버 정보를 확인합니다.')}
          </p>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md border border-[#3c3c3c] bg-[#2d2d30] px-3.5 py-2 text-sm font-semibold text-[#d4d4d4]"
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  );
};

export const Header = () => {
  const [authSession, setAuthSession] = useState(() => getStoredAuthSession());
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [
    isCreateTemporaryProjectModalOpen,
    setCreateTemporaryProjectModalOpen,
  ] = useState(false);
  const [isOpenProjectsModalOpen, setOpenProjectsModalOpen] = useState(false);
  const [isTeamManagementModalOpen, setTeamManagementModalOpen] =
    useState(false);
  const [teamManagementInitialTab, setTeamManagementInitialTab] =
    useState<TeamModalTab>('invite');
  const [isProfileMenuOpen, setProfileMenuOpen] = useState(false);
  const [createProjectError, setCreateProjectError] = useState<string | null>(
    null,
  );
  const location = useLocation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const authModalKey = `${authSession?.userId ?? 'none'}`;
  const currentProjectId = useMemo(() => {
    const projectId = Number(
      new URLSearchParams(location.search).get('projectId'),
    );

    return Number.isFinite(projectId) && projectId > 0 ? projectId : null;
  }, [location.search]);
  const profileInitial = useMemo(() => {
    if (!authSession?.nickname) {
      return '';
    }

    return authSession.nickname.trim().charAt(0).toUpperCase();
  }, [authSession]);
  const runtimesQuery = useQuery({
    queryKey: ['runtimes'],
    queryFn: getRuntimes,
  });
  const currentProjectQuery = useQuery({
    queryKey: ['header-project-detail', currentProjectId],
    queryFn: async () => {
      if (!currentProjectId) {
        throw new Error('프로젝트 ID가 없습니다.');
      }

      const projectSession = await resolveProjectApiSession(
        undefined,
        currentProjectId,
      );

      return getProjectDetail(projectSession.accessToken, currentProjectId);
    },
    enabled: currentProjectId !== null,
  });
  const createProjectMutation = useMutation({
    mutationFn: async (values: ProjectCreateFormValues) => {
      const projectSession = await resolveProjectApiSession(values.projectType);
      const projectType = projectSession.userId
        ? values.projectType === 'GUEST'
          ? 'PERSONAL'
          : values.projectType
        : 'GUEST';
      const project = await createProject(projectSession.accessToken, {
        name: values.name,
        description: projectSession.userId
          ? '회원 프로젝트입니다.'
          : '게스트 임시 프로젝트입니다.',
        runtimeId: values.runtimeId,
        projectType,
        visibility: projectType === 'TEAM' ? 'TEAM' : 'PRIVATE',
        memberUserIds: values.memberUserIds,
      });

      saveProjectApiSession(project.id, projectSession);

      return { accessToken: projectSession.accessToken, project };
    },
    onSuccess: async ({ accessToken, project }) => {
      const fileTree = await getProjectFileTree(project.id);
      const rootDirectory = fileTree.find((file) => file.parentFileId === null);

      if (rootDirectory) {
        await createProjectFile(project.id, {
          parentFileId: rootDirectory.id,
          name: 'README.md',
          fileType: 'FILE',
        });
      }

      await queryClient.invalidateQueries({ queryKey: ['projects'] });
      await queryClient.invalidateQueries({ queryKey: ['workspace-projects'] });
      await openProject(accessToken, project.id);
      navigate(`/workspace?projectId=${project.id}`);
    },
    onError: () => {
      setCreateProjectError('프로젝트 생성에 실패했습니다.');
    },
  });
  const runtimeOptions =
    runtimesQuery.data?.map((runtime) => ({
      id: String(runtime.id),
      label: runtime.displayName,
      description: runtime.dockerImage,
      badge: getRuntimeBadge(runtime),
      meta: getRuntimeMeta(runtime),
    })) ?? [];
  const currentProjectName =
    currentProjectQuery.data?.name ??
    (currentProjectId ? '프로젝트 로딩 중' : '');
  const showTeamMenu =
    Boolean(authSession) && currentProjectQuery.data?.projectType === 'TEAM';

  useEffect(() => {
    return subscribeAuthSession(() => {
      setAuthSession(getStoredAuthSession());
    });
  }, []);

  const handleProfileButtonClick = () => {
    if (!authSession) {
      setIsAuthModalOpen(true);
      return;
    }

    setProfileMenuOpen((isOpen) => !isOpen);
  };

  const handleLogout = () => {
    clearAuthSession();
    setProfileMenuOpen(false);
  };

  const handleCreateProject = async (values: ProjectCreateFormValues) => {
    setCreateProjectError(null);
    await createProjectMutation.mutateAsync(values);
  };

  const handleOpenTeamManagement = (tab: TeamModalTab) => {
    setTeamManagementInitialTab(tab);
    setTeamManagementModalOpen(true);
  };

  return (
    <header className="relative flex h-11 w-full bg-[#333333] pl-3 text-sm text-[#cccccc] select-none">
      <div className="logo flex items-center">
        <img className="w-6.5" src={logo} alt="Logo" />
      </div>
      <div className="dropdown-menu relative flex h-full items-center pl-4">
        <ul className="flex h-full">
          <li className="group relative flex h-full items-center px-2">
            <p>Projects</p>
            <ul className="absolute top-full left-0 z-50 hidden min-w-52 cursor-pointer bg-[#252525] py-1 shadow-lg group-hover:block">
              <li>
                <button
                  type="button"
                  onClick={() => {
                    setCreateTemporaryProjectModalOpen(true);
                  }}
                  className="w-full px-3 py-1.5 text-left text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white"
                >
                  New Project
                </button>
              </li>
              <li>
                <button
                  type="button"
                  onClick={() => setOpenProjectsModalOpen(true)}
                  aria-haspopup="dialog"
                  aria-expanded={isOpenProjectsModalOpen}
                  className="w-full px-3 py-1.5 text-left text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white"
                >
                  Open Projects
                </button>
              </li>
            </ul>
          </li>
          {showTeamMenu ? (
            <li className="group relative flex h-full items-center px-2">
              <p>Teams</p>
              <ul className="absolute top-full left-0 z-50 hidden min-w-52 cursor-pointer bg-[#252525] py-1 shadow-lg group-hover:block">
                <li>
                  <button
                    type="button"
                    onClick={() => handleOpenTeamManagement('invite')}
                    className="w-full px-3 py-1.5 text-left text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white"
                  >
                    Invite Member
                  </button>
                </li>
                <li>
                  <button
                    type="button"
                    onClick={() => handleOpenTeamManagement('settings')}
                    className="w-full px-3 py-1.5 text-left text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white"
                  >
                    Team Settings
                  </button>
                </li>
                <li>
                  <button
                    type="button"
                    onClick={() => handleOpenTeamManagement('permissions')}
                    className="w-full px-3 py-1.5 text-left text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white"
                  >
                    Permissions
                  </button>
                </li>
              </ul>
            </li>
          ) : null}
        </ul>
      </div>
      <div className="project-name absolute left-1/2 flex h-full -translate-x-1/2 items-center px-4">
        <p className="max-w-[42vw] truncate">{currentProjectName}</p>
      </div>
      <div className="user-profile absolute right-3 flex h-full items-center">
        <button
          type="button"
          onClick={handleProfileButtonClick}
          aria-haspopup="dialog"
          aria-expanded={authSession ? isProfileMenuOpen : isAuthModalOpen}
          aria-label={authSession ? '프로필 메뉴 열기' : '인증 팝업 열기'}
          className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold text-white transition focus-visible:ring-2 focus-visible:ring-[#007acc]/70 focus-visible:outline-none ${
            authSession
              ? 'bg-[#0e639c] hover:bg-[#1177bb]'
              : 'bg-[#555555] hover:bg-[#6a6a6a]'
          }`}
        >
          {authSession ? (
            profileInitial
          ) : (
            <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={1.5}
              stroke="currentColor"
              className="h-5 w-5"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M15 7.5a3 3 0 11-6 0 3 3 0 016 0ZM4.5 19.5a7.5 7.5 0 0115 0"
              />
              <path strokeLinecap="round" d="M4.3 20.5h15.4" />
            </svg>
          )}
        </button>
        {authSession && isProfileMenuOpen ? (
          <div className="absolute top-full right-0 z-50 mt-1 w-56 border border-[#3c3c3c] bg-[#252526] py-2 shadow-lg">
            <div className="border-b border-[#3c3c3c] px-3 pb-2">
              <p className="truncate text-sm font-semibold text-[#f3f3f3]">
                {authSession.nickname}
              </p>
              <p className="mt-1 truncate text-xs text-[#858585]">
                {authSession.email}
              </p>
            </div>
            <button
              type="button"
              onClick={() => {
                setProfileMenuOpen(false);
                window.location.assign('/profile/setup');
              }}
              className="mt-1 w-full px-3 py-1.5 text-left text-sm text-[#cccccc] hover:bg-[#04395e] hover:text-white"
            >
              닉네임 설정
            </button>
            <button
              type="button"
              onClick={handleLogout}
              className="w-full px-3 py-1.5 text-left text-sm text-[#cccccc] hover:bg-[#04395e] hover:text-white"
            >
              로그아웃
            </button>
          </div>
        ) : null}
      </div>
      <AuthModal
        key={authModalKey}
        isOpen={isAuthModalOpen}
        pendingSignup={null}
        profileSetupSession={null}
        onClose={() => setIsAuthModalOpen(false)}
      />
      <OpenProjectsModal
        isOpen={isOpenProjectsModalOpen}
        onClose={() => setOpenProjectsModalOpen(false)}
      />
      <CreateTemporaryProjectModal
        key={
          authSession
            ? 'header-member-project-modal'
            : 'header-guest-project-modal'
        }
        accessToken={authSession?.accessToken}
        createError={createProjectError}
        initialPreviewMode={authSession ? 'member' : 'guest'}
        isOpen={isCreateTemporaryProjectModalOpen}
        isCreating={createProjectMutation.isPending}
        isRuntimeLoading={runtimesQuery.isLoading}
        onClose={() => setCreateTemporaryProjectModalOpen(false)}
        onCreateProject={handleCreateProject}
        runtimeOptions={runtimeOptions}
      />
      {authSession && currentProjectQuery.data?.projectType === 'TEAM' ? (
        <TeamManagementModal
          key={`${currentProjectQuery.data.id}-${teamManagementInitialTab}-${
            isTeamManagementModalOpen ? 'open' : 'closed'
          }`}
          accessToken={authSession.accessToken}
          initialTab={teamManagementInitialTab}
          isOpen={isTeamManagementModalOpen}
          onClose={() => setTeamManagementModalOpen(false)}
          onProjectRefresh={async () => {
            await currentProjectQuery.refetch();
            await queryClient.invalidateQueries({
              queryKey: ['project-detail', currentProjectId],
            });
          }}
          project={currentProjectQuery.data}
        />
      ) : null}
    </header>
  );
};
