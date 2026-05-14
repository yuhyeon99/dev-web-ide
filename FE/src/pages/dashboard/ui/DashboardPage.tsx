import { DashboardHeader } from './DashboardHeader';
import { GuestQuickStartSection } from './GuestQuickStartSection';
import { MyProjectsSection } from './MyProjectsSection';
import { RecentProjectsSection } from './RecentProjectsSection';
import { SharedProjectsSection } from './SharedProjectsSection';

const memberRecentProjects = [
  {
    name: 'frontend-redesign',
    description: '어제 마지막으로 열었던 React 리뉴얼 작업 공간',
    updatedAt: '2시간 전',
    badge: '최근 편집',
  },
  {
    name: 'api-gateway',
    description: '배포 전 점검이 필요한 Node.js API 프로젝트',
    updatedAt: '어제',
    badge: '백엔드',
  },
  {
    name: 'algorithm-notes',
    description: '문제 풀이와 실험 코드를 모아둔 개인 저장소',
    updatedAt: '3일 전',
    badge: '학습',
  },
];

const myProjects = [
  {
    name: 'team-dashboard',
    description: '팀 현황판과 배포 모니터링 페이지',
    meta: '멤버 6명',
    accent: 'Active',
  },
  {
    name: 'design-system',
    description: '공용 토큰, 버튼, 입력 컴포넌트 관리',
    meta: '최근 배포 1일 전',
    accent: 'Library',
  },
  {
    name: 'cli-toolkit',
    description: '사내 작업 자동화를 위한 Node CLI 모음',
    meta: '브랜치 12개',
    accent: 'Internal',
  },
];

const sharedProjects = [
  {
    name: 'release-war-room',
    owner: 'Platform Team',
    permission: '읽기/실행',
    description: '릴리즈 점검용 공용 프로젝트',
  },
  {
    name: 'onboarding-examples',
    owner: 'DX Team',
    permission: '편집 가능',
    description: '신규 입사자 교육용 예제 묶음',
  },
  {
    name: 'pairing-lab',
    owner: 'Frontend Chapter',
    permission: '댓글 가능',
    description: '페어 프로그래밍과 실험용 스크래치 공간',
  },
];

const guestRecentProjects = [
  {
    name: 'temp-playground-01',
    description: '현재 세션에서 열었던 React 임시 작업 공간',
    updatedAt: '방금 전',
    badge: '세션 보관',
  },
  {
    name: 'quick-script-lab',
    description: '파이썬 스니펫을 실험하던 임시 프로젝트',
    updatedAt: '12분 전',
    badge: '자동 삭제 예정',
  },
];

const guestTeamProjects = [
  {
    name: 'frontend-sprint-room',
    description: '팀 프론트엔드 이슈를 함께 확인하는 작업 공간',
    updatedAt: '5분 전',
    badge: '팀 공유',
  },
  {
    name: 'api-response-check',
    description: '백엔드 응답 구조를 점검하는 협업 프로젝트',
    updatedAt: '26분 전',
    badge: '읽기 전용',
  },
  {
    name: 'design-handoff-lab',
    description: '디자인 시안과 구현 메모를 정리한 팀 공간',
    updatedAt: '1시간 전',
    badge: '참여 중',
  },
  {
    name: 'release-hotfix-room',
    description: '릴리즈 전 긴급 수정사항을 모아두는 워크스페이스',
    updatedAt: '어제',
    badge: '최근 업데이트',
  },
];

export const DashboardPage = () => {
  const isGuest = true;

  return (
    <div className="h-[calc(100vh-2.75rem)] overflow-hidden bg-[#1e1e1e] text-[#d4d4d4]">
      <div className="mx-auto flex h-full w-full max-w-7xl flex-col gap-4 px-4 py-4 sm:px-6 lg:px-8 lg:py-5">
        <DashboardHeader isGuest={isGuest} />

        {isGuest ? (
          <div className="flex min-h-0 flex-1 flex-col gap-4">
            <GuestQuickStartSection />
            <div className="flex min-h-0 flex-1 flex-col gap-4">
              <RecentProjectsSection
                title="최근 임시 프로젝트"
                subtitle="현재 브라우저 세션 기준"
                projects={guestRecentProjects}
                emptyMessage="이 브라우저 세션에는 아직 임시 프로젝트가 없습니다."
                defaultExpanded
              />
              <RecentProjectsSection
                title="최근 팀 프로젝트"
                subtitle="최근 접근한 팀 작업 공간"
                projects={guestTeamProjects}
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
                projects={memberRecentProjects}
                emptyMessage="최근 프로젝트가 없습니다."
              />
              <MyProjectsSection projects={myProjects} />
            </div>
            <SharedProjectsSection projects={sharedProjects} />
          </div>
        )}
      </div>
    </div>
  );
};
