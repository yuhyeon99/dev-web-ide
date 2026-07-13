import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router';

import { CreateTemporaryProjectModal } from '@/features/project-creation';
import type { ProjectCreateFormValues } from '@/features/project-creation/ui/create-temporary-project-modal';
import {
  createProject,
  createProjectFile,
  getMyProjects,
  getProjectFileTree,
  openProject,
} from '@/shared/api/projects';
import { getStoredAuthSession, subscribeAuthSession } from '@/shared/api/auth';
import { getRuntimes } from '@/shared/api/runtimes';
import {
  resolveProjectApiSession,
  saveProjectApiSession,
} from '@/shared/api/session';
import type {
  ProjectSummaryResponse,
  RuntimeResponse,
} from '@/shared/api/types';

import { DashboardHeader } from './DashboardHeader';
import { GuestQuickStartSection } from './GuestQuickStartSection';
import { MyProjectsSection } from './MyProjectsSection';
import { RecentProjectsSection } from './RecentProjectsSection';
import { SharedProjectsSection } from './SharedProjectsSection';

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

const mapProjectCard = (project: ProjectSummaryResponse) => {
  return {
    id: project.id,
    name: project.name,
    description:
      project.description ?? `${project.runtimeDisplayName} 프로젝트`,
    updatedAt: formatProjectUpdatedAt(project.updatedAt),
    badge:
      project.projectType === 'GUEST'
        ? '세션 보관'
        : project.runtimeDisplayName,
  };
};

export const DashboardPage = () => {
  const [authSession, setAuthSession] = useState(() => getStoredAuthSession());
  const isGuest = !authSession;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [
    isCreateTemporaryProjectModalOpen,
    setCreateTemporaryProjectModalOpen,
  ] = useState(false);
  const [createProjectError, setCreateProjectError] = useState<string | null>(
    null,
  );

  useEffect(() => {
    return subscribeAuthSession(() => {
      setAuthSession(getStoredAuthSession());
    });
  }, []);

  const runtimesQuery = useQuery({
    queryKey: ['runtimes'],
    queryFn: getRuntimes,
  });
  const projectsQuery = useQuery({
    queryKey: ['projects', authSession?.userId ?? 'guest'],
    queryFn: async () => {
      const projectSession = await resolveProjectApiSession();

      return getMyProjects(projectSession.accessToken);
    },
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
        memberUserIds: [],
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
  const projectCards = projectsQuery.data?.map(mapProjectCard) ?? [];
  const memberProjects =
    projectsQuery.data?.map((project) => ({
      id: project.id,
      name: project.name,
      description:
        project.description ?? `${project.runtimeDisplayName} 프로젝트`,
      meta: formatProjectUpdatedAt(project.updatedAt),
      accent: project.runtimeDisplayName,
    })) ?? [];

  const handleCreateProject = async (values: ProjectCreateFormValues) => {
    setCreateProjectError(null);
    await createProjectMutation.mutateAsync(values);
  };

  const handleProjectOpen = async (projectId: number) => {
    const projectSession = await resolveProjectApiSession();

    await openProject(projectSession.accessToken, projectId);
    saveProjectApiSession(projectId, projectSession);
    navigate(`/workspace?projectId=${projectId}`);
  };

  return (
    <div className="h-[calc(100vh-2.75rem)] overflow-hidden bg-[#1e1e1e] text-[#d4d4d4]">
      <div className="mx-auto flex h-full w-full max-w-7xl flex-col gap-4 px-4 py-4 sm:px-6 lg:px-8 lg:py-5">
        <DashboardHeader isGuest={isGuest} />

        {isGuest ? (
          <div className="flex min-h-0 flex-1 flex-col gap-4">
            <GuestQuickStartSection
              onCreateTemporaryProject={() =>
                setCreateTemporaryProjectModalOpen(true)
              }
            />
            <div className="flex min-h-0 flex-1 flex-col gap-4">
              <RecentProjectsSection
                title="최근 임시 프로젝트"
                subtitle="현재 브라우저 세션 기준"
                projects={projectCards}
                emptyMessage="이 브라우저 세션에는 아직 임시 프로젝트가 없습니다."
                defaultExpanded
                onProjectOpen={handleProjectOpen}
              />
              <RecentProjectsSection
                title="최근 팀 프로젝트"
                subtitle="최근 접근한 팀 작업 공간"
                projects={[]}
                emptyMessage="최근 팀 프로젝트가 없습니다."
              />
            </div>
          </div>
        ) : (
          <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
            <div className="flex flex-col gap-6">
              <RecentProjectsSection
                title="최근 프로젝트"
                subtitle="가장 최근에 열었던 작업 공간을 빠르게 이어서 진행할 수 있습니다."
                projects={projectCards}
                emptyMessage="최근 프로젝트가 없습니다."
                defaultExpanded
                onProjectOpen={handleProjectOpen}
              />
              <MyProjectsSection
                onCreateProject={() => setCreateTemporaryProjectModalOpen(true)}
                onProjectOpen={handleProjectOpen}
                projects={memberProjects}
              />
            </div>
            <SharedProjectsSection projects={[]} />
          </div>
        )}
      </div>

      <CreateTemporaryProjectModal
        key={isGuest ? 'guest-project-modal' : 'member-project-modal'}
        createError={createProjectError}
        initialPreviewMode={isGuest ? 'guest' : 'member'}
        isOpen={isCreateTemporaryProjectModalOpen}
        isCreating={createProjectMutation.isPending}
        isRuntimeLoading={runtimesQuery.isLoading}
        onClose={() => setCreateTemporaryProjectModalOpen(false)}
        onCreateProject={handleCreateProject}
        runtimeOptions={runtimeOptions}
      />
    </div>
  );
};
