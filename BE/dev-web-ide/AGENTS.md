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
- TerminalLog
- GET /api/runtimes
- POST /api/guest-sessions
- POST /api/auth/oauth/login
- GET /api/auth/oauth/google/authorize
- GET /api/auth/oauth/google/callback
- POST /api/auth/oauth/google/signup
- POST /api/auth/logout
- POST /api/auth/refresh
- GET /api/users/me
- GET /api/users/search
- PATCH /api/users/me/profile
- POST /api/projects
- GET /api/projects/my
- GET /api/projects/shared
- GET /api/projects/{projectId}
- PATCH /api/projects/{projectId}
- DELETE /api/projects/{projectId}
- POST /api/projects/{projectId}/open
- POST /api/projects/{projectId}/members
- PATCH /api/projects/{projectId}/members/{memberId}
- DELETE /api/projects/{projectId}/members/{memberId}
- POST /api/projects/{projectId}/members/{memberId}/accept
- GET /api/projects/{projectId}/files/tree
- GET /api/projects/{projectId}/files/{fileId}/content
- POST /api/projects/{projectId}/files
- PATCH /api/projects/{projectId}/files/{fileId}/rename
- DELETE /api/projects/{projectId}/files/{fileId}
- POST /api/projects/{projectId}/save
- POST /api/projects/{projectId}/run
- POST /api/workspace-sessions/{workspaceSessionId}/stop
- GET /api/workspace-sessions/{workspaceSessionId}/terminal/logs
- WebSocket/STOMP 실시간 협업 메시지 처리
- Redis Pub/Sub 기반 프로젝트 이벤트, CRDT 업데이트, 팀 채팅
- LocalProjectFileStorageService
- LocalContainerExecutionService
- ProjectRunService / ProjectRunController / 관련 테스트
- WorkspaceSessionStopService / WorkspaceSessionStopController / 관련 테스트
- TerminalLogService / TerminalLogController / 관련 테스트
- ProjectFileService 파일 생성 / 내용 조회 / 저장 / 이름 변경 / 삭제 API 및 관련 테스트
- Spring Security 기반 JWT 인증 필터
- SecurityContext principal 기반 사용자 식별

## 아직 미구현
- 실제 Google OAuth 운영 검증/연동 완성 여부는 환경 설정과 외부 provider 동작까지 확인 필요
- Project 계열 API 전체가 의도한 최종 인가 정책을 모두 만족하는지는 기능별 추가 검증 필요
