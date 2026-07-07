# AGENTS.md

## Project
Spring Boot 기반 Web IDE 백엔드 프로젝트.

## 반드시 참고할 문서
- docs/architecture.md
- docs/database-schema.md

## 작업 원칙
- database-schema에 없는 테이블, 컬럼, 관계를 임의로 추가하지 않는다.
- 새 구조가 필요하면 먼저 database-schema 변경 필요 여부를 보고한다.
- 기존 패키지 구조와 테스트 스타일을 따른다.
- 한 번에 하나의 기능만 구현한다.
- 구현 전 계획을 먼저 제시한다.
- 구현 후 관련 테스트를 실행한다.

## 현재 구현 완료 범위
- User / OAuthAccount / TermsAgreement / GuestSession / AuthSession
- Runtime
- Project / ProjectMember / ProjectSettings / ProjectFile / ProjectAccessLog
- ProjectSaveBatch / FileVersion
- WorkspaceSession
- ContainerInstance
- GET /api/runtimes
- POST /api/projects
- GET /api/projects/my
- GET /api/projects/{projectId}
- POST /api/projects/{projectId}/open
- GET /api/projects/{projectId}/files/tree
- POST /api/projects/{projectId}/save
- LocalProjectFileStorageService
- ProjectRunService / ProjectRunServiceTest

## 아직 미구현
- ProjectRunController
- ProjectRunControllerTest
- 프로젝트 실행 중지 API
- TerminalLog 엔티티 / Repository / API
- 파일 생성 / 내용 조회 / 삭제 / 이름 변경 API
- OAuth 실제 연동
- 인증 기반 userId 주입