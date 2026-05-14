type SampleProject = {
  title: string;
  summary: string;
  stack: string;
  tone: string;
};

type SampleProjectsSectionProps = {
  projects: SampleProject[];
};

export const SampleProjectsSection = ({
  projects,
}: SampleProjectsSectionProps) => {
  return (
    <section className="rounded-2xl border border-[#313131] bg-[#252526] p-5">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
            샘플 프로젝트
          </p>
          <h2 className="mt-2 text-lg font-semibold text-[#f3f3f3]">
            예제 프로젝트로 바로 시작
          </h2>
        </div>
        <span className="rounded-md border border-[#3c3c3c] bg-[#1e1e1e] px-2.5 py-1 text-[11px] font-semibold text-[#858585]">
          Starter Templates
        </span>
      </div>

      <div className="mt-4 grid gap-3 lg:grid-cols-3">
        {projects.map((project) => (
          <article
            key={project.title}
            className={`rounded-md border px-4 py-4 ${project.tone}`}
          >
            <div className="flex items-center justify-between gap-3">
              <h3 className="text-sm font-semibold text-[#f3f3f3]">
                {project.title}
              </h3>
              <span className="text-[10px] font-semibold tracking-[0.16em] uppercase">
                {project.stack}
              </span>
            </div>
            <p className="mt-2 text-sm text-[#9da1a6]">{project.summary}</p>
            <button
              type="button"
              className="mt-4 inline-flex items-center rounded-md border border-[#3c3c3c] bg-[#252526] px-3 py-2 text-sm font-semibold text-[#d4d4d4] transition hover:bg-[#2d2d30]"
            >
              샘플 열기
            </button>
          </article>
        ))}
      </div>
    </section>
  );
};
