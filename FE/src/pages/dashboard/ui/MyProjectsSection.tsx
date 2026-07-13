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
    <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.08)]">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-semibold tracking-[0.22em] text-slate-500 uppercase">
            내 프로젝트
          </p>
          <h2 className="mt-2 text-xl font-semibold text-slate-950">
            직접 생성하고 관리하는 작업 공간
          </h2>
        </div>
        <button
          type="button"
          onClick={onCreateProject}
          className="inline-flex items-center justify-center rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-100"
        >
          새 프로젝트 생성
        </button>
      </div>

      <div className="mt-5 grid gap-4 lg:grid-cols-3">
        {projects.map((project) => (
          <article
            key={project.name}
            className="rounded-3xl border border-slate-200 bg-[linear-gradient(180deg,_#ffffff_0%,_#f8fafc_100%)] p-5"
          >
            <div className="flex items-center justify-between gap-3">
              <button
                type="button"
                onClick={() => {
                  if (project.id) {
                    onProjectOpen?.(project.id);
                  }
                }}
                className="truncate text-left text-base font-semibold text-slate-950 hover:text-[#0e639c]"
              >
                {project.name}
              </button>
              <span className="rounded-full bg-slate-950 px-2.5 py-1 text-[11px] font-semibold tracking-[0.18em] text-white uppercase">
                {project.accent}
              </span>
            </div>
            <p className="mt-3 text-sm leading-6 text-slate-600">
              {project.description}
            </p>
            <p className="mt-5 text-sm font-medium text-slate-500">
              {project.meta}
            </p>
          </article>
        ))}
      </div>
    </section>
  );
};
