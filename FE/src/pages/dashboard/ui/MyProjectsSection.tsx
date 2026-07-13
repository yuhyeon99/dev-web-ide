/* eslint-disable no-unused-vars */

type MyProject = {
  id?: number;
  name: string;
  description: string;
  meta: string;
  accent: string;
};

type MyProjectsSectionProps = {
  onCreateProject: () => void;
  onProjectOpen?: (id: number) => void;
  projects: MyProject[];
};

export const MyProjectsSection = ({
  onCreateProject,
  onProjectOpen,
  projects,
}: MyProjectsSectionProps) => {
  return (
    <section className="rounded-[28px] border border-[#313131] bg-[#252526] p-6 shadow-[0_18px_40px_rgba(0,0,0,0.2)]">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-semibold tracking-[0.22em] text-[#4fc1ff] uppercase">
            내 프로젝트
          </p>
          <h2 className="mt-2 text-xl font-semibold text-[#f3f3f3]">
            직접 생성하고 관리하는 작업 공간
          </h2>
        </div>
        <button
          type="button"
          onClick={onCreateProject}
          className="inline-flex items-center justify-center rounded-2xl border border-[#3c3c3c] bg-[#1e1e1e] px-4 py-2 text-sm font-semibold text-[#d4d4d4] transition hover:border-[#4a4a4a] hover:bg-[#232326]"
        >
          새 프로젝트 생성
        </button>
      </div>

      <div className="mt-5 grid gap-4 lg:grid-cols-3">
        {projects.length > 0 ? (
          projects.map((project) => (
            <article
              key={project.id ?? project.name}
              className="rounded-3xl border border-[#313131] bg-[#1e1e1e] p-5"
            >
              <div className="flex items-center justify-between gap-3">
                <button
                  type="button"
                  onClick={() => {
                    if (project.id) {
                      onProjectOpen?.(project.id);
                    }
                  }}
                  className="truncate text-left text-base font-semibold text-[#f3f3f3] hover:text-[#4fc1ff]"
                >
                  {project.name}
                </button>
                <span className="rounded-full border border-[#3c3c3c] bg-[#252526] px-2.5 py-1 text-[11px] font-semibold tracking-[0.18em] text-[#ce9178] uppercase">
                  {project.accent}
                </span>
              </div>
              <p className="mt-3 text-sm leading-6 text-[#858585]">
                {project.description}
              </p>
              <p className="mt-5 text-sm font-medium text-[#6a9955]">
                {project.meta}
              </p>
            </article>
          ))
        ) : (
          <div className="rounded-md border border-dashed border-[#3c3c3c] bg-[#1e1e1e] p-4 text-sm text-[#858585] lg:col-span-3">
            아직 생성한 프로젝트가 없습니다.
          </div>
        )}
      </div>
    </section>
  );
};
