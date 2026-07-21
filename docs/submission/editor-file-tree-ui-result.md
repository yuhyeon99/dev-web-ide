# 코드 에디터 및 파일 트리 UI 결과물

## 1. 결과물 개요

워크스페이스 화면은 웹 IDE의 핵심 작업 공간입니다. 사용자는 왼쪽 파일 트리에서 파일과 폴더를 탐색하고, 중앙 Monaco Editor에서 코드를 작성하며, 하단 터미널에서 실행 결과를 확인합니다. 팀 프로젝트인 경우 오른쪽 하단에 Team Chat 패널이 추가되어 같은 프로젝트를 진행하는 팀원들과 실시간으로 소통할 수 있습니다.

## 1.1 첨부 화면 캡처

- `assets/local-dashboard.png`: 실제 로컬 FE preview 대시보드 화면
- `assets/demo-source-preview.png`: 코드 에디터/파일 트리/자막형 시연 화면
- `assets/presentation-cover.png`: 발표자료 표지 화면

## 2. UI 구성

```text
Workspace
  |
  +-- Header
  |     +-- 프로젝트 선택
  |     +-- 저장 상태
  |     +-- Save 버튼
  |
  +-- Sidebar
  |     +-- Explorer
  |     +-- File Tree
  |     +-- 파일 생성
  |     +-- 폴더 생성
  |
  +-- Editor
  |     +-- Tab Bar
  |     +-- Monaco Editor
  |     +-- CRDT binding
  |
  +-- Bottom Panel
        +-- Terminal
        +-- Team Chat
```

## 3. 파일 트리 UI

### 구현 기능

- 프로젝트 루트 디렉터리부터 계층형 파일 트리 표시
- 파일과 폴더 타입 분리
- 현재 선택된 파일 경로 강조
- 새 파일 생성
- 새 폴더 생성
- 프로젝트 파일 이벤트 수신 후 파일 트리 자동 갱신

### API 연동

| 기능 | API |
| --- | --- |
| 파일 트리 조회 | `GET /api/projects/{projectId}/files/tree` |
| 파일 생성 | `POST /api/projects/{projectId}/files` |
| 파일 내용 조회 | `GET /api/projects/{projectId}/files/{fileId}/content` |
| 프로젝트 이벤트 구독 | `/topic/projects/{projectId}/events` |

## 4. 코드 에디터 UI

### 구현 기능

- Monaco Editor 기반 코드 편집
- 파일 확장자별 언어 모드 자동 설정
- 여러 파일 탭 표시
- 변경된 파일 dirty 표시
- Save 버튼으로 변경분 저장
- Yjs/y-monaco 기반 CRDT 공동 편집

### 파일 확장자별 언어 매핑

| 확장자 | Monaco Language |
| --- | --- |
| `.ts`, `.tsx`, `.jsx` | `typescript` |
| `.css` | `css` |
| `.json` | `json` |
| `.md` | `markdown` |
| `.py` | `python` |
| `.java` | `java` |
| `.cpp`, `.cc` | `cpp` |
| 기타 | `plaintext` |

## 5. 저장 흐름

```text
Monaco Editor 변경
  |
  v
fileDrafts 상태 갱신
  |
  v
Save 버튼 클릭
  |
  v
POST /api/projects/{projectId}/save
  |
  v
RDS: 저장 배치/파일 버전 메타데이터
EFS: 실제 파일 원본과 버전 파일
  |
  v
프로젝트 이벤트 발행
  |
  v
다른 클라이언트 파일 트리/파일 내용 갱신
```

## 6. 실시간 공동 편집 흐름

```text
사용자 A 입력
  |
  v
Yjs update 생성
  |
  v
STOMP publish
  |
  v
Spring Boot WebSocket Controller
  |
  v
Redis Pub/Sub
  |
  v
사용자 B 브라우저로 update 전달
  |
  v
Y.applyUpdate
  |
  v
Monaco Editor 화면 반영
```

## 7. 터미널 UI

### 구현 기능

- Run 버튼으로 프로젝트 실행 요청
- 실행 세션 ID, 컨테이너 ID, 런타임, Docker image, EFS mount path 표시
- 실행 중/실패/대기 상태 표시

### API 연동

| 기능 | API |
| --- | --- |
| 프로젝트 실행 | `POST /api/projects/{projectId}/run` |

## 8. 팀 채팅 UI

### 구현 기능

- 팀 프로젝트에서만 Team Chat 패널 표시
- WebSocket 기반 메시지 송수신
- 본인 메시지와 상대 메시지 스타일 구분
- 최근 100개 메시지 클라이언트 보관
- Redis Pub/Sub 기반 다중 서버 fan-out 구조

### WebSocket 경로

| 기능 | Destination |
| --- | --- |
| 채팅 발행 | `/app/projects/{projectId}/chat` |
| 채팅 구독 | `/topic/projects/{projectId}/chat` |

## 9. 대시보드 연계 UI

### 내 프로젝트

- 로그인 사용자가 생성한 프로젝트 목록 표시
- 클릭 시 프로젝트 열기 API 호출 후 워크스페이스 이동

### 공유 프로젝트

- 초대받은 팀 프로젝트 목록 표시
- `INVITED`, `ACTIVE` 멤버 상태의 프로젝트 표시
- 클릭 시 프로젝트 열기 API 호출 후 워크스페이스 이동

### 팀원 초대

- 팀 프로젝트 생성 시 가입된 실제 회원 검색
- 닉네임 또는 이메일 2글자 이상 검색
- 선택한 회원 ID를 `memberUserIds`로 프로젝트 생성 API에 전달

## 10. UI 결과 요약

현재 UI는 단순 퍼블리싱 상태를 넘어 실제 API와 연결된 Web IDE 작업 공간으로 동작합니다. 파일 트리, 코드 편집, 저장, 실행, 팀 초대, 공유 프로젝트 진입, 실시간 공동 편집, 팀 채팅까지 프로젝트의 핵심 기능을 하나의 워크스페이스 흐름 안에서 사용할 수 있도록 구현했습니다.
