import { useEffect, useState, type MouseEvent } from 'react';

import {
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

const panelClassName = 'rounded-xl border border-[#313131] bg-[#252526] p-4';

const inputClassName =
  'w-full rounded-lg border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2.5 text-sm text-[#d4d4d4] transition placeholder:text-[#6b7280] focus:border-[#007acc] focus:outline-none focus:ring-2 focus:ring-[#007acc]/25';

const summaryRowClassName =
  'flex items-center justify-between gap-3 rounded-lg border border-[#313131] bg-[#1e1e1e] px-3 py-2 text-[13px]';

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

const projectTypeMetaMap: Record<ProjectType, string> = {
  personal: '혼자 작업',
  team: '멤버 초대',
};

const runtimeMetaMap: Record<RuntimeId, string> = {
  nodejs: 'JS/TS',
  python: '스크립트',
  java: 'JVM',
  cpp: 'Native',
};

const roleCapabilityMap: Record<ProjectRole, string> = {
  owner: '설정/멤버 관리',
  editor: '코드 편집/실행',
  viewer: '읽기 전용',
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

  useEffect(
    function manageCreateTemporaryProjectModalEffect() {
      if (!isOpen) {
        return;
      }

      const previousOverflow = document.body.style.overflow;

      function handleEscapeKeydown(event: KeyboardEvent) {
        if (event.key === 'Escape') {
          resetModalState();
          onClose();
        }
      }

      document.body.style.overflow = 'hidden';
      window.addEventListener('keydown', handleEscapeKeydown);

      return function cleanupCreateTemporaryProjectModalEffect() {
        document.body.style.overflow = previousOverflow;
        window.removeEventListener('keydown', handleEscapeKeydown);
      };
    },
    [isOpen, onClose],
  );

  if (!isOpen) {
    return null;
  }

  const isGuestPreview = previewMode === 'guest';
  const previewModeLabel = isGuestPreview ? '게스트' : '회원';
  const effectiveProjectType: ProjectType = isGuestPreview
    ? 'personal'
    : projectType;
  const selectedRuntimeOption = runtimeOptions.find(
    (runtimeOption) => runtimeOption.id === selectedRuntime,
  );
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
  const teamMemberCount =
    effectiveProjectType === 'team' ? selectedMembers.length + 1 : 1;
  const isCreateDisabled = projectTitle.trim().length === 0;
  const projectTitleDisplay = projectTitle.trim() || '제목 없음';
  const showTeamSection = !isGuestPreview && effectiveProjectType === 'team';

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
        className="flex max-h-[calc(100dvh-2rem)] w-full max-w-5xl flex-col overflow-hidden rounded-2xl border border-[#313131] bg-[#1e1e1e] shadow-[0_28px_90px_rgba(0,0,0,0.58)] sm:max-h-[calc(100dvh-3rem)] xl:h-[calc(100dvh-3rem)] xl:max-h-[788px]"
        onClick={handleDialogClick}
      >
        <div className="border-b border-[#313131] bg-[linear-gradient(180deg,#252526_0%,#1f1f1f_100%)] px-5 py-4 sm:px-6">
          <div className="flex items-start justify-between gap-4">
            <div className="min-w-0">
              <div className="flex items-center gap-2">
                <span className="inline-flex items-center rounded-md border border-[#3c3c3c] bg-[#1e1e1e] px-2 py-1 text-[10px] font-semibold tracking-[0.2em] text-[#4fc1ff] uppercase">
                  Temporary Workspace
                </span>
              </div>
              <div className="mt-2 flex flex-wrap items-center gap-3">
                <h2
                  id="create-temporary-project-title"
                  className="text-lg font-semibold text-[#f3f3f3] sm:text-xl"
                >
                  새 임시 프로젝트
                </h2>

                <div className="inline-flex rounded-xl border border-[#313131] bg-[#1b1b1c] p-1">
                  {projectCreationPreviewModeOptions.map((option) => {
                    const isActive = option.id === previewMode;

                    return (
                      <button
                        key={option.id}
                        type="button"
                        onClick={() => setPreviewMode(option.id)}
                        className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition sm:px-3.5 sm:text-sm ${
                          isActive
                            ? 'bg-[#0e639c] text-white'
                            : 'text-[#9da1a6] hover:bg-[#252526] hover:text-[#d4d4d4]'
                        }`}
                      >
                        {option.label}
                      </button>
                    );
                  })}
                </div>
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
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto xl:overflow-hidden">
          <div className="grid gap-5 px-5 py-5 sm:px-6 sm:py-6 xl:h-full xl:grid-cols-[minmax(0,1fr)_280px]">
            <div className="space-y-4 xl:flex xl:h-full xl:min-h-0 xl:flex-col">
              <section className={panelClassName}>
                <div className="flex items-center justify-between gap-3">
                  <h3 className="text-sm font-semibold text-[#f3f3f3]">
                    프로젝트 제목
                  </h3>
                </div>
                <label className="mt-3 block">
                  <span className="sr-only">프로젝트 제목</span>
                  <input
                    type="text"
                    value={projectTitle}
                    onChange={(event) => setProjectTitle(event.target.value)}
                    className={inputClassName}
                    placeholder="예: sprint-hotfix-lab"
                  />
                </label>
              </section>

              <section className={panelClassName}>
                <div className="flex items-center justify-between gap-3">
                  <h3 className="text-sm font-semibold text-[#f3f3f3]">
                    프로젝트 유형
                  </h3>
                  {isGuestPreview ? (
                    <span className="text-xs text-[#858585]">
                      회원 전용 기능 제한
                    </span>
                  ) : null}
                </div>

                <div className="mt-3 grid gap-3 sm:grid-cols-2">
                  {projectTypeOptions.map((option) => {
                    const isDisabled = isGuestPreview && option.id === 'team';
                    const isActive = effectiveProjectType === option.id;

                    if (isDisabled) {
                      return (
                        <div
                          key={option.id}
                          className="rounded-xl border border-[#313131] bg-[#1b1b1c] px-4 py-4 opacity-60"
                        >
                          <div className="flex items-center justify-between gap-3">
                            <div>
                              <p className="text-sm font-semibold text-[#f3f3f3]">
                                {option.label}
                              </p>
                              <p className="mt-1 text-xs text-[#858585]">
                                회원 전용
                              </p>
                            </div>
                            <span className="rounded-full border border-[#3c3c3c] bg-[#252526] px-2.5 py-1 text-[11px] font-semibold text-[#858585]">
                              Locked
                            </span>
                          </div>
                        </div>
                      );
                    }

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
                        <div className="flex items-center justify-between gap-3">
                          <div>
                            <p className="text-sm font-semibold text-[#f3f3f3]">
                              {option.label}
                            </p>
                            <p className="mt-1 text-xs text-[#858585]">
                              {projectTypeMetaMap[option.id]}
                            </p>
                          </div>
                          <span
                            className={`inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-full border ${
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

              {showTeamSection ? (
                <section
                  className={`${panelClassName} xl:flex xl:min-h-0 xl:flex-1 xl:flex-col xl:overflow-hidden`}
                >
                  <div className="flex items-center justify-between gap-3">
                    <h3 className="text-sm font-semibold text-[#f3f3f3]">
                      팀 초대
                    </h3>
                    <span className="text-xs text-[#858585]">
                      총 {teamMemberCount}명 · 생성자 포함
                    </span>
                  </div>

                  <div className="mt-3 grid gap-2 sm:grid-cols-3">
                    {projectRoleOptions.map((roleOption) => (
                      <div
                        key={roleOption.id}
                        className="flex items-center justify-between gap-2 rounded-lg border border-[#313131] bg-[#1e1e1e] px-3 py-2"
                      >
                        <span
                          className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold ${roleToneClassNameMap[roleOption.id]}`}
                        >
                          {roleOption.label}
                        </span>
                        <p className="text-[11px] text-[#9da1a6]">
                          {roleCapabilityMap[roleOption.id]}
                        </p>
                      </div>
                    ))}
                  </div>

                  <div className="mt-3 grid gap-4 xl:min-h-0 xl:flex-1 xl:grid-cols-[minmax(0,0.92fr)_minmax(0,1.08fr)]">
                    <div className="space-y-3 xl:flex xl:min-h-0 xl:flex-col">
                      <input
                        type="text"
                        value={memberSearchQuery}
                        onChange={(event) =>
                          setMemberSearchQuery(event.target.value)
                        }
                        className={inputClassName}
                        placeholder="이름, 이메일, 팀명 검색"
                      />

                      <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] p-2 xl:min-h-0 xl:flex-1 xl:overflow-hidden">
                        {visibleSearchResults.length > 0 ? (
                          <div className="space-y-2 xl:h-full xl:overflow-y-auto xl:pr-1">
                            {visibleSearchResults.map((member) => (
                              <div
                                key={member.id}
                                className="flex items-center gap-3 rounded-lg border border-[#2d2d30] bg-[#252526] px-3 py-3"
                              >
                                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-[#3c3c3c] bg-[#1b1b1c] text-xs font-semibold text-[#4fc1ff]">
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
                          <div className="rounded-lg border border-dashed border-[#3c3c3c] bg-[#1b1b1c] px-4 py-8 text-center text-sm text-[#858585]">
                            검색 결과 없음
                          </div>
                        )}
                      </div>
                    </div>

                    <div className="space-y-3 xl:flex xl:min-h-0 xl:flex-col">
                      {selectedMembers.length > 0 ? (
                        <div className="space-y-2 xl:min-h-0 xl:flex-1 xl:overflow-y-auto xl:pr-1">
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
                                  <p className="truncate text-xs text-[#858585]">
                                    {member.email}
                                  </p>
                                </div>

                                <div className="grid min-w-[13rem] grid-cols-[minmax(0,1fr)_auto] items-center gap-2">
                                  <label className="min-w-0">
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
                                      {projectRoleOptions.map((roleOption) => (
                                        <option
                                          key={roleOption.id}
                                          value={roleOption.id}
                                        >
                                          {roleOption.label}
                                        </option>
                                      ))}
                                    </select>
                                  </label>

                                  <button
                                    type="button"
                                    onClick={() =>
                                      handleRemoveMember(member.id)
                                    }
                                    className="inline-flex shrink-0 items-center rounded-md border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2 text-xs font-semibold text-[#d4d4d4] transition hover:border-[#4b4b4f] hover:bg-[#2a2a2d]"
                                  >
                                    제거
                                  </button>
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="rounded-xl border border-dashed border-[#3c3c3c] bg-[#1b1b1c] px-4 py-8 text-center text-sm text-[#858585]">
                          초대된 멤버 없음
                        </div>
                      )}
                    </div>
                  </div>
                </section>
              ) : null}
            </div>

            <aside className="space-y-4 xl:flex xl:h-full xl:min-h-0 xl:flex-col">
              <section className={panelClassName}>
                <h3 className="text-sm font-semibold text-[#f3f3f3]">요약</h3>

                <div className="mt-3 space-y-2">
                  <div className={summaryRowClassName}>
                    <span className="text-[#858585]">화면</span>
                    <span className="font-medium text-[#f3f3f3]">
                      {previewModeLabel}
                    </span>
                  </div>
                  <div className={summaryRowClassName}>
                    <span className="text-[#858585]">제목</span>
                    <span className="max-w-36 truncate font-medium text-[#f3f3f3]">
                      {projectTitleDisplay}
                    </span>
                  </div>
                  <div className={summaryRowClassName}>
                    <span className="text-[#858585]">유형</span>
                    <span className="font-medium text-[#f3f3f3]">
                      {effectiveProjectType === 'personal' ? '개인' : '팀'}
                    </span>
                  </div>
                  <div className={summaryRowClassName}>
                    <span className="text-[#858585]">런타임</span>
                    <span className="font-medium text-[#f3f3f3]">
                      {selectedRuntimeOption?.label}
                    </span>
                  </div>
                  <div className={summaryRowClassName}>
                    <span className="text-[#858585]">인원</span>
                    <span className="font-medium text-[#f3f3f3]">
                      {teamMemberCount}명
                    </span>
                  </div>
                </div>
              </section>

              <section className="rounded-xl border border-[#313131] bg-[#252526] p-3 xl:flex xl:flex-1 xl:flex-col">
                <div className="flex items-center justify-between gap-3">
                  <h3 className="text-sm font-semibold text-[#f3f3f3]">
                    런타임
                  </h3>
                  <span className="text-xs text-[#858585]">
                    {selectedRuntimeOption?.label}
                  </span>
                </div>

                <div className="mt-3 grid grid-cols-2 gap-2 xl:mt-4">
                  {runtimeOptions.map((runtimeOption) => {
                    const isActive = runtimeOption.id === selectedRuntime;

                    return (
                      <button
                        key={runtimeOption.id}
                        type="button"
                        onClick={() => setSelectedRuntime(runtimeOption.id)}
                        className={`rounded-lg border px-2.5 py-2.5 text-left transition ${
                          isActive
                            ? 'border-[#0e639c] bg-[#0e639c]/10 shadow-[inset_0_0_0_1px_rgba(14,99,156,0.24)]'
                            : 'border-[#313131] bg-[#1e1e1e] hover:border-[#3c3c3c] hover:bg-[#232326]'
                        }`}
                      >
                        <div className="flex items-start justify-between gap-2">
                          <div className="min-w-0">
                            <span
                              className={`inline-flex rounded-md border border-[#3c3c3c] bg-[#1b1b1c] px-2 py-1 text-[10px] font-semibold tracking-[0.16em] uppercase ${runtimeAccentClassNameMap[runtimeOption.id]}`}
                            >
                              {runtimeOption.badge}
                            </span>
                            <p className="mt-1.5 text-sm font-semibold text-[#f3f3f3]">
                              {runtimeOption.label}
                            </p>
                            <p className="mt-0.5 truncate text-[10px] text-[#858585]">
                              {runtimeMetaMap[runtimeOption.id]}
                            </p>
                          </div>
                          <span
                            className={`mt-0.5 inline-flex h-4 w-4 shrink-0 items-center justify-center rounded-full border ${
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
                              className="h-2.5 w-2.5"
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
            </aside>
          </div>
        </div>

        <div className="flex flex-col gap-2 border-t border-[#313131] bg-[#181818] px-4 py-3 sm:flex-row sm:items-center sm:justify-between sm:px-6">
          <p className="text-xs text-[#858585]">
            {isCreateDisabled
              ? '프로젝트 제목을 입력하세요.'
              : `${selectedRuntimeOption?.label} 환경으로 생성 준비됨`}
          </p>

          <div className="flex flex-col gap-2 sm:flex-row">
            <button
              type="button"
              onClick={handleClose}
              className="inline-flex items-center justify-center rounded-md border border-[#3c3c3c] bg-[#2d2d30] px-3.5 py-2 text-sm font-semibold text-[#d4d4d4] transition hover:bg-[#343438]"
            >
              취소
            </button>
            <button
              type="button"
              onClick={handleCreateProject}
              disabled={isCreateDisabled}
              className={`inline-flex items-center justify-center rounded-md px-3.5 py-2 text-sm font-semibold transition ${
                isCreateDisabled
                  ? 'cursor-not-allowed bg-[#3b3b3b] text-[#858585]'
                  : 'bg-[#0e639c] text-white hover:bg-[#1177bb]'
              }`}
            >
              생성
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
