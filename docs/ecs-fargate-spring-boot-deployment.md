# ECS Fargate로 Spring Boot 백엔드 배포하기

이번 프로젝트에서는 Spring Boot로 만든 Web IDE 백엔드를 AWS에 배포해야 했다.

처음에는 단순히 “EC2 하나 띄워서 jar 실행하면 되는 거 아닌가?”라고 생각했다.  
하지만 프로젝트가 점점 커지면서 배포 방식도 조금 더 운영 환경에 가까운 구조가 필요해졌다.

결국 선택한 방식은 **Docker + ECR + ECS Fargate + ALB** 조합이었다.

## 왜 ECS Fargate를 사용했을까?

백엔드를 배포하는 방법은 여러 가지가 있다.

가장 익숙한 방식은 EC2에 직접 접속해서 Spring Boot jar 파일을 실행하는 것이다.

```bash
java -jar app.jar
```

이 방식은 단순하지만 운영 관점에서는 직접 관리해야 할 것이 많다.

- EC2 서버 패치
- Java 설치
- 프로세스 관리
- 배포 중단 처리
- 장애 시 재시작
- 로그 관리
- 스케일 아웃

반면 ECS Fargate는 서버를 직접 관리하지 않고, **컨테이너 실행 단위로 백엔드를 배포**할 수 있다.

즉, 내가 관리하는 것은 EC2 서버가 아니라 Docker 이미지와 실행 설정이다.

## 전체 구조

이번 백엔드 배포 구조는 대략 이렇게 구성했다.

```text
사용자
  ↓
CloudFront
  ↓
ALB
  ↓
ECS Fargate
  ↓
Spring Boot Container
  ↓
RDS MySQL / EFS / Redis
```

프론트엔드는 S3와 CloudFront로 배포했고, 백엔드는 ECS Fargate에서 실행했다.

API 요청은 CloudFront를 거쳐 ALB로 전달되고, ALB가 ECS에서 실행 중인 Spring Boot 컨테이너로 요청을 보낸다.

## ECR은 무엇인가?

ECR은 **Docker 이미지를 저장하는 AWS의 이미지 저장소**다.

Spring Boot 애플리케이션을 바로 ECS에 올리는 것이 아니라, 먼저 Docker 이미지로 만든다.

```text
Spring Boot 코드
  ↓
Docker build
  ↓
Docker image
  ↓
ECR push
  ↓
ECS에서 image pull 후 실행
```

쉽게 말하면 ECR은 백엔드 배포용 Docker 이미지를 보관하는 공간이다.

프로젝트에서는 다음과 같은 ECR repository를 만들었다.

```text
dev-web-ide-be
```

그리고 GitHub Actions나 로컬에서 이미지를 빌드한 뒤 ECR에 push했다.

## ECS Fargate의 핵심 개념

처음 ECS를 보면 용어가 많아서 헷갈린다.

FE 개발자 입장에서 이해하기 쉽게 정리하면 아래와 같다.

### Cluster

컨테이너들이 실행되는 논리적인 공간이다.

```text
dev-web-ide-prod-cluster
```

하나의 프로젝트나 서비스 단위로 묶는 상위 그룹이라고 보면 된다.

### Task Definition

컨테이너 실행 설명서다.

여기에는 다음 정보가 들어간다.

- 어떤 Docker 이미지를 실행할지
- CPU와 메모리를 얼마나 쓸지
- 컨테이너 포트는 몇 번인지
- 환경변수는 무엇인지
- 로그는 어디로 보낼지
- EFS를 어디에 마운트할지

Spring Boot 백엔드라면 보통 컨테이너 포트는 `8080`으로 둔다.

### Task

Task Definition을 기반으로 실제 실행된 컨테이너 인스턴스다.

쉽게 말하면 “현재 떠 있는 백엔드 한 대”라고 볼 수 있다.

### Service

Task를 계속 유지해주는 관리자다.

예를 들어 desired count를 `1`로 설정하면 ECS Service는 항상 백엔드 컨테이너가 1개 떠 있도록 관리한다.

컨테이너가 죽으면 다시 띄운다.  
새 버전을 배포하면 기존 Task를 내리고 새 Task를 띄운다.

## ALB는 왜 필요할까?

ECS Task는 새로 배포될 때마다 내부 IP가 바뀔 수 있다.

그래서 사용자가 직접 Task로 접근하게 만들면 안정적이지 않다.

이때 ALB가 앞단에서 고정된 진입점 역할을 한다.

```text
Client
  ↓
ALB
  ↓
현재 살아있는 ECS Task
```

ALB는 Target Group에 등록된 ECS Task로 요청을 전달한다.

Spring Boot API 서버에서는 health check용 API를 만들었다.

```http
GET /api/health
```

ALB는 이 경로로 주기적으로 요청을 보내서 백엔드가 정상인지 확인한다.

정상 응답을 주는 Task에만 트래픽을 보낸다.

## Health Check가 중요한 이유

처음에는 health check가 단순한 상태 확인처럼 보였다.

하지만 실제 배포에서는 꽤 중요하다.

예를 들어 새 버전의 Spring Boot 컨테이너가 실행됐지만 DB 연결 문제로 정상 기동되지 않았다고 가정해보자.

이때 health check가 없다면 ALB가 문제가 있는 컨테이너에도 요청을 보낼 수 있다.

반대로 health check가 있으면 정상적으로 뜬 컨테이너에만 트래픽이 전달된다.

```text
새 Task 실행
  ↓
/api/health 확인
  ↓
200 OK
  ↓
트래픽 연결
```

그래서 ECS 배포에서는 health check API를 거의 필수로 두는 것이 좋다.

## 환경변수와 Secret

Spring Boot 백엔드는 운영 환경에서 여러 설정값이 필요했다.

예를 들면 다음과 같다.

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
APP_JWT_SECRET
GOOGLE_OAUTH_CLIENT_ID
GOOGLE_OAUTH_CLIENT_SECRET
APP_PROJECT_STORAGE_ROOT
```

처음에는 ECS Task Definition의 환경변수로 넣을 수 있다.

하지만 DB password, JWT secret, OAuth secret 같은 값은 장기적으로 AWS Secrets Manager나 Parameter Store로 옮기는 것이 좋다.

일반 환경변수는 콘솔에서 쉽게 보일 수 있기 때문이다.

## EFS는 왜 연결했을까?

컨테이너 내부 파일 시스템은 영속적이지 않다.

ECS Task가 재시작되면 컨테이너 내부에 저장한 파일은 사라질 수 있다.

Web IDE에서는 사용자가 작성한 프로젝트 파일이 유지되어야 한다.

그래서 파일 원본은 EFS에 저장하고, ECS 컨테이너에서는 `/app/storage` 경로로 마운트했다.

```text
Spring Boot Container
  ↓
/app/storage
  ↓
EFS
```

RDS에는 파일 메타데이터와 버전 정보를 저장하고, 실제 파일 내용은 EFS에 저장하는 구조로 분리했다.

## 배포 흐름

최종 배포 흐름은 이렇게 정리할 수 있다.

```text
GitHub push
  ↓
GitHub Actions
  ↓
Gradle test
  ↓
Docker build
  ↓
ECR push
  ↓
ECS Task Definition 등록
  ↓
ECS Service update
  ↓
ALB health check
  ↓
배포 완료
```

이 구조가 잡히면 이후에는 코드를 push하는 것만으로 백엔드가 자동 배포된다.

## 직접 해보면서 헷갈렸던 점

가장 헷갈렸던 부분은 ECR과 ECS의 역할 차이였다.

처음에는 ECR에 이미지를 push하면 배포가 끝난 것처럼 느껴졌다.

하지만 ECR은 단지 이미지를 저장하는 곳이다.

실제로 애플리케이션을 실행하는 것은 ECS다.

```text
ECR = Docker 이미지 저장소
ECS = Docker 이미지 실행 환경
```

또 하나 헷갈렸던 부분은 Task Definition과 Service였다.

Task Definition은 실행 설정이고, Service는 그 설정을 기반으로 Task를 계속 유지하는 역할이다.

```text
Task Definition = 실행 설명서
Task = 실행된 컨테이너
Service = Task를 유지하고 배포하는 관리자
```

이렇게 역할을 나눠서 이해하니 ECS 구조가 훨씬 명확해졌다.

## 마무리

ECS Fargate를 사용하면 EC2 서버를 직접 관리하지 않고도 Spring Boot 백엔드를 컨테이너 기반으로 배포할 수 있다.

이번 프로젝트에서는 ECR에 Docker 이미지를 저장하고, ECS Fargate에서 Spring Boot 컨테이너를 실행했다.  
ALB는 외부 요청을 안정적으로 라우팅했고, health check를 통해 정상 Task에만 트래픽을 전달했다.

또한 RDS, EFS, Redis와 연결하면서 단순 API 서버 배포를 넘어 실제 Web IDE 운영 구조에 가까운 백엔드 인프라를 구성할 수 있었다.

처음에는 AWS 용어가 많아서 어렵게 느껴졌지만, 결국 핵심은 단순했다.

```text
이미지는 ECR에 저장하고,
컨테이너는 ECS에서 실행하고,
요청은 ALB가 받아서 전달한다.
```

이 흐름만 이해하면 Spring Boot 백엔드를 ECS Fargate로 배포하는 구조를 훨씬 쉽게 받아들일 수 있다.
