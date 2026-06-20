# Web IDE Architecture

## 1. 프로젝트 개요

이 프로젝트는 브라우저 기반 웹 IDE 플랫폼이다.

사용자는 로컬 PC에 개발 환경을 직접 설치하지 않고, 브라우저에서 프로젝트를 생성하고, 코드를 작성하고, 실행하고, 팀원과 협업할 수 있다.

이 프로젝트의 설계는 API 목록을 먼저 정의한 뒤, 해당 API를 기준으로 데이터 구조를 역설계하는 방식으로 진행했다.

설계 진행 순서는 다음과 같다.

```text
1. 필요한 API 목록 정리
   ↓
2. API를 처리하기 위한 도메인 도출
   ↓
3. Mermaid ERD 작성
   ↓
4. 테이블 명세서 작성
   ↓
5. 설계 메모 작성
   ↓
6. Entity / Repository / Service / Controller 구현
```

---

## 2. 설계 기준

이 프로젝트는 API 기반으로 ERD를 설계했다.

먼저 Auth, Project, Workspace/File, Runtime API를 정리하고, 각 API가 어떤 데이터를 필요로 하는지 기준으로 테이블을 분리했다.

예를 들어 다음 API들을 기준으로 도메인을 나눴다.

```text
Auth
- OAuth 로그인
- 회원가입
- 현재 사용자 조회
- 로그아웃

Project
- 프로젝트 생성
- 프로젝트 목록 조회
- 프로젝트 열기
- 팀 멤버 초대/권한 변경/제거

Workspace / File
- 파일 트리 조회
- 파일 생성
- 파일 내용 조회
- 파일 저장
- 파일명 변경
- 파일/폴더 삭제

Runtime
- 프로젝트 실행
- 실행 중지
- 터미널 로그 조회
```

이 API 목록을 기준으로 다음 주요 테이블을 도출했다.

```text
USERS
OAUTH_ACCOUNTS
AUTH_SESSIONS
TERMS_AGREEMENTS
GUEST_SESSIONS
RUNTIMES
PROJECTS
PROJECT_MEMBERS
PROJECT_SETTINGS
PROJECT_ACCESS_LOGS
PROJECT_FILES
PROJECT_SAVE_BATCHES
FILE_VERSIONS
WORKSPACE_SESSIONS
CONTAINER_INSTANCES
TERMINAL_LOGS
```

---

## 3. 전체 아키텍처

```text
Client
  ↓
CloudFront
  ↓
S3 (Frontend Hosting)
  ↓
Nginx (Reverse Proxy)
  ↓
Spring Boot API Server
  ↓
RDS MySQL
  ↓
EFS
  ↓
ECS / Fargate
  ↓
Redis
```

---

## 4. 구성 요소 역할

### 4.1 Client

사용자는 브라우저를 통해 웹 IDE에 접근한다.

브라우저에서는 다음 기능을 사용한다.

```text
- OAuth 로그인
- 게스트 진입
- 프로젝트 목록 조회
- 프로젝트 생성
- 프로젝트 열기
- 파일 트리 조회
- 코드 편집
- 수동 저장
- 터미널 실행
- 공동 편집
```

---

### 4.2 CloudFront

CloudFront는 프론트엔드 정적 리소스를 빠르게 전달하기 위한 CDN이다.

React 빌드 결과물을 S3에 저장하고, 사용자는 CloudFront를 통해 가까운 엣지 서버에서 정적 파일을 전달받는다.

주요 역할은 다음과 같다.

```text
- 정적 파일 캐싱
- HTTPS 진입점 제공
- S3 Origin 접근
- 사용자 응답 속도 개선
```

---

### 4.3 Certificate Manager

AWS Certificate Manager는 HTTPS 통신을 위한 SSL/TLS 인증서를 관리한다.

CloudFront에 인증서를 연결하여 사용자가 `https://` 주소로 서비스에 접근할 수 있도록 한다.

---

### 4.4 S3

S3는 프론트엔드 빌드 결과물을 저장하는 정적 파일 저장소로 사용한다.

React 빌드 후 생성되는 파일을 S3에 업로드한다.

```text
index.html
main.js
style.css
assets/*
```

S3는 프로젝트 파일 저장소로 사용하지 않는다.

프로젝트 파일은 EFS에 저장한다.

---

### 4.5 Nginx

Nginx는 백엔드 API 요청의 진입점 역할을 한다.

주요 역할은 다음과 같다.

```text
- Reverse Proxy
- API 요청을 Spring Boot 서버로 전달
- 정적 요청과 API 요청 분리
- 로드 밸런싱 확장 가능
- HTTPS 처리 가능
```

API 요청 흐름은 다음과 같다.

```text
Client
  ↓
Nginx
  ↓
Spring Boot API Server
  ↓
RDS / EFS / Redis / ECS
```

API 응답은 Spring Boot가 생성하고, Nginx는 요청과 응답을 중계한다.

---

### 4.6 Spring Boot API Server

Spring Boot는 웹 IDE의 핵심 비즈니스 로직을 처리한다.

Spring Boot 애플리케이션은 내장 Tomcat 위에서 실행된다.

주요 역할은 다음과 같다.

```text
Auth
- OAuth 로그인 처리
- 회원가입 처리
- 게스트 세션 처리
- 현재 사용자 조회
- 로그아웃 처리

Project
- 프로젝트 생성
- 프로젝트 목록 조회
- 프로젝트 상세 조회
- 프로젝트 열기 처리
- 팀 멤버 초대
- 멤버 권한 변경
- 멤버 제거

Workspace / File
- 파일 트리 조회
- 파일 생성
- 파일 내용 조회
- 파일 저장
- 파일명 변경
- 파일/폴더 삭제
- 프로젝트 설정 변경

Runtime
- 프로젝트 실행
- 실행 중지
- 터미널 로그 조회
- 컨테이너 상태 관리
```

---

## 5. 데이터 저장 구조

이 프로젝트는 파일 원본과 메타데이터를 분리해서 저장한다.

```text
파일 원본
→ EFS

메타데이터
→ RDS MySQL
```

### 5.1 RDS MySQL 저장 대상

RDS MySQL에는 서비스 운영에 필요한 메타데이터를 저장한다.

```text
- 사용자 정보
- OAuth 계정 정보
- 인증 세션
- 약관 동의 정보
- 게스트 세션
- 런타임 정보
- 프로젝트 정보
- 프로젝트 멤버 및 권한
- 프로젝트 설정
- 프로젝트 접근 기록
- 파일/폴더 메타데이터
- 저장 배치 이력
- 파일 버전 정보
- 워크스페이스 세션
- 컨테이너 실행 기록
- 터미널 로그
```

### 5.2 EFS 저장 대상

EFS에는 실제 프로젝트 파일을 저장한다.

```text
/projects/{projectId}/
  ├── src/
  ├── package.json
  ├── README.md
  └── ...
```

컨테이너는 EFS를 마운트해서 프로젝트 파일에 접근한다.

컨테이너가 종료되어도 EFS에 저장된 프로젝트 파일은 삭제되지 않는다.

---

## 6. ERD 기준

ERD 기준 문서는 `docs/erd.md`이다.

ERD는 API 목록을 기준으로 작성되었으며, 다음 내용을 포함한다.

```text
1. Mermaid ERD
   - 전체 테이블 관계 시각화

2. 테이블 명세서
   - PK
   - FK
   - NN
   - UK
   - VARCHAR 길이
   - ENUM 값
   - DATETIME 컬럼

3. 설계 메모
   - 왜 테이블을 분리했는지
   - 어떤 API와 연결되는지
   - MVP에서 먼저 구현할 테이블
   - 추후 확장 가능한 테이블
```

엔티티 구현 시 `docs/erd.md`를 우선 기준으로 한다.

새 기능을 구현할 때 ERD에 없는 테이블이 필요하면 바로 구현하지 않고, 먼저 ERD 변경안을 작성한다.

---

## 7. 주요 도메인

API와 ERD 기준으로 도메인은 다음과 같이 나눈다.

```text
auth
user
runtime
project
workspace
file
execution
terminal
collaboration
```

추천 패키지 구조는 다음과 같다.

```text
com.yuhyeon.devwebide
 ├── auth
 ├── user
 ├── runtime
 ├── project
 ├── workspace
 ├── file
 ├── execution
 ├── terminal
 └── collaboration
```

각 도메인 내부는 필요에 따라 다음 구조를 사용한다.

```text
domain
application
presentation
infrastructure
```

---

## 8. 인증 정책

지원 인증 방식은 다음과 같다.

```text
- GitHub OAuth
- Google OAuth
- Kakao OAuth
- 게스트 세션
```

회원 사용자는 `USERS` 기준으로 관리한다.

OAuth 계정 연결 정보는 `OAUTH_ACCOUNTS`에서 관리한다.

게스트 사용자는 `GUEST_SESSIONS` 기준으로 관리한다.

로그인 세션은 `AUTH_SESSIONS` 기준으로 관리한다.

신규 OAuth 회원가입 시 닉네임과 약관 동의 정보는 `USERS`, `OAUTH_ACCOUNTS`, `TERMS_AGREEMENTS`에 저장한다.

---

## 9. 프로젝트 정책

프로젝트는 개인 프로젝트, 팀 프로젝트, 게스트 프로젝트를 지원한다.

```text
개인 프로젝트
→ USERS.owner_user_id 기준

팀 프로젝트
→ PROJECTS + PROJECT_MEMBERS 기준

게스트 프로젝트
→ GUEST_SESSIONS.guest_session_id 기준
```

프로젝트 생성 시 템플릿 선택 기능은 현재 범위에 포함하지 않는다.

프로젝트 생성 시 런타임은 `RUNTIMES`에서 선택한다.

---

## 10. 파일 저장 정책

프로젝트 파일 원본은 DB에 저장하지 않는다.

```text
파일 원본
→ EFS

파일 메타데이터
→ PROJECT_FILES

저장 이력
→ PROJECT_SAVE_BATCHES

파일 버전 메타데이터
→ FILE_VERSIONS
```

저장 방식은 수동 저장을 기본 정책으로 한다.

`POST /api/projects/{projectId}/save` API는 열린 dirty 파일을 일괄 저장하는 방식으로 처리한다.

---

## 11. 런타임 및 컨테이너 정책

런타임 목록은 `RUNTIMES` 테이블에서 관리한다.

예상 런타임은 다음과 같다.

```text
- Node.js
- Python
- Java
- C++
```

프로젝트 실행 환경은 Docker 컨테이너로 분리한다.

컨테이너 실행 정보는 `CONTAINER_INSTANCES`에 기록한다.

워크스페이스 실행 세션은 `WORKSPACE_SESSIONS`에 기록한다.

```text
WORKSPACE_SESSIONS
→ 사용자가 프로젝트를 실행한 논리적 세션

CONTAINER_INSTANCES
→ ECS/Fargate에서 실행된 실제 컨테이너 정보
```

---

## 12. 컨테이너 종료 정책

마지막 사용자 연결 종료 후 30분 뒤 컨테이너를 종료한다.

```text
마지막 사용자 연결 종료
  ↓
30분 idle timer 시작
  ↓
30분 내 재접속 발생
  ├─ timer 취소
  └─ 기존 컨테이너 재사용

30분 내 재접속 없음
  ↓
컨테이너 종료
  ↓
EFS 프로젝트 데이터 유지
```

컨테이너 종료는 프로젝트 삭제가 아니다.

---

## 13. Redis 사용 정책

Redis는 공동 편집 전용 저장소가 아니라 시스템 공통 상태 관리 인프라로 사용한다.

활용 범위는 다음과 같다.

```text
- 사용자 세션 상태 관리
- 프로젝트별 활성 사용자 수 관리
- WebSocket 서버 간 이벤트 공유
- Pub/Sub 메시지 전달
- 컨테이너 idle timer 관리 보조
- 캐시 처리
```

---

## 14. 협업 정책

실시간 공동 편집은 WebSocket과 CRDT 기반으로 구현한다.

공동 편집 기능은 다음 책임을 가진다.

```text
- 동일 파일 동시 편집
- 변경사항 충돌 방지
- 사용자 커서 위치 공유
- 접속 사용자 상태 표시
```

커서 위치 공유 여부는 `PROJECT_SETTINGS.share_cursor_position` 기준으로 제어한다.

게스트 편집 허용 여부는 `PROJECT_SETTINGS.guest_can_edit` 기준으로 제어한다.

---

## 15. API와 테이블 연결 원칙

API 구현 시 먼저 해당 API가 어떤 테이블과 연결되는지 확인한다.

예시는 다음과 같다.

```text
GET /api/runtimes
→ RUNTIMES

POST /api/projects
→ PROJECTS
→ PROJECT_MEMBERS
→ PROJECT_SETTINGS
→ PROJECT_FILES
→ EFS 디렉토리 생성

GET /api/projects/recent
→ PROJECT_ACCESS_LOGS
→ PROJECTS

POST /api/projects/{projectId}/open
→ PROJECT_ACCESS_LOGS
→ WORKSPACE_SESSIONS

POST /api/projects/{projectId}/save
→ PROJECT_SAVE_BATCHES
→ FILE_VERSIONS
→ PROJECT_FILES
→ EFS 파일 저장

프로젝트 실행
→ WORKSPACE_SESSIONS
→ CONTAINER_INSTANCES
→ RUNTIMES
→ EFS 마운트
```

---

## 16. MVP 구현 순서

초기 구현은 전체 테이블을 한 번에 구현하지 않고 API 연결 우선순위에 따라 진행한다.

### 1차 MVP

```text
- users
- oauth_accounts
- auth_sessions
- guest_sessions
- terms_agreements
- runtimes
- projects
- project_members
- project_settings
- project_access_logs
- project_files
```

### 2차 확장

```text
- project_save_batches
- file_versions
- workspace_sessions
- container_instances
- terminal_logs
```

### 추천 API 구현 순서

```text
1. GET /api/runtimes
2. POST /api/projects
3. GET /api/projects/my
4. GET /api/projects/{projectId}
5. POST /api/projects/{projectId}/open
6. 파일 트리 조회
7. 파일 저장
8. 프로젝트 실행
9. 터미널 로그 조회
```
