# Dev Web IDE Backend

Goorm Dev Web IDE의 백엔드 프로젝트입니다.

## Documentation
- [Architecture Design](./docs/architecture.md)

## Tech Stack
- Java 25
- Spring Boot
- Gradle

## Getting Started
### Prerequisites
- JDK 25
- Gradle

### Run
```bash
./gradlew bootRun
```

### Docker
```bash
docker build -t dev-web-ide-be:local .
```

예시 파일을 복사해서 로컬 운영 환경변수 파일을 생성합니다.

```bash
cp .env.prod.example .env.prod
```

아래 값은 직접 채워야 합니다.

```text
SPRING_DATASOURCE_PASSWORD
APP_JWT_SECRET
```

운영 프로필 컨테이너를 실행합니다.

```bash
docker run --rm --env-file .env.prod -p 8080:8080 dev-web-ide-be:local
```

헬스 체크:

```bash
curl http://localhost:8080/api/health
```
