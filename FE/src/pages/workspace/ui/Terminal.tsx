import { TerminalIcon } from './WorkspaceIcons';

type TerminalProps = {
  isRunning: boolean;
  lines: string[];
  onRun: () => void;
  runtimeLabel?: string;
  statusLabel?: string;
};

export const Terminal = ({
  isRunning,
  lines,
  onRun,
  runtimeLabel,
  statusLabel,
}: TerminalProps) => {
  return (
    <section className="flex h-[17.5rem] min-h-[17.5rem] w-full min-w-0 flex-col overflow-hidden rounded-[24px] border border-[var(--ws-border)] bg-[linear-gradient(180deg,rgba(37,37,38,0.96)_0%,rgba(20,20,20,0.98)_100%)] shadow-[0_18px_40px_rgba(0,0,0,0.26)]">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[var(--ws-border)] bg-[rgba(18,18,18,0.88)] px-3 py-2">
        <div className="inline-flex items-center gap-2 rounded-lg bg-[#1f1f1f] px-3 py-2 text-sm text-white">
          <TerminalIcon className="h-4 w-4" />
          <span>Terminal</span>
        </div>
        <button
          type="button"
          disabled={isRunning}
          onClick={onRun}
          className="rounded-md border border-[#3c3c3c] bg-[#1e1e1e] px-3 py-2 text-sm font-semibold text-[#d4d4d4] transition hover:border-[#4a4a4a] hover:bg-[#252526] disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isRunning ? 'Running' : 'Run'}
        </button>
      </div>

      <div className="flex flex-wrap items-center gap-3 border-b border-[var(--ws-border)] bg-[#1f1f1f] px-3 py-2 text-[11px] text-[var(--ws-muted)]">
        <span>{runtimeLabel ?? 'runtime: unknown'}</span>
        <span>{statusLabel ?? 'status: idle'}</span>
      </div>

      <div className="min-h-0 flex-1 overflow-auto px-4 py-4 font-mono text-[13px] leading-7 text-[#d4d4d4]">
        {lines.length > 0 ? (
          <div className="space-y-0.5">
            {lines.map((output, index) => (
              <p key={`${output}-${index + 1}`} className="whitespace-pre-wrap">
                {output || ' '}
              </p>
            ))}
          </div>
        ) : (
          <div className="rounded-md border border-dashed border-[#3c3c3c] bg-[#1e1e1e] px-3 py-2 text-sm text-[var(--ws-muted)]">
            프로젝트를 실행하면 결과가 여기에 표시됩니다.
          </div>
        )}
      </div>
    </section>
  );
};
