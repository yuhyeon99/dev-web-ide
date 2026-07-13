/* eslint-disable no-unused-vars */

import { useState } from 'react';

type RecentProject = {
  id?: number;
  name: string;
  description: string;
  updatedAt: string;
  badge: string;
};

type RecentProjectsSectionProps = {
  title: string;
  subtitle: string;
  projects: RecentProject[];
  emptyMessage: string;
  defaultExpanded?: boolean;
  onProjectOpen?: (projectId: number) => void;
};

export const RecentProjectsSection = ({
  title,
  subtitle,
  projects,
  emptyMessage,
  defaultExpanded = false,
  onProjectOpen,
}: RecentProjectsSectionProps) => {
  const [isExpanded, setIsExpanded] = useState(defaultExpanded);

  return (
    <section
      className={`rounded-2xl border border-[#313131] bg-[#252526] p-5 ${
        isExpanded ? 'flex min-h-0 flex-1 flex-col' : 'flex-none'
      }`}
    >
      <button
        type="button"
        onClick={() => setIsExpanded((prev) => !prev)}
        className="flex w-full items-center justify-between gap-4 text-left"
      >
        <div>
          <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
            최근 작업
          </p>
          <h2 className="mt-2 text-lg font-semibold text-[#f3f3f3]">{title}</h2>
          <p className="mt-1 text-sm text-[#858585]">{subtitle}</p>
        </div>
        <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-md border border-[#3c3c3c] bg-[#1e1e1e] text-[#cccccc]">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            className={`h-4 w-4 transition-transform ${isExpanded ? 'rotate-180' : ''}`}
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="m6 9 6 6 6-6"
            />
          </svg>
        </span>
      </button>

      {isExpanded ? (
        <div className="mt-4 min-h-0 flex-1 overflow-y-auto pr-1">
          <div className="flex flex-col gap-2">
            {projects.length > 0 ? (
              projects.map((project) => (
                <button
                  key={`${project.name}-${project.updatedAt}`}
                  type="button"
                  onClick={() => {
                    if (project.id) {
                      onProjectOpen?.(project.id);
                    }
                  }}
                  className="flex flex-col gap-3 rounded-md border border-[#313131] bg-[#1e1e1e] px-4 py-3 text-left transition hover:border-[#3c3c3c] hover:bg-[#232326] sm:flex-row sm:items-center sm:justify-between"
                >
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-3">
                      <h3 className="text-sm font-semibold text-[#d4d4d4]">
                        {project.name}
                      </h3>
                      <span className="rounded-md border border-[#3c3c3c] bg-[#252526] px-2 py-0.5 text-[10px] font-semibold tracking-[0.16em] text-[#ce9178] uppercase">
                        {project.badge}
                      </span>
                    </div>
                    <p className="mt-1 text-sm text-[#858585]">
                      {project.description}
                    </p>
                  </div>
                  <div className="shrink-0 text-xs font-medium text-[#6a9955]">
                    {project.updatedAt}
                  </div>
                </button>
              ))
            ) : (
              <div className="rounded-md border border-dashed border-[#3c3c3c] bg-[#1e1e1e] p-4 text-sm text-[#858585]">
                {emptyMessage}
              </div>
            )}
          </div>
        </div>
      ) : null}
    </section>
  );
};
