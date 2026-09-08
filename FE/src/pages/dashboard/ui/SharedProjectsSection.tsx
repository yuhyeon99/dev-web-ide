/* eslint-disable no-unused-vars */

type SharedProject = {
  id: number;
  name: string;
  owner: string;
  permission: string;
  description: string;
};

type SharedProjectsSectionProps = {
  onProjectOpen?: {
    (projectId: number): void;
  };
  projects: SharedProject[];
};

export const SharedProjectsSection = ({
  onProjectOpen,
  projects,
}: SharedProjectsSectionProps) => {
  return (
    <section className="min-w-0 rounded-[28px] border border-[#313131] bg-[#252526] p-5 shadow-[0_18px_40px_rgba(0,0,0,0.2)] sm:p-6">
      <div className="min-w-0">
        <p className="text-xs font-semibold tracking-[0.22em] text-[#4fc1ff] uppercase">
          공유 프로젝트
        </p>
        <h2 className="mt-2 text-lg leading-snug font-semibold break-keep text-[#f3f3f3] sm:text-xl">
          팀과 함께 사용하는 협업 공간
        </h2>
        <p className="mt-2 text-sm leading-6 text-[#858585]">
          초대받은 프로젝트와 권한 범위를 확인하고 바로 작업을 이어갈 수
          있습니다.
        </p>
      </div>

      <div className="mt-5 flex flex-col gap-4">
        {projects.length > 0 ? (
          projects.map((project) => (
            <article
              key={project.id}
              className="rounded-3xl border border-[#313131] bg-[#1e1e1e] p-5"
            >
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <button
                    type="button"
                    onClick={() => onProjectOpen?.(project.id)}
                    className="text-left text-base font-semibold text-[#f3f3f3] transition hover:text-[#4fc1ff]"
                  >
                    {project.name}
                  </button>
                  <p className="mt-1 text-sm text-[#858585]">{project.owner}</p>
                </div>
                <span className="rounded-full border border-[#3c3c3c] bg-[#252526] px-3 py-1 text-xs font-semibold text-[#d4d4d4]">
                  {project.permission}
                </span>
              </div>
              <p className="mt-3 text-sm leading-6 text-[#858585]">
                {project.description}
              </p>
            </article>
          ))
        ) : (
          <div className="rounded-md border border-dashed border-[#3c3c3c] bg-[#1e1e1e] p-4 text-sm text-[#858585]">
            공유받은 프로젝트가 없습니다.
          </div>
        )}
      </div>
    </section>
  );
};
