# Dev Web IDE 전체 시스템 아키텍처 및 기획 문서

## 1. 프로젝트 개요

Dev Web IDE는 브라우저에서 프로젝트를 생성하고, 파일을 편집하고, 실행하며, 팀원과 함께 협업할 수 있는 웹 기반 통합 개발 환경입니다. 사용자는 별도의 로컬 개발 환경을 설치하지 않고 웹 브라우저만으로 코드 작성, 파일 관리, 저장, 실행, 팀 프로젝트 협업을 수행할 수 있습니다.

## 2. 기획 목표

- 게스트 사용자가 빠르게 임시 프로젝트를 생성하고 체험할 수 있게 한다.
- 회원은 Google OAuth로 로그인하고 개인 프로젝트와 팀 프로젝트를 관리할 수 있게 한다.
- 프로젝트 파일 트리와 코드 에디터를 실제 API와 연결한다.
- 파일 저장은 서버와 영속 스토리지에 반영한다.
- 팀 프로젝트에서는 멤버 초대, 공유 프로젝트 목록, 실시간 공동 편집, 채팅을 제공한다.
- AWS 기반 배포 구조를 통해 프론트엔드와 백엔드를 실제 운영 환경에 배포한다.

## 3. 주요 사용자 흐름

### 게스트 흐름

1. 사용자가 대시보드에서 게스트 프로젝트를 생성한다.
2. 백엔드가 게스트 세션과 프로젝트를 생성한다.
3. 사용자는 워크스페이스에서 파일을 생성하고 편집한다.
4. Save 버튼을 누르면 `/api/projects/{projectId}/save`로 파일 내용이 저장된다.
5. Run 버튼을 누르면 프로젝트 실행 API를 호출하고 실행 결과를 터미널 패널에서 확인한다.

### 회원 흐름

1. 사용자가 Google OAuth로 로그인한다.
2. 신규 사용자는 닉네임 설정을 완료한다.
3. 대시보드에서 개인 프로젝트 또는 팀 프로젝트를 생성한다.
4. 팀 프로젝트 생성 시 실제 가입된 회원을 검색해 초대한다.
5. 초대받은 사용자는 공유 프로젝트 목록에서 팀 프로젝트를 확인하고 워크스페이스에 진입한다.

### 협업 흐름

1. 같은 팀 프로젝트의 같은 파일을 여러 사용자가 연다.
2. Monaco Editor 변경 사항이 Yjs CRDT 업데이트로 변환된다.
3. WebSocket/STOMP를 통해 백엔드로 전송된다.
4. 백엔드는 Redis Pub/Sub으로 업데이트를 fan-out한다.
5. 다른 클라이언트가 CRDT 업데이트를 받아 에디터에 반영한다.
6. 팀 워크스페이스 채팅도 같은 WebSocket/Redis 구조로 전달된다.

## 4. 전체 시스템 구성

```text
User Browser
  |
  | HTTPS
  v
CloudFront - FE Distribution
  |
  v
S3 Static Website Bucket

User Browser
  |
  | HTTPS / WSS
  v
CloudFront - API Distribution
  |
  | HTTP origin
  v
Application Load Balancer
  |
  v
ECS Fargate Service
  |
  +--> Spring Boot API
  +--> RDS MySQL
  +--> EFS
  +--> Redis / ElastiCache target
  +--> ECR Docker Image
```

## 5. AWS 구성 요소 역할

| 구성 요소 | 역할 |
| --- | --- |
| S3 | React/Vite 빌드 결과물을 저장하는 정적 호스팅 저장소 |
| CloudFront | FE/API HTTPS 진입점, 캐싱, CDN, WSS 프록시 |
| ACM | CloudFront HTTPS 인증서 관리 |
| ALB | 백엔드 ECS 서비스로 HTTP 트래픽 분산 |
| ECS Fargate | Spring Boot API 컨테이너 실행 |
| ECR | 백엔드 Docker 이미지 저장소 |
| RDS MySQL | 사용자, 프로젝트, 파일 메타데이터, 권한 정보 저장 |
| EFS | 프로젝트 파일 원본과 버전 파일 영속 저장 |
| Redis | WebSocket 다중 인스턴스 fan-out, 프로젝트 이벤트, CRDT 업데이트, 팀 채팅 메시지 전달 |
| GitHub Actions | FE/BE CI/CD 자동 배포 |

## 6. 프론트엔드 아키텍처

### 기술 스택

- React 19
- TypeScript
- Vite
- TanStack Query
- Tailwind CSS
- Monaco Editor
- Yjs / y-monaco
- STOMP WebSocket

### 주요 화면

- 대시보드
  - 최근 프로젝트
  - 내 프로젝트
  - 공유 프로젝트
  - 프로젝트 생성 모달
- 프로필 설정
  - Google OAuth 신규 가입 후 닉네임 입력
- 워크스페이스
  - 파일 트리
  - Monaco 코드 에디터
  - Save 버튼
  - Run 터미널 패널
  - 팀 채팅 패널

## 7. 백엔드 아키텍처

### 기술 스택

- Spring Boot
- Spring Security + JWT
- JPA
- MySQL
- Redis Pub/Sub
- WebSocket/STOMP
- Docker

### 주요 도메인

- Auth
  - Google OAuth
  - JWT Access Token
  - Refresh Token
  - Guest Session
- User
  - 현재 사용자 조회
  - 프로필 수정
  - 초대 가능한 회원 검색
- Project
  - 개인/팀/게스트 프로젝트 생성
  - 내 프로젝트 조회
  - 공유 프로젝트 조회
  - 프로젝트 상세/열기/삭제
  - 멤버 초대/수락/권한 변경/제거
- File
  - 파일 트리 조회
  - 파일 생성
  - 파일 내용 조회
  - 파일 저장
  - 파일명 변경/삭제
- Realtime
  - 프로젝트 파일 이벤트
  - CRDT 업데이트
  - 팀 채팅 메시지
- Runtime
  - 런타임 목록
  - 프로젝트 실행

## 8. 핵심 API 요약

| 기능 | Method | Path |
| --- | --- | --- |
| 헬스 체크 | GET | `/api/health` |
| 런타임 목록 | GET | `/api/runtimes` |
| 게스트 세션 생성 | POST | `/api/guest-sessions` |
| Google OAuth 시작 | GET | `/api/auth/oauth/google/authorize` |
| Google OAuth 콜백 | GET | `/api/auth/oauth/google/callback` |
| 현재 사용자 조회 | GET | `/api/users/me` |
| 회원 검색 | GET | `/api/users/search?query=` |
| 프로젝트 생성 | POST | `/api/projects` |
| 내 프로젝트 목록 | GET | `/api/projects/my` |
| 공유 프로젝트 목록 | GET | `/api/projects/shared` |
| 프로젝트 상세 | GET | `/api/projects/{projectId}` |
| 프로젝트 열기 | POST | `/api/projects/{projectId}/open` |
| 파일 트리 | GET | `/api/projects/{projectId}/files/tree` |
| 파일 내용 | GET | `/api/projects/{projectId}/files/{fileId}/content` |
| 파일 생성 | POST | `/api/projects/{projectId}/files` |
| 프로젝트 저장 | POST | `/api/projects/{projectId}/save` |
| 프로젝트 실행 | POST | `/api/projects/{projectId}/run` |

## 9. 실시간 협업 구조

```text
Monaco Editor
  |
  | y-monaco binding
  v
Yjs Document
  |
  | CRDT update
  v
STOMP /app/projects/{projectId}/files/{fileId}/crdt
  |
  v
Spring Boot WebSocket Controller
  |
  v
Redis Pub/Sub: dev-web-ide:crdt-updates
  |
  v
STOMP /topic/projects/{projectId}/files/{fileId}/crdt
  |
  v
Other Browsers
```

팀 채팅은 `/app/projects/{projectId}/chat`과 `/topic/projects/{projectId}/chat`을 사용하며, Redis 채널 `dev-web-ide:chat-messages`로 fan-out됩니다.

## 10. 데이터 저장 구조

```text
RDS MySQL
  - users
  - oauth_accounts
  - auth_sessions
  - guest_sessions
  - projects
  - project_members
  - project_files
  - project_save_batches
  - file_versions
  - workspace_sessions
  - container_instances
  - terminal_logs

EFS
  - /app/storage/projects/{project}
  - 현재 파일 원본
  - .versions/{projectFileId}/v{versionNo}
```

## 11. CI/CD

### FE 배포

1. GitHub Actions에서 FE 빌드
2. S3 버킷에 정적 파일 업로드
3. CloudFront invalidation 실행
4. 사용자는 CloudFront HTTPS 주소로 접근

### BE 배포

1. GitHub Actions에서 Gradle test/build
2. Docker 이미지 생성
3. ECR에 이미지 push
4. ECS task definition 갱신
5. ECS service rolling update

## 12. 구현 완료 범위

- FE S3/CloudFront 배포
- API CloudFront/ALB/ECS 배포
- RDS MySQL 연결
- EFS 파일 저장 연결
- Google OAuth 로그인
- 프로젝트 생성/조회/열기/저장
- 파일 트리/파일 생성/파일 내용 조회
- Save 버튼과 Monaco 변경 감지 연동
- 프로젝트 실행 API와 터미널 UI 연동
- 실제 회원 검색 기반 팀원 초대
- 초대받은 프로젝트의 공유 목록 표시
- Redis 기반 프로젝트 이벤트/CRDT/채팅 메시지 코드 구현
- CI/CD 자동 배포 workflow

## 13. 남은 운영 개선 사항

- ElastiCache Redis 운영 생성 및 ECS 환경변수 반영
- DB password, JWT secret의 Secrets Manager 또는 Parameter Store 이전
- RDS 보안 그룹 inbound 축소
- API 커스텀 도메인 연결
- ALB와 CloudFront origin 구간 HTTPS 적용
- 팀 초대 수락 UX 강화
- 터미널 로그 실시간 스트리밍 고도화
- CRDT 스냅샷/복구 전략 추가

## 14. 결론

이 프로젝트는 단순한 코드 편집 UI를 넘어, 실제 배포 가능한 클라우드 기반 Web IDE 구조를 구현했습니다. 정적 프론트엔드 배포, 컨테이너 기반 백엔드, RDS/EFS 저장소, OAuth 인증, 프로젝트/파일 API, 실시간 협업 기반까지 연결해 웹 IDE의 핵심 사용자 흐름을 end-to-end로 구성했습니다.
