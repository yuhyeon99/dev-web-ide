# dev-web-ide

웹 기반 통합 개발 환경(Web IDE) 플랫폼 모노레포입니다.

## Workspace

```text
.
├── FE/        # React + Vite frontend
├── LICENSE
├── README.md
├── package.json
└── pnpm-workspace.yaml
```

## Stack

| 기술 | 버전 |
| --- | --- |
| React | `19.2.x` |
| TypeScript | `6.0.x` |
| Vite | `7.0.x` |
| @vitejs/plugin-react | `5.x` |
| TanStack Query | `5.x` |
| Jotai | `2.x` |
| Tailwind CSS | `4.2.x` |
| Node.js | `24 LTS` |

`@vitejs/plugin-react 6.0.x`는 `Vite 7.0.x`와 peer 호환되지 않아, 빌드 가능한 조합인 `5.x`로 조정했습니다.

## Commands

```bash
pnpm install
pnpm dev:fe
pnpm build:fe
pnpm preview:fe
```
