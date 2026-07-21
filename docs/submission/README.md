# Dev Web IDE 제출 산출물

이 폴더는 프로젝트 제출에 필요한 문서와 시연 자료를 모아둔 패키지입니다.

## 제출 파일

- `architecture-and-planning.md`  
  전체 시스템 아키텍처 및 기획 문서입니다.
- `editor-file-tree-ui-result.md`  
  코드 에디터와 파일 트리 UI 결과물 설명 문서입니다.
- `presentation.html`  
  브라우저에서 바로 열 수 있는 발표용 슬라이드입니다.
- `presentation-script.md`  
  발표 대본입니다.
- `demo-script-and-subtitles.vtt`  
  무음 시연 영상용 자막 파일입니다.
- `demo-recording-guide.md`  
  시연 영상 구성과 녹화 안내입니다.
- `demo-video-source.html`  
  무음 WebM 시연 영상을 생성하기 위한 브라우저 기반 영상 소스입니다.
- `generate-demo-video.mjs`  
  시스템 Chrome과 임시 `playwright-core` 설치를 이용해 WebM을 생성하는 스크립트입니다.
- `assets/`  
  화면 캡처와 시연용 이미지가 저장되는 폴더입니다.
- `video/`  
  생성 가능한 영상 파일이 저장되는 폴더입니다.

## 권장 제출 방식

1. `architecture-and-planning.md`를 PDF로 변환하거나 Markdown 그대로 제출합니다.
2. `editor-file-tree-ui-result.md`와 `assets/` 화면 캡처를 함께 제출합니다.
3. `presentation.html`을 발표자료로 사용합니다.
4. `video/dev-web-ide-demo.webm`을 시연 영상으로 제출합니다. 영상 포맷 제한이 있으면 `demo-recording-guide.md`의 순서대로 다시 녹화하거나 WebM을 MP4로 변환합니다.

## 생성된 보조 파일

- `assets/local-dashboard.png`: 로컬 FE preview 대시보드 캡처
- `assets/presentation-cover.png`: 발표자료 첫 화면 캡처
- `assets/demo-source-preview.png`: 시연 영상 소스 화면 캡처
- `video/dev-web-ide-demo.webm`: 무음 자막형 시연 영상
