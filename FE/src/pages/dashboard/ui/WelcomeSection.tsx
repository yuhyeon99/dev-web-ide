type WelcomeSectionProps = {
  isGuest: boolean;
};

const guestHighlights = [
  '브라우저 세션 기반 임시 작업',
  '로그인 없이 코드 편집 및 실행 가능',
  '샘플 프로젝트로 즉시 시작',
];

const memberHighlights = [
  '최근 프로젝트 이어서 작업',
  '내 프로젝트 저장 및 관리',
  '팀 프로젝트 공유와 협업',
];

export const WelcomeSection = ({ isGuest }: WelcomeSectionProps) => {
  const highlights = isGuest ? guestHighlights : memberHighlights;

  return (
    <section className="rounded-[32px] border border-white/70 bg-[linear-gradient(135deg,_rgba(255,255,255,0.92)_0%,_rgba(240,249,255,0.88)_45%,_rgba(236,253,245,0.92)_100%)] p-6 shadow-[0_24px_70px_rgba(15,23,42,0.08)] backdrop-blur sm:p-7">
      <div className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
        <div>
          <p className="text-xs font-semibold tracking-[0.24em] text-slate-500 uppercase">
            {isGuest ? 'Guest Dashboard' : 'Member Dashboard'}
          </p>
          <h2 className="mt-3 text-3xl leading-tight font-semibold text-slate-950 sm:text-4xl">
            {isGuest
              ? '내 프로젝트 대신 임시 프로젝트와 샘플 프로젝트를 먼저 보여주는 흐름'
              : '프로젝트 탐색, 저장, 협업을 한 번에 정리하는 기본 대시보드'}
          </h2>
          <p className="mt-4 max-w-2xl text-sm leading-7 text-slate-600 sm:text-base">
            {isGuest
              ? '게스트 모드에서는 내 프로젝트와 공유 프로젝트 개념보다, 지금 바로 만들 수 있는 임시 작업 공간과 다시 열 수 있는 세션 프로젝트가 더 중요합니다.'
              : '회원 모드에서는 최근 프로젝트, 내 프로젝트, 공유 프로젝트를 분리해 보여주는 편이 탐색성과 협업 맥락 모두에 유리합니다.'}
          </p>
        </div>

        <div className="grid gap-3">
          {highlights.map((highlight, index) => (
            <div
              key={highlight}
              className="flex items-center gap-4 rounded-3xl border border-white/80 bg-white/80 px-5 py-4"
            >
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-slate-950 text-sm font-semibold text-white">
                0{index + 1}
              </span>
              <p className="text-sm font-medium text-slate-700 sm:text-base">
                {highlight}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};
