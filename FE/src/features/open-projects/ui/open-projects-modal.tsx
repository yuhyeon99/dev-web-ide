import { useEffect, useState, type MouseEvent } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router';

import {
  projectFilterTabs,
  type OpenProject,
  type ProjectExecutionStatus,
  type ProjectFilterId,
  type ProjectVisibility,
} from '../model/open-projects';
import {
  getMyProjects,
  getSharedProjects,
  openProject,
} from '@/shared/api/projects';
import { getStoredAuthSession } from '@/shared/api/auth';
import {
  resolveProjectApiSession,
  saveProjectApiSession,
} from '@/shared/api/session';
import type { ProjectSummaryResponse } from '@/shared/api/types';

type OpenProjectsModalProps = {
  isOpen: boolean;
  onClose: () => void;
};

const handleDialogClick = (event: MouseEvent<HTMLDivElement>) => {
  event.stopPropagation();
};

const panelClassName = 'rounded-xl border border-[#313131] bg-[#252526] p-4';

const inputClassName =
  'w-full rounded-lg border border-[#3c3c3c] bg-[#1f1f1f] py-2.5 pr-3 pl-10 text-sm text-[#d4d4d4] transition placeholder:text-[#6b7280] focus:border-[#007acc] focus:outline-none focus:ring-2 focus:ring-[#007acc]/25';

const executionStatusToneMap: Record<ProjectExecutionStatus, string> = {
  running:
    'border-[#4ec9b0]/40 bg-[#4ec9b0]/10 text-[#9cf5e2] shadow-[inset_0_0_0_1px_rgba(78,201,176,0.12)]',
  stopped:
    'border-[#3c3c3c] bg-[#252526] text-[#9da1a6] shadow-[inset_0_0_0_1px_rgba(60,60,60,0.16)]',
  starting:
    'border-[#d7ba7d]/40 bg-[#d7ba7d]/10 text-[#f0dca8] shadow-[inset_0_0_0_1px_rgba(215,186,125,0.12)]',
};

const executionStatusLabelMap: Record<ProjectExecutionStatus, string> = {
  running: 'Running',
  stopped: 'Stopped',
  starting: 'Starting',
};

const visibilityLabelMap: Record<ProjectVisibility, string> = {
  personal: '개인 프로젝트',
  team: '팀 프로젝트',
};

const formatProjectUpdatedAt = (dateText: string) => {
  const updatedAt = new Date(dateText).getTime();

  if (Number.isNaN(updatedAt)) {
    return '최근 업데이트';
  }

  const diffMinutes = Math.max(
    0,
    Math.floor((Date.now() - updatedAt) / 1000 / 60),
  );

  if (diffMinutes < 1) {
    return '방금 전';
  }

  if (diffMinutes < 60) {
    return `${diffMinutes}분 전`;
  }

  const diffHours = Math.floor(diffMinutes / 60);

  if (diffHours < 24) {
    return `${diffHours}시간 전`;
  }

  return `${Math.floor(diffHours / 24)}일 전`;
};

const toOpenProject = (
  project: ProjectSummaryResponse,
  source: 'my' | 'shared',
): OpenProject => {
  const visibility = project.projectType === 'TEAM' ? 'team' : 'personal';

  return {
    id: String(project.id),
    name: project.name,
    updatedAt: formatProjectUpdatedAt(project.updatedAt),
    executionStatus: 'stopped',
    visibility,
    filters: ['recent', source === 'shared' ? 'invited' : 'created'],
  };
};

export const OpenProjectsModal = ({
  isOpen,
  onClose,
}: OpenProjectsModalProps) => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState<ProjectFilterId>('recent');
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(
    null,
  );
  const authSession = getStoredAuthSession();
  const projectsQuery = useQuery({
    queryKey: ['open-projects', authSession?.userId ?? 'guest'],
    queryFn: async () => {
      const projectSession = await resolveProjectApiSession();
      const myProjects = await getMyProjects(projectSession.accessToken);
      const sharedProjects = authSession
        ? await getSharedProjects(authSession.accessToken)
        : [];

      return [
        ...myProjects.map((project) => toOpenProject(project, 'my')),
        ...sharedProjects.map((project) => toOpenProject(project, 'shared')),
      ];
    },
    enabled: isOpen,
  });
  const projects = projectsQuery.data ?? [];
  const openProjectMutation = useMutation({
    mutationFn: async (projectId: number) => {
      const projectSession = await resolveProjectApiSession(
        undefined,
        projectId,
      );

      await openProject(projectSession.accessToken, projectId);
      saveProjectApiSession(projectId, projectSession);

      return projectId;
    },
    onSuccess: (projectId) => {
      handleClose();
      navigate(`/workspace?projectId=${projectId}`);
    },
  });

  const resetModalState = () => {
    setSearchQuery('');
    setActiveFilter('recent');
    setSelectedProjectId(null);
  };

  const handleClose = () => {
    resetModalState();
    onClose();
  };

  useEffect(
    function manageOpenProjectsModalEffect() {
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

      return function cleanupOpenProjectsModalEffect() {
        document.body.style.overflow = previousOverflow;
        window.removeEventListener('keydown', handleEscapeKeydown);
      };
    },
    [isOpen, onClose],
  );

  const normalizedQuery = searchQuery.trim().toLowerCase();
  const filteredProjects = projects.filter((project) => {
    if (!project.filters.includes(activeFilter)) {
      return false;
    }

    if (!normalizedQuery) {
      return true;
    }

    const visibilityLabel =
      visibilityLabelMap[project.visibility].toLowerCase();
    const statusLabel =
      executionStatusLabelMap[project.executionStatus].toLowerCase();

    return (
      project.name.toLowerCase().includes(normalizedQuery) ||
      visibilityLabel.includes(normalizedQuery) ||
      statusLabel.includes(normalizedQuery)
    );
  });

  if (!isOpen) {
    return null;
  }

  const effectiveSelectedProjectId = filteredProjects.some(
    (project) => project.id === selectedProjectId,
  )
    ? selectedProjectId
    : (filteredProjects[0]?.id ?? null);
  const selectedProject =
    filteredProjects.find(
      (project) => project.id === effectiveSelectedProjectId,
    ) ?? null;
  const resultCount = filteredProjects.length;
  const shouldScrollProjectCards = filteredProjects.length > 4;

  const handleOpenProject = () => {
    if (!selectedProject) {
      return;
    }

    const projectId = Number(selectedProject.id);

    if (!Number.isFinite(projectId) || openProjectMutation.isPending) {
      return;
    }

    openProjectMutation.mutate(projectId);
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
        aria-labelledby="open-projects-title"
        className="flex max-h-[calc(100dvh-2rem)] w-full max-w-5xl flex-col overflow-hidden rounded-2xl border border-[#313131] bg-[#1e1e1e] shadow-[0_28px_90px_rgba(0,0,0,0.58)] sm:max-h-[calc(100dvh-3rem)] xl:max-h-[760px]"
        onClick={handleDialogClick}
      >
        <div className="border-b border-[#313131] bg-[linear-gradient(180deg,#252526_0%,#1f1f1f_100%)] px-5 py-4 sm:px-6">
          <div className="flex items-start justify-between gap-4">
            <div className="min-w-0">
              <span className="inline-flex items-center rounded-md border border-[#3c3c3c] bg-[#1e1e1e] px-2 py-1 text-[10px] font-semibold tracking-[0.2em] text-[#4fc1ff] uppercase">
                Project Explorer
              </span>
              <div className="mt-2 flex flex-wrap items-center gap-3">
                <h2
                  id="open-projects-title"
                  className="text-lg font-semibold text-[#f3f3f3] sm:text-xl"
                >
                  프로젝트 열기
                </h2>
                <span className="rounded-full border border-[#313131] bg-[#1b1b1c] px-2.5 py-1 text-xs font-medium text-[#9da1a6]">
                  {resultCount} Projects
                </span>
              </div>
            </div>

            <button
              type="button"
              onClick={handleClose}
              className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-md border border-transparent text-[#9da1a6] transition hover:border-[#3c3c3c] hover:bg-[#2a2d2e] hover:text-white focus-visible:ring-2 focus-visible:ring-[#007acc]/70 focus-visible:outline-none"
              aria-label="프로젝트 열기 팝업 닫기"
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

        <div className="min-h-0 flex-1 overflow-hidden">
          <div className="flex h-full min-h-0 flex-col gap-4 px-5 py-5 sm:px-6 sm:py-6">
            <section className={panelClassName}>
              <div className="flex items-center justify-between gap-3">
                <h3 className="text-sm font-semibold text-[#f3f3f3]">
                  프로젝트 검색
                </h3>
                <span className="text-xs text-[#858585]">
                  프로젝트명 기준 검색
                </span>
              </div>
              <label className="mt-3 block">
                <span className="sr-only">프로젝트 검색</span>
                <div className="relative">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.8"
                    className="pointer-events-none absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2 text-[#7f848e]"
                  >
                    <circle cx="11" cy="11" r="6.5" />
                    <path strokeLinecap="round" d="m16 16 4 4" />
                  </svg>
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(event) => setSearchQuery(event.target.value)}
                    className={inputClassName}
                    placeholder="프로젝트명을 검색하세요"
                  />
                </div>
              </label>
            </section>

            <section className={panelClassName}>
              <div className="flex items-center justify-between gap-3">
                <h3 className="text-sm font-semibold text-[#f3f3f3]">
                  프로젝트 필터
                </h3>
                <span className="text-xs text-[#858585]">
                  원하는 범위만 빠르게 확인
                </span>
              </div>

              <div className="mt-3 flex flex-wrap gap-2">
                {projectFilterTabs.map((filterTab) => {
                  const isActive = filterTab.id === activeFilter;
                  const count = projects.filter((project) =>
                    project.filters.includes(filterTab.id),
                  ).length;

                  return (
                    <button
                      key={filterTab.id}
                      type="button"
                      onClick={() => setActiveFilter(filterTab.id)}
                      className={`inline-flex items-center gap-2 rounded-lg border px-3 py-2 text-sm font-medium transition ${
                        isActive
                          ? 'border-[#0e639c] bg-[#0e639c]/12 text-white shadow-[inset_0_0_0_1px_rgba(14,99,156,0.28)]'
                          : 'border-[#313131] bg-[#1e1e1e] text-[#9da1a6] hover:border-[#3c3c3c] hover:bg-[#232326] hover:text-[#d4d4d4]'
                      }`}
                    >
                      <span>{filterTab.label}</span>
                      <span
                        className={`rounded-full px-2 py-0.5 text-[11px] ${
                          isActive
                            ? 'bg-[#0b4f7c] text-[#dff3ff]'
                            : 'bg-[#252526] text-[#858585]'
                        }`}
                      >
                        {count}
                      </span>
                    </button>
                  );
                })}
              </div>
            </section>

            <section
              className={`${panelClassName} flex min-h-0 flex-1 flex-col overflow-hidden`}
            >
              <div className="flex items-center justify-between gap-3">
                <h3 className="text-sm font-semibold text-[#f3f3f3]">
                  프로젝트 카드
                </h3>
                <span className="text-xs text-[#858585]">
                  카드를 선택한 뒤 열기
                </span>
              </div>

              {projectsQuery.isLoading ? (
                <div className="mt-3 rounded-xl border border-dashed border-[#3c3c3c] bg-[#1b1b1c] px-4 py-10 text-center">
                  <p className="text-sm font-medium text-[#d4d4d4]">
                    프로젝트를 불러오는 중입니다.
                  </p>
                </div>
              ) : filteredProjects.length > 0 ? (
                <div className="mt-3 min-h-0 flex-1 overflow-hidden">
                  <div
                    className={`grid h-full w-full content-start gap-3 overflow-y-auto pr-1 [--project-card-min-height:7.5rem] lg:grid-cols-2 ${
                      shouldScrollProjectCards
                        ? 'max-h-[calc((var(--project-card-min-height)*4)+(0.75rem*3))] lg:max-h-[calc((var(--project-card-min-height)*2)+0.75rem)]'
                        : 'max-h-full'
                    }`}
                  >
                    {filteredProjects.map((project) => {
                      const isSelected =
                        project.id === effectiveSelectedProjectId;

                      return (
                        <button
                          key={project.id}
                          type="button"
                          onClick={() => setSelectedProjectId(project.id)}
                          className={`min-h-[var(--project-card-min-height)] rounded-xl border px-4 py-4 text-left transition ${
                            isSelected
                              ? 'border-[#0e639c] bg-[#0e639c]/12 shadow-[inset_0_0_0_1px_rgba(14,99,156,0.28)]'
                              : 'border-[#313131] bg-[#1e1e1e] hover:border-[#3c3c3c] hover:bg-[#232326]'
                          }`}
                          aria-pressed={isSelected}
                        >
                          <ProjectCard project={project} />
                        </button>
                      );
                    })}
                  </div>
                </div>
              ) : (
                <div className="mt-3 rounded-xl border border-dashed border-[#3c3c3c] bg-[#1b1b1c] px-4 py-10 text-center">
                  <p className="text-sm font-medium text-[#d4d4d4]">
                    검색 결과가 없습니다.
                  </p>
                  <p className="mt-1 text-xs text-[#858585]">
                    다른 검색어나 필터를 선택해보세요.
                  </p>
                </div>
              )}
            </section>
          </div>
        </div>

        <div className="flex flex-col gap-2 border-t border-[#313131] bg-[#181818] px-4 py-3 sm:flex-row sm:items-center sm:justify-between sm:px-6">
          <p className="text-xs text-[#858585]">
            {selectedProject
              ? `${selectedProject.name} · ${executionStatusLabelMap[selectedProject.executionStatus]}`
              : '열 프로젝트를 선택하세요.'}
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
              onClick={handleOpenProject}
              disabled={!selectedProject}
              className={`inline-flex items-center justify-center rounded-md px-3.5 py-2 text-sm font-semibold transition ${
                selectedProject
                  ? 'bg-[#0e639c] text-white hover:bg-[#1177bb]'
                  : 'cursor-not-allowed bg-[#3b3b3b] text-[#858585]'
              }`}
            >
              {openProjectMutation.isPending ? '여는 중' : '열기'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

type ProjectCardProps = {
  project: OpenProject;
};

const ProjectCard = ({ project }: ProjectCardProps) => {
  return (
    <article className="space-y-4">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate text-base font-semibold text-[#f3f3f3]">
            {project.name}
          </p>
          <p className="mt-1 text-xs text-[#858585]">
            마지막 수정 {project.updatedAt}
          </p>
        </div>

        <span
          className={`inline-flex shrink-0 items-center rounded-full border px-2.5 py-1 text-[11px] font-semibold ${executionStatusToneMap[project.executionStatus]}`}
        >
          {executionStatusLabelMap[project.executionStatus]}
        </span>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        <span className="inline-flex items-center rounded-md border border-[#3c3c3c] bg-[#1b1b1c] px-2.5 py-1 text-xs font-medium text-[#d4d4d4]">
          {visibilityLabelMap[project.visibility]}
        </span>
        {project.visibility === 'team' && project.memberCount ? (
          <span className="inline-flex items-center rounded-md border border-[#3c3c3c] bg-[#1b1b1c] px-2.5 py-1 text-xs font-medium text-[#9da1a6]">
            팀원 {project.memberCount}명
          </span>
        ) : null}
      </div>
    </article>
  );
};
