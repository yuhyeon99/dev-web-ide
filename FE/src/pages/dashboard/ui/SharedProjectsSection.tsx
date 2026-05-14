type SharedProject = {
  name: string;
  owner: string;
  permission: string;
  description: string;
};

type SharedProjectsSectionProps = {
  projects: SharedProject[];
};

export const SharedProjectsSection = ({
  projects,
}: SharedProjectsSectionProps) => {
  return (
    <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.08)]">
      <div>
        <p className="text-xs font-semibold tracking-[0.22em] text-slate-500 uppercase">
          공유 프로젝트
        </p>
        <h2 className="mt-2 text-xl font-semibold text-slate-950">
          팀과 함께 사용하는 협업 공간
        </h2>
        <p className="mt-2 text-sm leading-6 text-slate-600">
          초대받은 프로젝트와 권한 범위를 확인하고 바로 작업을 이어갈 수
          있습니다.
        </p>
      </div>

      <div className="mt-5 flex flex-col gap-4">
        {projects.map((project) => (
          <article
            key={`${project.name}-${project.owner}`}
            className="rounded-3xl border border-slate-200 bg-slate-50 p-5"
          >
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h3 className="text-base font-semibold text-slate-950">
                  {project.name}
                </h3>
                <p className="mt-1 text-sm text-slate-500">{project.owner}</p>
              </div>
              <span className="rounded-full bg-white px-3 py-1 text-xs font-semibold text-slate-700">
                {project.permission}
              </span>
            </div>
            <p className="mt-3 text-sm leading-6 text-slate-600">
              {project.description}
            </p>
          </article>
        ))}
      </div>
    </section>
  );
};
