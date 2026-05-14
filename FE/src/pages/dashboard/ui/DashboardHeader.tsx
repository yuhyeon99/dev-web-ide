type DashboardHeaderProps = {
  isGuest: boolean;
};

export const DashboardHeader = ({ isGuest }: DashboardHeaderProps) => {
  return (
    <section className="flex flex-col gap-4 rounded-2xl border border-[#313131] bg-[#252526] px-4 py-4 sm:flex-row sm:items-center sm:justify-between">
      <div className="space-y-2">
        <div className="inline-flex items-center gap-2 rounded-md border border-[#3c3c3c] bg-[#1e1e1e] px-2.5 py-1 text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
          {isGuest ? 'Guest Mode' : 'Dashboard'}
        </div>
        <div>
          <h1 className="text-xl font-semibold text-[#f3f3f3] sm:text-2xl">
            {isGuest ? 'Try Web IDE as Guest' : '프로젝트 대시보드'}
          </h1>
          <p className="mt-1 text-sm leading-6 text-[#9da1a6]">
            {isGuest
              ? '로그인 없이 임시 프로젝트를 열고 바로 편집과 실행을 시작할 수 있습니다.'
              : '최근 작업, 내 프로젝트, 공유 프로젝트를 한 곳에서 정리해 빠르게 이어갈 수 있습니다.'}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3 sm:min-w-72">
        <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-3 py-3">
          <p className="text-[11px] font-medium tracking-[0.18em] text-[#858585] uppercase">
            Workspace
          </p>
          <p className="mt-1 text-sm font-semibold text-[#d4d4d4]">
            {isGuest ? 'Temporary' : 'Saved Projects'}
          </p>
        </div>
        <div className="rounded-xl border border-[#313131] bg-[#1e1e1e] px-3 py-3">
          <p className="text-[11px] font-medium tracking-[0.18em] text-[#858585] uppercase">
            Storage
          </p>
          <p className="mt-1 text-sm font-semibold text-[#d4d4d4]">
            {isGuest ? 'Session Based' : 'Cloud Synced'}
          </p>
        </div>
      </div>
    </section>
  );
};
