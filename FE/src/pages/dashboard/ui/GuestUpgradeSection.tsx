const guestLimits = [
  { feature: '저장', guest: '제한적' },
  { feature: '공유', guest: '불가' },
  { feature: '공동 편집', guest: '제한됨' },
  { feature: '터미널', guest: '가능' },
];

export const GuestUpgradeSection = () => {
  return (
    <aside className="rounded-2xl border border-[#313131] bg-[#252526] p-5">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-[11px] font-semibold tracking-[0.18em] text-[#ce9178] uppercase">
            로그인 유도
          </p>
          <h2 className="mt-2 text-lg font-semibold text-[#f3f3f3]">
            저장과 공유는 로그인 후 사용할 수 있습니다.
          </h2>
          <p className="mt-2 text-sm leading-6 text-[#9da1a6]">
            게스트 모드는 체험 중심입니다. 계속 작업할 예정이면 계정 전환이 더
            자연스럽습니다.
          </p>
        </div>
        <div className="rounded-md border border-[#3c3c3c] bg-[#1e1e1e] px-2.5 py-1 text-[11px] font-semibold text-[#dcdcaa]">
          Upgrade
        </div>
      </div>

      <div className="mt-4 grid gap-2">
        {guestLimits.map((item) => (
          <div
            key={item.feature}
            className="flex items-center justify-between rounded-md border border-[#313131] bg-[#1e1e1e] px-3 py-2.5 text-sm"
          >
            <span className="text-[#cccccc]">{item.feature}</span>
            <span className="text-[#858585]">{item.guest}</span>
          </div>
        ))}
      </div>

      <div className="mt-4 grid gap-3 sm:grid-cols-2">
        <button
          type="button"
          className="rounded-md bg-[#0e639c] px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-[#1177bb]"
        >
          로그인하고 저장하기
        </button>
        <button
          type="button"
          className="rounded-md border border-[#3c3c3c] bg-[#2d2d30] px-4 py-2.5 text-sm font-semibold text-[#d4d4d4] transition hover:bg-[#343438]"
        >
          계정 만들기
        </button>
      </div>
    </aside>
  );
};
