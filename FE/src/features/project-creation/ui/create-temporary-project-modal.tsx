import { useEffect, useState, type MouseEvent } from 'react';

import {
  currentProjectOwner,
  invitableMembers,
  projectCreationPreviewModeOptions,
  projectRoleOptions,
  projectTypeOptions,
  runtimeOptions,
  type ProjectCreationPreviewMode,
  type ProjectRole,
  type ProjectType,
  type RuntimeId,
  type SelectedMember,
} from '../model/project-creation';

type CreateTemporaryProjectModalProps = {
  isOpen: boolean;
  onClose: () => void;
};

const handleDialogClick = (event: MouseEvent<HTMLDivElement>) => {
  event.stopPropagation();
};

const searchInputClassName =
  'w-full rounded-lg border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2.5 text-sm text-[#d4d4d4] transition placeholder:text-[#6b7280] focus:border-[#007acc] focus:outline-none focus:ring-2 focus:ring-[#007acc]/25';

const roleLabelMap: Record<ProjectRole, string> = {
  owner: 'Owner',
  editor: 'Editor',
  viewer: 'Viewer',
};

const roleToneClassNameMap: Record<ProjectRole, string> = {
  owner:
    'border-[#0e639c]/70 bg-[#0e639c]/15 text-[#7fd6ff] shadow-[inset_0_0_0_1px_rgba(14,99,156,0.2)]',
  editor:
    'border-[#4ec9b0]/40 bg-[#4ec9b0]/10 text-[#94f1de] shadow-[inset_0_0_0_1px_rgba(78,201,176,0.16)]',
  viewer:
    'border-[#c5c5c5]/20 bg-[#c5c5c5]/8 text-[#d4d4d4] shadow-[inset_0_0_0_1px_rgba(197,197,197,0.1)]',
};

const runtimeAccentClassNameMap: Record<RuntimeId, string> = {
  nodejs: 'text-[#4ec9b0]',
  python: 'text-[#ffd866]',
  java: 'text-[#ff8f6b]',
  cpp: 'text-[#9cdcfe]',
};

export const CreateTemporaryProjectModal = ({
  isOpen,
  onClose,
}: CreateTemporaryProjectModalProps) => {
  const [previewMode, setPreviewMode] =
    useState<ProjectCreationPreviewMode>('guest');
  const [projectTitle, setProjectTitle] = useState('');
  const [projectType, setProjectType] = useState<ProjectType>('personal');
  const [selectedRuntime, setSelectedRuntime] = useState<RuntimeId>('nodejs');
  const [memberSearchQuery, setMemberSearchQuery] = useState('');
  const [selectedMembers, setSelectedMembers] = useState<SelectedMember[]>([]);

  const resetModalState = () => {
    setPreviewMode('guest');
    setProjectTitle('');
    setProjectType('personal');
    setSelectedRuntime('nodejs');
    setMemberSearchQuery('');
    setSelectedMembers([]);
  };

  const handleClose = () => {
    resetModalState();
    onClose();
  };

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    const previousOverflow = document.body.style.overflow;

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        resetModalState();
        onClose();
      }
    };

    document.body.style.overflow = 'hidden';
    window.addEventListener('keydown', handleEscape);

    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener('keydown', handleEscape);
    };
  }, [isOpen, onClose]);

  if (!isOpen) {
    return null;
  }

  const isGuestPreview = previewMode === 'guest';
  const effectiveProjectType: ProjectType = isGuestPreview
    ? 'personal'
    : projectType;
  const normalizedQuery = memberSearchQuery.trim().toLowerCase();
  const searchableMembers = invitableMembers.filter((member) => {
    if (
      selectedMembers.some((selectedMember) => selectedMember.id === member.id)
    ) {
      return false;
    }

    if (!normalizedQuery) {
      return true;
    }

    return (
      member.name.toLowerCase().includes(normalizedQuery) ||
      member.email.toLowerCase().includes(normalizedQuery) ||
      member.team.toLowerCase().includes(normalizedQuery)
    );
  });
  const visibleSearchResults = searchableMembers.slice(0, 5);
  const selectedRuntimeOption = runtimeOptions.find(
    (runtimeOption) => runtimeOption.id === selectedRuntime,
  );
  const teamMemberCount =
    effectiveProjectType === 'team' ? selectedMembers.length + 1 : 1;
  const isCreateDisabled = projectTitle.trim().length === 0;
  const projectTitleDisplay = projectTitle.trim() || '제목 미설정';

  const handleProjectTypeChange = (nextType: ProjectType) => {
    setProjectType(nextType);

    if (nextType === 'personal') {
      setMemberSearchQuery('');
      setSelectedMembers([]);
    }
  };

  const handleAddMember = (memberId: string) => {
    const memberToInvite = invitableMembers.find(
      (member) => member.id === memberId,
    );

    if (!memberToInvite) {
      return;
    }

    setSelectedMembers((previousMembers) => [
      ...previousMembers,
      { ...memberToInvite, role: 'editor' },
    ]);
    setMemberSearchQuery('');
  };

  const handleRemoveMember = (memberId: string) => {
    setSelectedMembers((previousMembers) =>
      previousMembers.filter((member) => member.id !== memberId),
    );
  };

  const handleRoleChange = (memberId: string, role: ProjectRole) => {
    setSelectedMembers((previousMembers) =>
      previousMembers.map((member) =>
        member.id === memberId ? { ...member, role } : member,
      ),
    );
  };

  const handleCreateProject = () => {
    handleClose();
  };

  return (
    <div
      className="fixed inset-0 z-[120] flex items-center justify-center bg-[rgba(0,0,0,0.72)] px-4 py-4 backdrop-blur-[2px] sm:py-6"
      onClick={handleClose}
      role="presentation"
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="create-temporary-project-title"
        className="flex max-h-[calc(100dvh-2rem)] w-full max-w-6xl flex-col overflow-hidden rounded-2xl border border-[#313131] bg-[#1e1e1e] shadow-[0_28px_90px_rgba(0,0,0,0.58)] sm:max-h-[calc(100dvh-3rem)]"
        onClick={handleDialogClick}
      >
        <div className="flex items-start justify-between gap-4 border-b border-[#313131] bg-[linear-gradient(180deg,#252526_0%,#1f1f1f_100%)] px-5 py-4 sm:px-6">
          <div className="min-w-0 flex-1">
            <span className="inline-flex items-center rounded-md border border-[#3c3c3c] bg-[#1e1e1e] px-2 py-1 text-[10px] font-semibold tracking-[0.2em] text-[#4fc1ff] uppercase">
              Temporary Workspace
            </span>
            <h2
              id="create-temporary-project-title"
              className="mt-2 text-lg font-semibold text-[#f3f3f3] sm:text-xl"
            >
              새 임시 프로젝트 만들기
            </h2>
            <p className="mt-1 max-w-2xl text-sm leading-6 text-[#9da1a6]">
              회원 기능 연동 전까지는 상단 탭으로 게스트 화면과 회원 화면을 모두
              확인할 수 있습니다.
            </p>

            <div className="mt-4 flex flex-col gap-3">
              <div className="inline-flex w-full max-w-xl rounded-xl border border-[#313131] bg-[#1b1b1c] p-1">
                {projectCreationPreviewModeOptions.map((option) => {
                  const isActive = option.id === previewMode;

                  return (
                    <button
                      key={option.id}
                      type="button"
                      onClick={() => setPreviewMode(option.id)}
                      className={`flex-1 rounded-lg px-3 py-2.5 text-left transition ${
                        isActive
                          ? 'bg-[#0e639c] text-white shadow-[0_10px_24px_rgba(14,99,156,0.26)]'
                          : 'text-[#9da1a6] hover:bg-[#252526] hover:text-[#d4d4d4]'
                      }`}
                    >
                      <p className="text-sm font-semibold">{option.label}</p>
                      <p
                        className={`mt-1 text-xs leading-5 ${
                          isActive ? 'text-[#d8efff]' : 'text-[#6b7280]'
                        }`}
                      >
                        {option.description}
                      </p>
                    </button>
                  );
                })}
              </div>

              <p className="text-xs text-[#6b7280]">
                실제 연동 전 임시 프리뷰용 탭입니다. 현재 선택된 화면 기준으로
                생성 플로우를 확인할 수 있습니다.
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={handleClose}
            className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-md border border-transparent text-[#9da1a6] transition hover:border-[#3c3c3c] hover:bg-[#2a2d2e] hover:text-white focus-visible:ring-2 focus-visible:ring-[#007acc]/70 focus-visible:outline-none"
            aria-label="임시 프로젝트 생성 팝업 닫기"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              className="h-4 w-4"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M6 6l12 12"
              />
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M18 6L6 18"
              />
            </svg>
          </button>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto">
          <div className="grid gap-6 px-5 py-5 sm:px-6 sm:py-6 xl:grid-cols-[minmax(0,1.35fr)_minmax(320px,0.65fr)]">
            <div className="space-y-5">
              <section className="rounded-2xl border border-[#313131] bg-[#252526] p-4">
                <div className="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
                  <div>
                    <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
                      Project Basics
                    </p>
                    <h3 className="mt-2 text-base font-semibold text-[#f3f3f3]">
                      프로젝트 제목 설정
                    </h3>
                  </div>
                  <p className="text-xs text-[#858585]">
                    게스트와 회원 모두 공통으로 설정
                  </p>
                </div>

                <div className="mt-4 grid gap-4 lg:grid-cols-[minmax(0,1fr)_minmax(220px,0.7fr)]">
                  <label className="block">
                    <span className="mb-2 block text-xs font-medium text-[#c8c8c8]">
                      프로젝트 제목
                    </span>
                    <input
                      type="text"
                      value={projectTitle}
                      onChange={(event) => setProjectTitle(event.target.value)}
                      className={searchInputClassName}
                      placeholder="예: sprint-hotfix-lab"
                    />
                  </label>

                  <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-4 py-3">
                    <p className="text-xs font-semibold tracking-[0.16em] text-[#858585] uppercase">
                      Current Mode
                    </p>
                    <p className="mt-2 text-sm font-semibold text-[#f3f3f3]">
                      {isGuestPreview
                        ? '게스트 프로젝트 생성 화면'
                        : '회원 프로젝트 생성 화면'}
                    </p>
                    <p className="mt-2 text-sm leading-6 text-[#9da1a6]">
                      {isGuestPreview
                        ? '게스트는 개인 임시 프로젝트만 생성할 수 있고, 사용자 초대는 제공되지 않습니다.'
                        : '회원은 개인 또는 팀 프로젝트를 선택하고, 팀 프로젝트일 때 초대와 권한 설정을 진행할 수 있습니다.'}
                    </p>
                  </div>
                </div>
              </section>

              <section className="rounded-2xl border border-[#313131] bg-[#252526] p-4">
                <div className="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
                  <div>
                    <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
                      Project Type
                    </p>
                    <h3 className="mt-2 text-base font-semibold text-[#f3f3f3]">
                      프로젝트 유형 선택
                    </h3>
                  </div>
                  <p className="text-xs text-[#858585]">
                    {isGuestPreview
                      ? '게스트는 개인 프로젝트만 생성 가능'
                      : '개인 또는 팀 협업용 임시 공간'}
                  </p>
                </div>

                {isGuestPreview ? (
                  <div className="mt-4 grid gap-3 md:grid-cols-2">
                    {projectTypeOptions.map((option) => {
                      const isPersonal = option.id === 'personal';

                      return (
                        <div
                          key={option.id}
                          className={`rounded-xl border px-4 py-4 ${
                            isPersonal
                              ? 'border-[#0e639c] bg-[#0e639c]/12 shadow-[inset_0_0_0_1px_rgba(14,99,156,0.28)]'
                              : 'border-[#313131] bg-[#1b1b1c] opacity-60'
                          }`}
                        >
                          <div className="flex items-start justify-between gap-3">
                            <div>
                              <p className="text-sm font-semibold text-[#f3f3f3]">
                                {option.label}
                              </p>
                              <p className="mt-2 text-sm leading-6 text-[#9da1a6]">
                                {isPersonal
                                  ? option.description
                                  : '로그인 후 회원 화면에서만 팀 프로젝트와 초대 기능을 사용할 수 있습니다.'}
                              </p>
                            </div>
                            <span
                              className={`inline-flex rounded-full border px-2.5 py-1 text-[11px] font-semibold ${
                                isPersonal
                                  ? 'border-[#0e639c]/60 bg-[#0e639c]/18 text-[#7fd6ff]'
                                  : 'border-[#3c3c3c] bg-[#252526] text-[#858585]'
                              }`}
                            >
                              {isPersonal ? 'Available' : 'Members Only'}
                            </span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <div className="mt-4 grid gap-3 md:grid-cols-2">
                    {projectTypeOptions.map((option) => {
                      const isActive = option.id === projectType;

                      return (
                        <button
                          key={option.id}
                          type="button"
                          onClick={() => handleProjectTypeChange(option.id)}
                          className={`rounded-xl border px-4 py-4 text-left transition ${
                            isActive
                              ? 'border-[#0e639c] bg-[#0e639c]/12 shadow-[inset_0_0_0_1px_rgba(14,99,156,0.28)]'
                              : 'border-[#313131] bg-[#1e1e1e] hover:border-[#3c3c3c] hover:bg-[#232326]'
                          }`}
                        >
                          <div className="flex items-start justify-between gap-3">
                            <div>
                              <p className="text-sm font-semibold text-[#f3f3f3]">
                                {option.label}
                              </p>
                              <p className="mt-2 text-sm leading-6 text-[#9da1a6]">
                                {option.description}
                              </p>
                            </div>
                            <span
                              className={`mt-0.5 inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-full border ${
                                isActive
                                  ? 'border-[#4fc1ff] bg-[#0e639c] text-white'
                                  : 'border-[#4b4b4f] bg-transparent text-transparent'
                              }`}
                              aria-hidden="true"
                            >
                              <svg
                                xmlns="http://www.w3.org/2000/svg"
                                viewBox="0 0 16 16"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                                className="h-3 w-3"
                              >
                                <path
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                  d="m3.5 8 2.5 2.5L12.5 4"
                                />
                              </svg>
                            </span>
                          </div>
                        </button>
                      );
                    })}
                  </div>
                )}
              </section>

              {!isGuestPreview && projectType === 'team' ? (
                <section className="rounded-2xl border border-[#313131] bg-[#252526] p-4">
                  <div className="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
                    <div>
                      <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
                        Team Invite
                      </p>
                      <h3 className="mt-2 text-base font-semibold text-[#f3f3f3]">
                        검색으로 팀원 추가
                      </h3>
                    </div>
                    <p className="text-xs text-[#858585]">
                      이름, 이메일, 팀명으로 검색
                    </p>
                  </div>

                  <div className="mt-4 grid gap-4 lg:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)]">
                    <div className="space-y-3">
                      <label className="block">
                        <span className="mb-2 block text-xs font-medium text-[#c8c8c8]">
                          사용자 검색
                        </span>
                        <input
                          type="text"
                          value={memberSearchQuery}
                          onChange={(event) =>
                            setMemberSearchQuery(event.target.value)
                          }
                          className={searchInputClassName}
                          placeholder="예: mira, platform, devide.team"
                        />
                      </label>

                      <div className="rounded-xl border border-[#313131] bg-[#1e1e1e]">
                        <div className="flex items-center justify-between border-b border-[#313131] px-3 py-2.5">
                          <p className="text-xs font-semibold tracking-[0.16em] text-[#858585] uppercase">
                            Search Results
                          </p>
                          <span className="text-xs text-[#6b7280]">
                            {visibleSearchResults.length}명 표시
                          </span>
                        </div>

                        <div className="max-h-72 overflow-y-auto p-2">
                          {visibleSearchResults.length > 0 ? (
                            <div className="space-y-2">
                              {visibleSearchResults.map((member) => (
                                <div
                                  key={member.id}
                                  className="flex items-center gap-3 rounded-lg border border-[#2d2d30] bg-[#252526] px-3 py-3"
                                >
                                  <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-[#3c3c3c] bg-[#1b1b1c] text-sm font-semibold text-[#4fc1ff]">
                                    {member.name
                                      .split(' ')
                                      .map((namePart) => namePart[0])
                                      .join('')
                                      .slice(0, 2)}
                                  </div>
                                  <div className="min-w-0 flex-1">
                                    <p className="truncate text-sm font-semibold text-[#f3f3f3]">
                                      {member.name}
                                    </p>
                                    <p className="truncate text-xs text-[#858585]">
                                      {member.email}
                                    </p>
                                    <p className="mt-1 text-xs text-[#6b7280]">
                                      {member.team} · {member.status}
                                    </p>
                                  </div>
                                  <button
                                    type="button"
                                    onClick={() => handleAddMember(member.id)}
                                    className="inline-flex shrink-0 items-center rounded-md border border-[#0e639c]/40 bg-[#0e639c]/12 px-3 py-2 text-xs font-semibold text-[#7fd6ff] transition hover:border-[#0e639c] hover:bg-[#0e639c]/20"
                                  >
                                    추가
                                  </button>
                                </div>
                              ))}
                            </div>
                          ) : (
                            <div className="rounded-lg border border-dashed border-[#3c3c3c] bg-[#1b1b1c] px-4 py-8 text-center">
                              <p className="text-sm text-[#9da1a6]">
                                검색 조건에 맞는 사용자가 없습니다.
                              </p>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="space-y-3">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-xs font-semibold tracking-[0.16em] text-[#858585] uppercase">
                            Invited Members
                          </p>
                          <p className="mt-1 text-sm text-[#9da1a6]">
                            추가 후 권한 구조를 바로 조정할 수 있습니다.
                          </p>
                        </div>
                        <span className="rounded-md border border-[#3c3c3c] bg-[#1e1e1e] px-2 py-1 text-[11px] font-semibold text-[#d4d4d4]">
                          총 {teamMemberCount}명
                        </span>
                      </div>

                      <div className="space-y-3 rounded-xl border border-[#313131] bg-[#1e1e1e] p-3">
                        <div className="rounded-xl border border-[#0e639c]/30 bg-[#0e639c]/10 px-3 py-3">
                          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                            <div>
                              <p className="text-sm font-semibold text-[#f3f3f3]">
                                {currentProjectOwner.name}
                              </p>
                              <p className="mt-1 text-xs text-[#9da1a6]">
                                {currentProjectOwner.email}
                              </p>
                              <p className="mt-1 text-xs text-[#6b7280]">
                                {currentProjectOwner.status}
                              </p>
                            </div>
                            <span
                              className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold ${roleToneClassNameMap.owner}`}
                            >
                              Owner
                            </span>
                          </div>
                        </div>

                        {selectedMembers.length > 0 ? (
                          <div className="space-y-2">
                            {selectedMembers.map((member) => (
                              <div
                                key={member.id}
                                className="rounded-xl border border-[#2d2d30] bg-[#252526] px-3 py-3"
                              >
                                <div className="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
                                  <div className="min-w-0">
                                    <p className="truncate text-sm font-semibold text-[#f3f3f3]">
                                      {member.name}
                                    </p>
                                    <p className="mt-1 truncate text-xs text-[#858585]">
                                      {member.email}
                                    </p>
                                    <p className="mt-1 text-xs text-[#6b7280]">
                                      {member.team} · {member.status}
                                    </p>
                                  </div>

                                  <div className="flex flex-wrap items-center gap-2">
                                    <label className="min-w-36">
                                      <span className="sr-only">권한 선택</span>
                                      <select
                                        value={member.role}
                                        onChange={(event) =>
                                          handleRoleChange(
                                            member.id,
                                            event.target.value as ProjectRole,
                                          )
                                        }
                                        className="w-full rounded-md border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2 text-sm text-[#d4d4d4] focus:border-[#007acc] focus:ring-2 focus:ring-[#007acc]/25 focus:outline-none"
                                      >
                                        {projectRoleOptions.map(
                                          (roleOption) => (
                                            <option
                                              key={roleOption.id}
                                              value={roleOption.id}
                                            >
                                              {roleOption.label} ·{' '}
                                              {roleOption.description}
                                            </option>
                                          ),
                                        )}
                                      </select>
                                    </label>

                                    <span
                                      className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold ${roleToneClassNameMap[member.role]}`}
                                    >
                                      {roleLabelMap[member.role]}
                                    </span>

                                    <button
                                      type="button"
                                      onClick={() =>
                                        handleRemoveMember(member.id)
                                      }
                                      className="inline-flex items-center rounded-md border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2 text-xs font-semibold text-[#d4d4d4] transition hover:border-[#4b4b4f] hover:bg-[#2a2a2d]"
                                    >
                                      제거
                                    </button>
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <div className="rounded-xl border border-dashed border-[#3c3c3c] bg-[#1b1b1c] px-4 py-8 text-center">
                            <p className="text-sm text-[#9da1a6]">
                              아직 초대된 팀원이 없습니다. 왼쪽 검색 영역에서
                              사용자를 추가하세요.
                            </p>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </section>
              ) : null}

              <section className="rounded-2xl border border-[#313131] bg-[#252526] p-4">
                <div className="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
                  <div>
                    <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
                      Runtime
                    </p>
                    <h3 className="mt-2 text-base font-semibold text-[#f3f3f3]">
                      런타임 선택
                    </h3>
                  </div>
                  <p className="text-xs text-[#858585]">
                    임시 프로젝트 시작 시 적용될 기본 환경
                  </p>
                </div>

                <div className="mt-4 grid gap-3 md:grid-cols-2">
                  {runtimeOptions.map((runtimeOption) => {
                    const isActive = runtimeOption.id === selectedRuntime;

                    return (
                      <button
                        key={runtimeOption.id}
                        type="button"
                        onClick={() => setSelectedRuntime(runtimeOption.id)}
                        className={`rounded-xl border px-4 py-4 text-left transition ${
                          isActive
                            ? 'border-[#0e639c] bg-[#0e639c]/10 shadow-[inset_0_0_0_1px_rgba(14,99,156,0.24)]'
                            : 'border-[#313131] bg-[#1e1e1e] hover:border-[#3c3c3c] hover:bg-[#232326]'
                        }`}
                      >
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <div className="flex items-center gap-3">
                              <span
                                className={`inline-flex rounded-md border border-[#3c3c3c] bg-[#1b1b1c] px-2 py-1 text-[11px] font-semibold tracking-[0.16em] uppercase ${runtimeAccentClassNameMap[runtimeOption.id]}`}
                              >
                                {runtimeOption.badge}
                              </span>
                              <p className="text-sm font-semibold text-[#f3f3f3]">
                                {runtimeOption.label}
                              </p>
                            </div>
                            <p className="mt-3 text-sm leading-6 text-[#9da1a6]">
                              {runtimeOption.description}
                            </p>
                          </div>
                          <span
                            className={`mt-0.5 inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-full border ${
                              isActive
                                ? 'border-[#4fc1ff] bg-[#0e639c] text-white'
                                : 'border-[#4b4b4f] bg-transparent text-transparent'
                            }`}
                            aria-hidden="true"
                          >
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              viewBox="0 0 16 16"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2"
                              className="h-3 w-3"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                d="m3.5 8 2.5 2.5L12.5 4"
                              />
                            </svg>
                          </span>
                        </div>
                      </button>
                    );
                  })}
                </div>
              </section>
            </div>

            <aside className="space-y-5">
              <section className="rounded-2xl border border-[#313131] bg-[linear-gradient(180deg,#252526_0%,#202123_100%)] p-4">
                <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
                  Summary
                </p>
                <h3 className="mt-2 text-base font-semibold text-[#f3f3f3]">
                  생성 예정 설정
                </h3>

                <div className="mt-4 grid gap-3">
                  <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-4 py-3">
                    <p className="text-xs text-[#858585]">생성 화면</p>
                    <p className="mt-1 text-sm font-semibold text-[#f3f3f3]">
                      {isGuestPreview ? '게스트' : '회원'}
                    </p>
                  </div>
                  <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-4 py-3">
                    <p className="text-xs text-[#858585]">프로젝트 제목</p>
                    <p className="mt-1 text-sm font-semibold text-[#f3f3f3]">
                      {projectTitleDisplay}
                    </p>
                  </div>
                  <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-4 py-3">
                    <p className="text-xs text-[#858585]">프로젝트 유형</p>
                    <p className="mt-1 text-sm font-semibold text-[#f3f3f3]">
                      {effectiveProjectType === 'personal'
                        ? '개인 프로젝트'
                        : '팀 프로젝트'}
                    </p>
                  </div>
                  <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-4 py-3">
                    <p className="text-xs text-[#858585]">참여 인원</p>
                    <p className="mt-1 text-sm font-semibold text-[#f3f3f3]">
                      {teamMemberCount}명
                    </p>
                  </div>
                  <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-4 py-3">
                    <p className="text-xs text-[#858585]">선택 런타임</p>
                    <p className="mt-1 text-sm font-semibold text-[#f3f3f3]">
                      {selectedRuntimeOption?.label}
                    </p>
                    <p className="mt-1 text-xs leading-5 text-[#6b7280]">
                      {selectedRuntimeOption?.description}
                    </p>
                  </div>
                </div>
              </section>

              {isGuestPreview ? (
                <section className="rounded-2xl border border-[#313131] bg-[#252526] p-4">
                  <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
                    Guest Limits
                  </p>
                  <h3 className="mt-2 text-base font-semibold text-[#f3f3f3]">
                    게스트 생성 제한
                  </h3>

                  <div className="mt-4 grid gap-3">
                    <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-4 py-3">
                      <p className="text-sm font-semibold text-[#f3f3f3]">
                        개인 프로젝트만 생성 가능
                      </p>
                      <p className="mt-1 text-sm leading-6 text-[#9da1a6]">
                        팀 프로젝트 유형 선택은 비활성화되며, 협업용 임시 공간은
                        회원 전용으로 분리됩니다.
                      </p>
                    </div>
                    <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-4 py-3">
                      <p className="text-sm font-semibold text-[#f3f3f3]">
                        사용자 초대 불가
                      </p>
                      <p className="mt-1 text-sm leading-6 text-[#9da1a6]">
                        검색 기반 사용자 추가와 권한 설정 UI는 게스트 화면에서
                        제공되지 않습니다.
                      </p>
                    </div>
                    <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-4 py-3">
                      <p className="text-sm font-semibold text-[#f3f3f3]">
                        세션 기반 임시 저장
                      </p>
                      <p className="mt-1 text-sm leading-6 text-[#9da1a6]">
                        브라우저 세션 중심으로 빠르게 실험하는 흐름에 맞춘
                        화면입니다.
                      </p>
                    </div>
                  </div>
                </section>
              ) : (
                <section className="rounded-2xl border border-[#313131] bg-[#252526] p-4">
                  <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
                    Roles
                  </p>
                  <h3 className="mt-2 text-base font-semibold text-[#f3f3f3]">
                    권한 구조
                  </h3>

                  <div className="mt-4 space-y-3">
                    {projectRoleOptions.map((roleOption) => (
                      <div
                        key={roleOption.id}
                        className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-4 py-3"
                      >
                        <div className="flex items-center justify-between gap-3">
                          <span
                            className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold ${roleToneClassNameMap[roleOption.id]}`}
                          >
                            {roleOption.label}
                          </span>
                          <p className="text-sm text-[#9da1a6]">
                            {roleOption.description}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>

                  <div className="mt-4 rounded-xl border border-dashed border-[#3c3c3c] bg-[#1b1b1c] px-4 py-4">
                    <p className="text-sm leading-6 text-[#9da1a6]">
                      팀 프로젝트에서는 생성자가 기본 Owner로 설정되며, 초대한
                      팀원은 추가 후 즉시 권한을 변경할 수 있습니다.
                    </p>
                  </div>
                </section>
              )}
            </aside>
          </div>
        </div>

        <div className="flex flex-col gap-3 border-t border-[#313131] bg-[#181818] px-5 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
          <p className="text-sm text-[#858585]">
            {isGuestPreview
              ? '게스트 화면은 개인 임시 프로젝트 중심이며, 팀 협업 기능은 제공되지 않습니다.'
              : '회원 화면은 팀 협업을 포함한 임시 프로젝트 생성 흐름을 미리 확인할 수 있습니다.'}
          </p>
          <div className="flex flex-col gap-3 sm:flex-row">
            <button
              type="button"
              onClick={handleClose}
              className="inline-flex items-center justify-center rounded-md border border-[#3c3c3c] bg-[#2d2d30] px-4 py-2.5 text-sm font-semibold text-[#d4d4d4] transition hover:bg-[#343438]"
            >
              취소
            </button>
            <button
              type="button"
              onClick={handleCreateProject}
              disabled={isCreateDisabled}
              className={`inline-flex items-center justify-center rounded-md px-4 py-2.5 text-sm font-semibold transition ${
                isCreateDisabled
                  ? 'cursor-not-allowed bg-[#3b3b3b] text-[#858585]'
                  : 'bg-[#0e639c] text-white hover:bg-[#1177bb]'
              }`}
            >
              {isGuestPreview
                ? '게스트 임시 프로젝트 생성'
                : '회원 임시 프로젝트 생성'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
