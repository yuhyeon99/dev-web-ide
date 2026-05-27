const quickStartItems = [
  '임시 프로젝트만 생성 가능',
  '터미널 실행 가능',
  '일정 시간 후 자동 삭제 가능',
];

type GuestQuickStartSectionProps = {
  onCreateTemporaryProject: () => void;
};

export const GuestQuickStartSection = ({
  onCreateTemporaryProject,
}: GuestQuickStartSectionProps) => {
  return (
    <section className="rounded-2xl border border-[#313131] bg-[#252526] p-5">
      <div className="max-w-2xl">
        <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
          빠른 시작
        </p>
        <h2 className="mt-2 text-2xl font-semibold text-[#f3f3f3]">
          새 임시 프로젝트를 열고 바로 시작하세요.
        </h2>
        <p className="mt-2 text-sm leading-6 text-[#9da1a6]">
          로그인 없이 코드를 작성하고 실행할 수 있습니다. 저장과 공유 기능은
          제한됩니다.
        </p>
      </div>

      <div className="mt-5 flex flex-col gap-3 sm:flex-row">
        <button
          type="button"
          onClick={onCreateTemporaryProject}
          className="inline-flex items-center justify-center rounded-md bg-[#0e639c] px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-[#1177bb]"
        >
          새 임시 프로젝트
        </button>
        <button
          type="button"
          className="inline-flex items-center justify-center rounded-md border border-[#3c3c3c] bg-[#2d2d30] px-4 py-2.5 text-sm font-semibold text-[#d4d4d4] transition hover:bg-[#343438]"
        >
          로그인하고 저장하기
        </button>
      </div>

      <div className="mt-5 grid gap-2 sm:grid-cols-3">
        {quickStartItems.map((item) => (
          <div
            key={item}
            className="rounded-md border border-[#313131] bg-[#1e1e1e] px-3 py-3"
          >
            <p className="text-sm text-[#cccccc]">{item}</p>
          </div>
        ))}
      </div>
    </section>
  );
};
