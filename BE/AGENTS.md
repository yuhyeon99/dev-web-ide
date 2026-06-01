# BE Agent Guide

## Commit Rules

- Use Conventional Commits with BE scope: `type(be): summary`.
- Common types: `feat`, `fix`, `refactor`, `style`, `test`, `docs`, `chore`.
- Write commit summaries in Korean, imperative, and under 72 characters.
- Commit only related BE changes together. Do not include FE or unrelated workspace changes.
- Before committing BE changes, run `./gradlew test` from `BE/dev-web-ide`.
- If the tests cannot be run or fail for an unrelated reason, mention it in the final response.
