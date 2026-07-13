# 구현 완료 체크리스트

기준 경로: `BE/dev-web-ide`  
작성일: 2026-07-07  
확인 범위: `src/main/java`, `src/test/java`, `src/main/resources`, `docs`, Gradle 설정  
중점 확인 파일: `src/test/java/com/yuhyeon/devwebide/execution/service/ProjectRunServiceTest.java`

## 상태 기준

- `[x]` 구현 완료: 엔티티/저장소/서비스/API 또는 테스트 중 해당 기능을 실제로 수행하는 코드가 있음
- `[~]` 부분 구현: 핵심 코드 일부는 있으나 API 노출, 외부 연동, 인증, 예외 처리, 운영 구현 중 일부가 빠져 있음
- `[ ]` 미구현: 현재 폴더 기준으로 기능 구현 코드를 확인하지 못함

## 전체 요약

- [x] Spring Boot 4 기반 백엔드 프로젝트 구성
- [x] Java 25 Toolchain, Gradle 빌드 구성
- [x] JPA, Validation, Web MVC, MySQL 런타임, H2 테스트 의존성 구성
- [x] 메인 Java 파일 85개 확인
- [x] 테스트 Java 파일 28개 확인
- [x] `@Test` 기준 테스트 케이스 203개 확인
- [x] 주요 ERD 문서 확인: `docs/database-schema.md`
- [x] 아키텍처 문서 확인: `docs/architecture.md`
- [~] 일부 문서/주석의 한글 인코딩이 깨져 보임

## 도메인 및 저장소

### 사용자/인증 도메인

- [x] `User` 엔티티 구현
- [x] `OAuthAccount` 엔티티 구현
- [x] `TermsAgreement` 엔티티 구현
- [x] `GuestSession` 엔티티 구현
- [x] `AuthSession` 엔티티 구현
- [x] `UserRole`, `UserStatus`, `OAuthProvider` enum 구현
- [x] `UserRepository` 구현: 이메일 조회/존재 여부
- [x] `OAuthAccountRepository` 구현: provider + providerUserId 조회
- [x] `TermsAgreementRepository` 구현: 사용자별 약관 동의 조회/최신 동의 조회/동의 여부 확인
- [x] `GuestSessionRepository` 구현: 게스트 토큰 조회/존재 여부/만료 전 조회
- [x] `AuthSessionRepository` 구현: refresh token hash 조회/활성 세션 조회
- [x] 사용자/인증 저장소 테스트 구현
- [x] 게스트 세션 생성 API 구현
- [x] 게스트 토큰 생성 구현
- [x] 게스트 세션 만료 시각 설정 구현
- [x] 로그아웃 API 구현
- [x] refresh token hash 기반 AuthSession 조회 구현
- [x] AuthSession revoke 처리 구현
- [x] refresh token cookie 제거 구현
- [x] 토큰 재발급 API 구현
- [x] TokenService 구현
- [x] JWT Access Token 발급 구현
- [x] Access Token 검증 구현
- [x] JWT signature / exp / claim 검증 구현
- [x] Access Token 기반 AuthSession active 재검증 구현
- [x] 현재 사용자 조회 서비스 구현
- [x] Mock OAuth Login API 구현
- [x] OAuthAccount 기반 User 조회/생성 구현
- [x] 신규 가입 시 TermsAgreement 생성 구현
- [x] AuthSession 생성 구현
- [x] refreshToken hash 저장 구현
- [x] refreshToken Cookie 발급 구현
- [x] 로그인 성공 시 Access Token 발급 구현
- [x] Spring Security 기반 JWT 인증 필터 구현
- [x] SecurityContext principal 주입 구현
- [x] AuthSession active 재검증 필터 적용 구현
- [x] `GET /api/users/me` SecurityContext 기반 전환 구현
- [x] 토큰 재발급 시 refresh token hash 기반 AuthSession 검증 구현
- [x] 토큰 재발급 시 user session ACTIVE 검증 구현
- [x] 토큰 재발급 시 guest session 만료 검증 구현
- [~] 실제 OAuth provider 검증 연동은 미구현
- [~] Project 계열 API의 Spring Security 기반 인가 전환은 일부만 완료

### 런타임 도메인

- [x] `Runtime` 엔티티 구현
- [x] `RuntimeLanguage`, `RuntimeStatus` enum 구현
- [x] `RuntimeRepository` 구현: 이름, 상태, 언어별 조회
- [x] `RuntimeService` 구현: ACTIVE 런타임 목록 조회
- [x] `RuntimeController` 구현: `GET /api/runtimes`
- [x] 런타임 저장소/컨트롤러 테스트 구현
- [~] 런타임 seed 데이터 또는 운영 등록 API는 확인되지 않음

### 프로젝트 도메인

- [x] `Project` 엔티티 구현
- [x] `ProjectMember` 엔티티 구현
- [x] `ProjectSettings` 엔티티 구현
- [x] `ProjectAccessLog` 엔티티 구현
- [x] `ProjectStatus`, `ProjectType`, `ProjectVisibility` enum 구현
- [x] `ProjectMemberRole`, `ProjectMemberStatus` enum 구현
- [x] `ProjectAccessType` enum 구현
- [x] `ProjectRepository` 구현: 소유자/게스트/상태/검색 조건 조회
- [x] `ProjectMemberRepository` 구현: 프로젝트/사용자/역할/상태 기준 조회
- [x] `ProjectSettingsRepository` 구현: 프로젝트 설정 조회/존재 여부
- [x] `ProjectAccessLogRepository` 구현: 사용자/게스트/프로젝트/접근 유형별 조회
- [x] 프로젝트 관련 저장소 테스트 구현

### 파일/저장 도메인

- [x] `ProjectFile` 엔티티 구현
- [x] `ProjectSaveBatch` 엔티티 구현
- [x] `FileVersion` 엔티티 구현
- [x] `ProjectFileType`, `ProjectFileStatus`, `ProjectSaveBatchStatus` enum 구현
- [x] `ProjectFileRepository` 구현: 경로, 부모 파일, 파일 타입, 상태 기준 조회
- [x] `ProjectSaveBatchRepository` 구현: 프로젝트/사용자/게스트/상태별 저장 이력 조회
- [x] `FileVersionRepository` 구현: 파일별 버전, 저장 배치별 버전 조회/카운트
- [x] 파일/저장 관련 저장소 테스트 구현

### 워크스페이스/실행 도메인

- [x] `WorkspaceSession` 엔티티 구현
- [x] `WorkspaceSessionStatus` enum 구현
- [x] `ContainerInstance` 엔티티 구현
- [x] `ContainerInstanceStatus` enum 구현
- [x] `WorkspaceSessionRepository` 구현: 프로젝트/사용자/게스트/상태 기준 조회
- [x] `ContainerInstanceRepository` 구현: 세션/상태/taskArn/containerId 기준 조회
- [x] 워크스페이스/컨테이너 저장소 테스트 구현

### 터미널 도메인

- [x] `TerminalLog` 엔티티 구현
- [x] `TerminalStreamType` enum 구현
- [x] `TerminalLogRepository` 구현: 워크스페이스 세션별 순번 조회, afterSequenceNo 조회, 최신 순번 조회
- [x] 터미널 로그 저장소 테스트 구현

## 서비스 구현

### 프로젝트 서비스

- [x] 개인 프로젝트 생성 구현
- [x] 팀 프로젝트 생성 구현
- [x] 게스트 프로젝트 생성 구현
- [x] 프로젝트 생성 시 ACTIVE 런타임 검증
- [x] 프로젝트 생성 시 기본 설정 생성
- [x] 프로젝트 생성 시 루트 디렉터리 메타데이터 생성
- [x] 팀 프로젝트 생성 시 OWNER 멤버 생성
- [x] 팀 프로젝트 생성 시 초대 멤버 생성 및 중복 제거
- [x] 내 프로젝트 목록 조회 구현
- [x] ACTIVE 프로젝트만 목록 조회하도록 구현
- [x] 프로젝트 상세 조회 구현
- [x] 프로젝트 기본 API 1차 Security principal 기반 전환: 생성/내 프로젝트 목록/상세 조회
- [x] `ProjectAuthorizationService` 1차 구현
- [x] 프로젝트 상세 조회 owner 권한 검증 구현
- [x] 프로젝트 상세 조회 ACTIVE member 권한 검증 구현
- [x] 프로젝트 상세에 런타임/설정/활성 멤버 포함
- [x] 프로젝트 열기 구현
- [x] 프로젝트 열기 시 접근 로그 저장
- [x] 프로젝트 열기 API Security principal 기반 전환
- [x] 프로젝트 열기 owner/ACTIVE member 권한 검증 구현
- [x] 프로젝트 열기 접근 로그 principal userId 기록 구현
- [x] 프로젝트 수정 API 구현
- [x] 프로젝트 이름 수정 구현
- [x] 프로젝트 설명 수정 구현
- [x] 프로젝트 수정 시 ACTIVE 프로젝트 검증
- [x] 프로젝트 수정 API Security principal 기반 전환
- [x] 프로젝트 수정 OWNER 권한 검증 구현
- [x] 프로젝트 삭제 API 구현
- [x] 프로젝트 삭제 API Security principal 기반 전환
- [x] 프로젝트 삭제 OWNER 권한 검증 구현
- [x] 프로젝트 soft delete 구현
- [x] 실행 중 WorkspaceSession 삭제 차단 구현
- [x] 실제 저장소 삭제 없이 프로젝트 상태만 변경하는 정책 반영
- [x] 프로젝트 멤버 초대 API 구현
- [x] 프로젝트 멤버 초대 API Security principal 기반 전환
- [x] 프로젝트 멤버 초대 수락 API 구현
- [x] 프로젝트 멤버 초대 수락 API Security principal 기반 전환
- [x] 프로젝트 멤버 권한 변경 API 구현
- [x] 프로젝트 멤버 권한 변경 API Security principal 기반 전환
- [x] 프로젝트 멤버 제거 API 구현
- [x] 프로젝트 멤버 제거 API Security principal 기반 전환
- [x] OWNER 기반 임시 권한 검증 구현
- [x] ProjectMember API `X-User-Id` 기반 요청자 식별 제거
- [x] 중복 멤버 초대 방지 구현
- [x] OWNER 권한 변경/제거 금지 구현
- [x] 초대 수락 시 INVITED -> ACTIVE 전환 구현
- [x] 프로젝트 멤버 초대 OWNER 권한 검증 구현
- [x] 프로젝트 멤버 초대 수락 대상자 본인 검증 구현
- [x] 프로젝트 멤버 권한 변경 OWNER 권한 검증 구현
- [x] 프로젝트 멤버 제거 OWNER 권한 검증 구현
- [x] ProjectMember API GUEST principal 차단 구현
- [x] 초대 대상 사용자 본인 검증 구현
- [x] 초대 수락 시 joinedAt 기록 구현
- [x] `ProjectServiceTest` 구현
- [~] 프로젝트 복구 API는 확인되지 않음

### 프로젝트 파일 서비스

- [x] 파일 트리 조회 구현
- [x] ACTIVE 프로젝트 검증 후 파일 트리 조회
- [x] 부모/자식 관계 기반 트리 응답 생성
- [x] 파일 일괄 저장 구현
- [x] 저장 요청자 검증: userId 또는 guestSessionId 중 하나만 허용
- [x] 저장 대상 파일의 프로젝트 소속 검증
- [x] ACTIVE 파일만 저장 허용
- [x] 디렉터리 저장 차단
- [x] 저장 배치 생성
- [x] 파일 버전 번호 증가 처리
- [x] 파일 메타데이터 크기 갱신
- [x] `ProjectFileServiceTest` 구현
- [x] 파일 생성 API 구현
- [x] 파일 생성 시 ACTIVE 프로젝트 검증
- [x] 파일 생성 시 부모 디렉터리 검증
- [x] 파일명 검증
- [x] 중복 이름 검증
- [x] StorageService 파일/디렉터리 생성 구현
- [x] 파일 내용 조회 API 구현
- [x] 파일 내용 조회 시 ACTIVE 프로젝트 검증
- [x] 파일 내용 조회 시 파일 소속/상태/타입 검증
- [x] StorageService를 통한 파일 내용 읽기 구현
- [x] 파일 이름 변경 API 구현
- [x] 파일 이름 변경 시 ACTIVE 프로젝트 검증
- [x] 파일 이름 변경 시 파일 소속/상태 검증
- [x] 파일명 검증
- [x] 중복 이름 검증
- [x] 디렉터리 하위 path 갱신
- [x] StorageService rename 구현
- [x] 파일/디렉터리 삭제 API 구현
- [x] 파일/디렉터리 삭제 시 ACTIVE 프로젝트 검증
- [x] 파일/디렉터리 삭제 시 파일 소속/상태 검증
- [x] root directory 삭제 차단
- [x] 디렉터리 하위 파일/폴더 soft delete
- [x] StorageService delete 구현

### 로컬 파일 저장소

- [x] `ProjectFileStorageService` 인터페이스 구현
- [x] `LocalProjectFileStorageService` 구현
- [x] 현재 파일 저장 구현
- [x] 현재 파일 생성 구현
- [x] 디렉터리 생성 구현
- [x] 파일 rename 구현
- [x] 디렉터리 rename 구현
- [x] 파일 삭제 구현
- [x] 디렉터리 삭제 구현
- [x] 현재 파일 내용 읽기 구현
- [x] `.versions/{projectFileId}/v{versionNo}` 버전 파일 저장 구현
- [x] UTF-8 바이트 기준 파일 크기 계산
- [x] SHA-256 content hash 계산
- [x] 경로 탈출 방지 검증 구현
- [x] 필수 파라미터 및 버전 번호 검증 구현
- [x] `LocalProjectFileStorageServiceTest` 구현
- [x] 운영 ECS에서 `/app/storage`를 EFS Access Point로 mount
- [x] `app.storage.project-root` 설정 키 추가로 파일 저장 경로 설정 불일치 해소

### 프로젝트 실행 서비스

- [x] `ProjectRunService` 구현
- [x] 실행 요청자 검증: userId 또는 guestSessionId 중 하나만 허용
- [x] ACTIVE 프로젝트 조회
- [x] 회원 실행 요청 처리
- [x] 게스트 실행 요청 처리
- [x] 프로젝트 런타임 추출
- [x] `WorkspaceSession` STARTING 상태 저장
- [x] `ContainerExecutionService.start(...)` 호출
- [x] `ContainerInstance` 저장
- [x] 실행 응답 DTO 생성
- [x] `ContainerExecutionService` 인터페이스 구현
- [x] `LocalContainerExecutionService` 구현
- [x] 로컬 컨테이너 시작 결과 생성
- [x] `ProjectRunServiceTest.java` 구현 확인
- [~] 실제 Docker/ECS/Fargate 컨테이너 실행은 로컬 임시 구현으로 대체됨
- [~] 워크스페이스 세션 상태가 STARTING으로 저장되며 RUNNING 전환 로직은 확인되지 않음

### 워크스페이스 실행 중지 서비스

- [x] `WorkspaceSessionStopService` 구현
- [x] 실행 중인 워크스페이스 세션만 중지 허용
- [x] RUNNING 컨테이너 조회
- [x] `ContainerExecutionService.stop(...)` 호출
- [x] 컨테이너 상태 STOPPED 처리
- [x] 워크스페이스 세션 상태 STOPPED 처리
- [x] `WorkspaceSessionStopServiceTest` 구현
- [x] 실행 중지 컨트롤러/API 구현
- [x] `POST /api/workspace-sessions/{workspaceSessionId}/stop` 구현
- [x] `WorkspaceSessionStopControllerTest` 구현
- [~] 로컬 컨테이너 중지는 no-op 구현

### 터미널 로그 서비스

- [x] `TerminalLogService` 구현
- [x] 워크스페이스 세션 존재 검증
- [x] 전체 로그 조회 구현
- [x] `afterSequenceNo` 이후 로그 조회 구현
- [x] 최신 sequenceNo 반환 구현
- [x] 음수 afterSequenceNo 검증 구현
- [x] `TerminalLogServiceTest` 구현
- [~] 컨테이너 출력 로그 수집/저장 파이프라인은 확인되지 않음

## API 구현

- [x] `POST /api/guest-sessions`
- [x] `POST /api/auth/oauth/login`
- [x] `POST /api/auth/logout`
- [x] `POST /api/auth/refresh`
- [x] `GET /api/users/me`
- [x] `GET /api/runtimes`
- [x] `POST /api/projects`
- [x] `GET /api/projects/my`
- [x] `GET /api/projects/{projectId}`
- [x] `PATCH /api/projects/{projectId}`
- [x] `DELETE /api/projects/{projectId}`
- [x] `POST /api/projects/{projectId}/open`
- [x] `POST /api/projects/{projectId}/members`
- [x] `POST /api/projects/{projectId}/members/{memberId}/accept`
- [x] `PATCH /api/projects/{projectId}/members/{memberId}`
- [x] `DELETE /api/projects/{projectId}/members/{memberId}`
- [x] `GET /api/projects/{projectId}/files/tree`
- [x] `POST /api/projects/{projectId}/files`
- [x] `GET /api/projects/{projectId}/files/{fileId}/content`
- [x] `PATCH /api/projects/{projectId}/files/{fileId}/rename`
- [x] `DELETE /api/projects/{projectId}/files/{fileId}`
- [x] `POST /api/projects/{projectId}/save`
- [x] `POST /api/projects/{projectId}/run`
- [x] `GET /api/workspace-sessions/{workspaceSessionId}/terminal/logs`
- [x] `POST /api/workspace-sessions/{workspaceSessionId}/stop`
- [x] 주요 API 컨트롤러 테스트 구현
- [~] ProjectFile/Run/Workspace/Terminal API는 인증 연동 전 임시로 header/request param/body의 userId, guestSessionId 사용
- [~] 전역 예외 응답 포맷/ControllerAdvice는 확인되지 않음

## DTO 및 검증

- [x] 런타임 응답 DTO 구현
- [x] 프로젝트 생성 요청/응답 DTO 구현
- [x] 프로젝트 목록/상세/열기 응답 DTO 구현
- [x] 프로젝트 설정/멤버 응답 DTO 구현
- [x] 파일 트리 응답 DTO 구현
- [x] 파일 저장 요청/응답 DTO 구현
- [x] 실행 요청/응답 DTO 구현
- [x] 컨테이너 인스턴스 응답 DTO 구현
- [x] 워크스페이스 중지 응답 DTO 구현
- [x] 터미널 로그 목록/단건 응답 DTO 구현
- [x] 프로젝트 생성 요청 Bean Validation 적용
- [~] 파일 저장 요청과 실행 요청에는 컨트롤러 레벨 `@Valid`가 확인되지 않음

## 테스트 구현 확인

### Repository 테스트

- [x] User, OAuthAccount, TermsAgreement, GuestSession, AuthSession 테스트
- [x] Runtime 테스트
- [x] Project, ProjectMember, ProjectSettings, ProjectAccessLog 테스트
- [x] ProjectFile, ProjectSaveBatch, FileVersion 테스트
- [x] WorkspaceSession 테스트
- [x] ContainerInstance 테스트
- [x] TerminalLog 테스트

### Service 테스트

- [x] ProjectService 테스트: Project 기본 API와 수정/삭제/open principal 기반 전환 포함
- [x] ProjectMemberService 테스트: Security principal 기반 전환 포함
- [x] ProjectFileService 테스트
- [x] LocalProjectFileStorageService 테스트
- [x] ProjectRunService 테스트
- [x] WorkspaceSessionStopService 테스트
- [x] TerminalLogService 테스트
- [x] GuestSessionService 테스트
- [x] TokenService 테스트: Access Token 발급/검증, refresh token 생성 포함
- [x] OAuthLoginService 테스트
- [x] AccessTokenAuthenticationService 테스트
- [x] AuthService 테스트: 로그아웃, 토큰 재발급 포함
- [x] UserService 테스트: Security principal 기반 현재 사용자 조회 포함

### Controller 테스트

- [x] RuntimeController 테스트
- [x] ProjectController 테스트: Project 기본 API와 수정/삭제/open Security principal 기반 전환
- [x] ProjectMemberController 테스트: Security principal 기반 전환 포함
- [x] ProjectFileController 테스트: 파일 트리 조회, 파일 생성, 파일 내용 조회, 파일 저장, 파일 이름 변경, 파일/디렉터리 삭제
- [x] ProjectRunController 테스트
- [x] TerminalLogController 테스트
- [x] WorkspaceSessionStopController 테스트
- [x] GuestSessionController 테스트
- [x] AuthController 테스트: Mock OAuth 로그인, 로그아웃, 토큰 재발급 포함
- [x] UserController 테스트: Security 인증 기반 현재 사용자 조회 포함
- [x] JwtAuthenticationFilter 테스트
- [x] SecurityConfig 테스트

### `ProjectRunServiceTest.java` 세부 확인

- [x] 회원 프로젝트 실행 성공 케이스
- [x] 게스트 프로젝트 실행 성공 케이스
- [x] WorkspaceSession 저장 검증
- [x] ContainerInstance 저장 검증
- [x] ContainerExecutionService 호출 검증
- [x] 프로젝트 미존재 예외 검증
- [x] 요청자 정보 누락 예외 검증
- [x] 회원 ID와 게스트 세션 ID 동시 전달 예외 검증
- [x] 사용자 미존재 예외 검증
- [x] 게스트 세션 미존재 예외 검증
- [x] 컨테이너 실행 실패 시 트랜잭션/상태 처리 케이스는 확인되지 않음

## 현재 미완료 또는 추가 구현 필요 항목

- [~] 실제 OAuth provider 검증 연동
  - Google OAuth authorization code flow는 구현 및 운영 배포 완료
  - GitHub/Kakao 등 다른 provider는 아직 미연동
- [ ] ProjectFile API Security principal 기반 전환
- [ ] ProjectRun / WorkspaceSessionStop / TerminalLog API Security principal 기반 전환
- [ ] 프로젝트 복구 API
- [ ] 컨테이너 상태 STARTING -> RUNNING 전환 처리
- [ ] 실제 Docker/ECS/Fargate 실행 연동
- [ ] 컨테이너 출력 수집 후 TerminalLog 저장 처리
- [ ] idle timer 기반 컨테이너 자동 종료
- [ ] WebSocket/공동 편집/CRDT 연동
- [ ] Redis 연동
- [ ] 전역 예외 응답 표준화
- [ ] 운영 DB 마이그레이션 도구 적용 여부 확인

## 인프라 배포 진행 상태

기준일: 2026-07-13

### 완료

- [x] RDS MySQL 생성 및 백엔드 연결 확인
  - DB 이름: `dev_web_ide`
  - 엔드포인트: `database-1.cif2g02ggeeo.us-east-1.rds.amazonaws.com:3306`
  - Spring Boot 컨테이너에서 RDS 연결 및 JPA 초기화 확인
- [x] 백엔드 Docker 이미지 빌드 설정 추가
  - `Dockerfile`, `.dockerignore` 추가
  - Java 25 런타임 기반 Spring Boot jar 실행 이미지 구성
- [x] ECR repository 생성 및 이미지 push
  - Repository: `dev-web-ide-be`
  - Image URI: `725478842252.dkr.ecr.us-east-1.amazonaws.com/dev-web-ide-be:latest`
- [x] ECS Fargate 기반 백엔드 서비스 배포
  - Cluster: `dev-web-ide-prod-cluster`
  - Service: `dev-web-ide-be-prod-service`
  - Task Definition: `dev-web-ide-be-prod-task:5`
  - Desired tasks: `1`
  - Running tasks: `1`
- [x] ALB 기반 백엔드 HTTP 진입점 구성
  - ALB: `dev-web-ide-be-prod-alb`
  - Target Group: `dev-web-ide-be-prod-tg`
  - Health check path: `/api/health`
- [x] API용 CloudFront 배포
  - Distribution: `E1QTFPLGD97O3L`
  - HTTPS API base URL: `https://d15mkrht7zfcoy.cloudfront.net`
  - Origin: `dev-web-ide-be-prod-alb-293005819.us-east-1.elb.amazonaws.com`
  - Cache policy: `Managed-CachingDisabled`
- [x] 헬스 체크 API 구현 및 배포 확인
  - Endpoint: `GET /api/health`
  - `https://d15mkrht7zfcoy.cloudfront.net/api/health` 응답 `200 OK` 확인
- [x] BE CORS 설정 추가 및 운영 배포 반영
  - 설정 키: `app.cors.allowed-origins`
  - 운영 환경변수: `APP_CORS_ALLOWED_ORIGINS`
  - 허용 Origin: `https://d1qcnjd8lnakb.cloudfront.net`
  - 허용 Origin: `http://dev-web-ide-fe-prod-apne2.s3-website-ap-southeast-2.amazonaws.com`
  - CloudFront API endpoint 기준 preflight 응답 확인
- [x] EFS 연결 및 파일 저장 영속성 확인
  - File system: `fs-06bac21d4761a0ad7`
  - Access Point: `fsap-032b7a3e38c1af59e`
  - EFS security group: `sg-02c0c5275b6812b82`
  - ECS mount path: `/app/storage`
  - ECS task definition: `dev-web-ide-be-prod-task:5`
  - 임시 프로젝트 파일 저장 후 ECS task 재배포 뒤 동일 파일 내용 재조회 성공
- [x] Google OAuth 로그인 운영 배포
  - Google authorize/callback API 구현
  - Google client secret은 AWS Secrets Manager에서 ECS secret으로 주입
  - FE 로그인 버튼, OAuth callback 처리, JWT 저장 흐름 연결
- [x] CI/CD 자동 배포 설정
  - GitHub Actions workflow: `.github/workflows/deploy-fe.yml`
  - GitHub Actions workflow: `.github/workflows/deploy-be.yml`
  - AWS OIDC Role: `GitHubActionsDevWebIdeDeployRole`
  - FE: build 후 S3 sync 및 CloudFront invalidation
  - BE: test 후 Docker image build/push, ECS task definition 등록, service update

### 부분 완료

- [x] FE API base URL 설정 및 주요 API 연동
  - FE production env: `VITE_API_BASE_URL=https://d15mkrht7zfcoy.cloudfront.net`
  - 게스트 세션, 프로젝트 생성/조회/열기, 파일 조회/저장, Google OAuth 로그인 흐름 연동
- [~] HTTPS API endpoint 적용
  - 도메인 없이 API용 CloudFront를 ALB 앞에 두어 HTTPS endpoint 확보
  - FE/BE 모두 CloudFront HTTPS endpoint를 사용하므로 현재 단계에서 ALB 자체 HTTPS listener는 필수 아님
- [~] DB/JWT secret 운영 주입
  - 현재 ECS Task Definition 환경변수로 주입됨
  - Google OAuth client secret은 AWS Secrets Manager로 전환 완료
  - DB password, JWT secret은 Secrets Manager 또는 SSM Parameter Store 이전 필요
- [~] RDS 보안 그룹
  - ECS service security group에서 RDS 접근 가능
  - RDS security group에 넓은 inbound 규칙이 남아 있어 운영 전 축소 필요

### 미완료

- [ ] Secret 관리 전환
  - `SPRING_DATASOURCE_PASSWORD`
  - `APP_JWT_SECRET`
  - AWS Secrets Manager 또는 SSM Parameter Store 사용 필요
- [ ] Redis 연결
  - 세션 상태, 활성 사용자 수, WebSocket Pub/Sub, idle timer 보조 용도
  - ElastiCache Redis 또는 호환 Redis 구성 필요
### 선택 또는 운영 고도화

- [ ] API 커스텀 도메인 연결
  - 예: `api.<domain>`
  - 현재는 `https://d15mkrht7zfcoy.cloudfront.net`로 HTTPS API 호출 가능하므로 필수 아님
  - 서비스 주소를 고정/브랜딩하거나 CloudFront distribution 교체 영향을 줄이고 싶을 때 진행
- [ ] ALB HTTPS listener 구성
  - 현재 외부 클라이언트는 CloudFront HTTPS로 접근하고, CloudFront -> ALB 구간만 HTTP
  - 내부 origin 구간까지 암호화하거나 ALB 직접 접근을 HTTPS로 제한하고 싶을 때 진행
  - 도메인 확보 후 ACM 인증서 발급, ALB `443` listener 연결, `80 -> 443` redirect 구성 필요

## 확인 필요 사항

- [ ] `application.properties`는 `app.project.storage-root=./storage`를 사용하지만 `LocalProjectFileStorageService`는 `app.storage.project-root`를 읽고 있음
- [ ] `README.md`, `AGENTS.md`, 일부 Java 주석과 테스트 DisplayName의 한글 인코딩 정리 필요
- [ ] `src/test/java/com/yuhyeon/devwebide/user/repository/GuestSessionRepository.java` 파일명은 테스트 클래스인데 `*Test.java` 패턴이 아님
- [ ] 현재 API는 인증 연동 전 임시 userId/guestSessionId 전달 방식을 사용하므로 FE 연동 시 계약 재확인 필요
