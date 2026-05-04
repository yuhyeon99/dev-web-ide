import { atom, useAtom } from 'jotai'

const activePaneAtom = atom('editor')

const stack = [
  ['React', '19.2.x'],
  ['TypeScript', '6.0.x'],
  ['Vite', '7.0.x'],
  ['@vitejs/plugin-react', '5.x'],
  ['TanStack Query', '5.x'],
  ['Jotai', '2.x'],
  ['Tailwind CSS', '4.2.x'],
  ['Node.js', '24 LTS'],
] as const

function App() {
  const [activePane, setActivePane] = useAtom(activePaneAtom)

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(34,197,94,0.18),_transparent_28%),linear-gradient(180deg,_#050816_0%,_#09111f_48%,_#050816_100%)] px-6 py-10 text-slate-50">
      <div className="mx-auto flex min-h-[calc(100vh-5rem)] max-w-6xl flex-col gap-6">
        <section className="overflow-hidden rounded-[2rem] border border-white/10 bg-white/6 shadow-[0_20px_80px_rgba(5,8,22,0.45)] backdrop-blur">
          <div className="border-b border-white/10 px-6 py-4">
            <p className="text-xs uppercase tracking-[0.32em] text-emerald-300/80">
              dev-web-ide bootstrap
            </p>
            <div className="mt-3 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
              <div className="max-w-2xl">
                <h1 className="text-4xl font-semibold tracking-tight md:text-5xl">
                  React 19 + Vite 7 기반 Web IDE 프런트엔드 스타터
                </h1>
                <p className="mt-3 text-sm leading-6 text-slate-300 md:text-base">
                  FE 패키지에서 바로 시작할 수 있도록 TypeScript, Tailwind CSS,
                  TanStack Query, Jotai 기본 구성을 연결해 두었습니다.
                </p>
              </div>
              <div className="rounded-2xl border border-emerald-400/20 bg-emerald-400/10 px-4 py-3 text-sm text-emerald-100">
                <p className="font-medium">환경 기준</p>
                <p className="mt-1 text-emerald-50/80">Node.js 24 LTS + pnpm</p>
              </div>
            </div>
          </div>

          <div className="grid gap-6 px-6 py-6 lg:grid-cols-[1.2fr_0.8fr]">
            <article className="rounded-[1.5rem] border border-white/10 bg-slate-950/35 p-5">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-medium">Workspace Preview</h2>
                <div className="flex gap-2">
                  {['explorer', 'editor', 'terminal'].map((pane) => (
                    <button
                      key={pane}
                      type="button"
                      onClick={() => setActivePane(pane)}
                      className={`rounded-full px-3 py-1.5 text-xs font-medium capitalize transition ${
                        activePane === pane
                          ? 'bg-emerald-300 text-slate-950'
                          : 'bg-white/5 text-slate-300 hover:bg-white/10'
                      }`}
                    >
                      {pane}
                    </button>
                  ))}
                </div>
              </div>

              <div className="mt-5 grid gap-4 md:grid-cols-[220px_1fr]">
                <div className="rounded-2xl border border-white/10 bg-slate-900/70 p-4">
                  <p className="text-xs uppercase tracking-[0.24em] text-slate-400">
                    Project
                  </p>
                  <ul className="mt-4 space-y-3 text-sm text-slate-200">
                    <li className="rounded-xl bg-white/5 px-3 py-2">FE/src/</li>
                    <li className="rounded-xl bg-white/5 px-3 py-2">FE/components/</li>
                    <li className="rounded-xl bg-white/5 px-3 py-2">FE/features/</li>
                    <li className="rounded-xl bg-white/5 px-3 py-2">FE/shared/</li>
                  </ul>
                </div>

                <div className="rounded-2xl border border-white/10 bg-[#0b1325] p-4">
                  <p className="text-xs uppercase tracking-[0.24em] text-slate-400">
                    Active Pane
                  </p>
                  <div className="mt-4 rounded-2xl border border-emerald-400/15 bg-slate-950/70 p-4 text-sm leading-7 text-slate-200">
                    <p className="text-emerald-300">$ pnpm dev:fe</p>
                    <p className="mt-2">현재 선택된 패널: {activePane}</p>
                    <p>TanStack Query Provider: connected</p>
                    <p>Jotai Store: connected</p>
                    <p>Tailwind CSS v4 Vite plugin: enabled</p>
                  </div>
                </div>
              </div>
            </article>

            <aside className="rounded-[1.5rem] border border-white/10 bg-slate-950/35 p-5">
              <h2 className="text-lg font-medium">Tech Stack</h2>
              <div className="mt-4 space-y-3">
                {stack.map(([name, version]) => (
                  <div
                    key={name}
                    className="flex items-center justify-between rounded-2xl border border-white/8 bg-white/4 px-4 py-3 text-sm"
                  >
                    <span className="text-slate-200">{name}</span>
                    <code className="rounded-full bg-white/8 px-3 py-1 text-emerald-200">
                      {version}
                    </code>
                  </div>
                ))}
              </div>
            </aside>
          </div>
        </section>
      </div>
    </main>
  )
}

export default App
