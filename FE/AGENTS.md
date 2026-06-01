# FE Agent Guide

Keep this file short. It exists so Codex can follow FE commit rules without loading long project docs.

## Commit Rules

- Use Conventional Commits with FE scope: `type(fe): summary`.
- Common types: `feat`, `fix`, `refactor`, `style`, `test`, `docs`, `chore`.
- Write commit summaries in Korean, imperative, and under 72 characters.
- Commit only related FE changes together. Do not include BE or unrelated workspace changes.
- Before committing FE changes, run `pnpm --filter @dev-web-ide/fe build`.
- If the build cannot be run or fails for an unrelated reason, mention it in the final response.

## Code Rules

- Follow the existing React, TypeScript, Vite, Tailwind CSS, and FSD architecture.
- Keep code organized by FSD boundaries such as `app`, `pages`, `widget`, `features`, and shared layers when present.
- Prefer small, focused edits over broad refactors.
- Keep UI text, component names, and file names consistent with nearby FE code.
- Do not add dependencies unless the task clearly requires them.
