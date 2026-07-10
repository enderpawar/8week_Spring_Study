# Study Plan — Spring Backend 구현 중심 학습 플래너

> 목적: 인강 완강이 아니라, AI와 함께 기능을 먼저 구현하고 뜯어보면서 Spring 백엔드 기본기를 체득한다.  
> 사용 방식: Claude Code, Codex CLI, Cursor, ChatGPT 같은 AI에게 이 파일을 프로젝트 루트에 둔 뒤 참조하게 한다.  
> 핵심 명령 예시: `1주차 학습 내용 구현`, `2주차 학습 내용 구현`, `이번 주 코드 리뷰`, `오늘 학습 회고 작성`.

---

## 0. AI에게 주는 기본 지시문

AI는 이 저장소에서 학습 보조자이자 페어 프로그래머 역할을 한다.

### AI 역할

- 코드를 대신 완성하는 것이 아니라, 학습자가 이해할 수 있는 구조로 초안을 만든다.
- 한 번에 너무 많은 기술을 넣지 않는다.
- 현재 주차 범위를 넘는 기술은 사용하지 않는다.
- 각 클래스와 주요 메서드에 학습용 주석을 남긴다.
- 코드 생성 후 반드시 다음을 함께 제공한다.
  - 파일 구조
  - 실행 방법
  - Postman 테스트 예시
  - 학습자가 직접 다시 작성해야 할 부분
  - 스스로 답해야 할 질문
- 면접에서 설명할 수 있는 포인트를 따로 정리한다.

### AI가 지켜야 할 금지사항

- 1주차에 JPA, MySQL, Security, JWT, Redis, Docker를 넣지 않는다.
- 2주차 전에는 DB를 붙이지 않는다.
- 3주차 전에는 Security/JWT를 넣지 않는다.
- 전체 취업용 프로젝트를 한 번에 생성하지 않는다.
- 학습자가 이해하기 어려운 과도한 추상화, 디자인 패턴, MSA, Kafka, Kubernetes, WebFlux를 사용하지 않는다.
- Controller에서 Repository를 직접 호출하지 않는다.
- Entity 또는 Domain 객체를 응답으로 직접 노출하지 않는다.

---

## 1. 전체 목표

8주 동안 Spring Boot 기반 백엔드 프로젝트를 직접 구현한다.

### 8주 기본 목표

- Spring Boot API 서버 구현
- Controller / Service / Repository / DTO 계층 분리
- JPA / MySQL 기반 데이터 저장
- Spring Security + JWT 인증/인가
- 예약 도메인 로직 구현
- 예약 시간 중복 검증
- 예외 처리와 공통 응답 포맷
- 테스트 코드 작성
- Swagger API 문서화
- Docker 실행 환경 구성
- README / Velog 정리

### 12주 확장 목표

8주 기본 과정을 끝낸 뒤 아래 고도화를 추가한다.

- MySQL EXPLAIN 기반 쿼리 개선
- Redis 캐싱
- k6 성능 테스트
- HikariCP Connection Pool 설정 실험
- GitHub Actions 테스트 자동화

---

## 2. 기술 스택

### 기본 스택

```text
Java 17 or 21
Spring Boot 3.x
Gradle
Spring Web
Spring Validation
Lombok
Spring Data JPA
MySQL
Spring Security
JWT
JUnit5
Swagger/OpenAPI
Docker
```

### 고도화 스택

```text
Redis
k6
GitHub Actions
HikariCP
MySQL EXPLAIN
```

---

## 3. 학습 방식

이 커리큘럼은 인강 중심이 아니라 구현 중심이다.

### 기본 루틴

```text
1. AI에게 현재 주차 기능 전체 뼈대 생성 요청
2. 서버 실행
3. Postman으로 동작 확인
4. 코드 한 줄씩 읽고 주석으로 해부
5. 일부 파일을 지우고 직접 다시 작성
6. 에러를 만나고 해결
7. README 또는 Velog에 정리
```

### AI 사용 원칙

AI에게는 기능 단위로 요청한다.

좋은 요청:

```text
1주차 학습 내용 구현
2주차 JPA/MySQL 적용
예약 시간 중복 검증 로직 구현
이번 주 코드 리뷰
Velog 회고 초안 작성
```

나쁜 요청:

```text
Spring 백엔드 취업용 프로젝트 전체 완성해줘
실무급으로 다 넣어줘
JPA, Security, Redis, Docker, CI/CD 한 번에 넣어줘
```

---

## 4. AI 명령어 인덱스

아래 문장을 AI에게 입력하면 해당 주차 목표를 수행한다.

| 명령어 | AI가 해야 할 일 |
|---|---|
| `1주차 학습 내용 구현` | InMemory Todo API 전체 뼈대 생성 |
| `1주차 코드 해부` | Todo API의 흐름과 계층 역할 설명 |
| `1주차 다시 쓰기 순서` | 어떤 파일부터 지우고 다시 쓸지 안내 |
| `1주차 코드 리뷰` | 1주차 코드가 학습 목표에 맞는지 점검 |
| `2주차 학습 내용 구현` | MySQL/JPA 적용, StudyRoom CRUD 구현 |
| `2주차 코드 해부` | Entity, Repository, Transaction, DTO 분리 설명 |
| `3주차 학습 내용 구현` | 회원가입, 로그인, JWT 인증 구현 |
| `4주차 학습 내용 구현` | 예약 생성/취소/내 예약 조회 구현 |
| `5주차 학습 내용 구현` | 예약 시간 중복 검증 구현 |
| `6주차 학습 내용 구현` | 리뷰, 찜, 검색/필터링 구현 |
| `7주차 학습 내용 구현` | 테스트 코드, Swagger, 공통 응답 정리 |
| `8주차 학습 내용 구현` | Docker, 배포 준비, README 정리 |
| `9주차 고도화 구현` | MySQL EXPLAIN, 인덱스 개선 |
| `10주차 고도화 구현` | Redis 캐싱 적용 |
| `11주차 고도화 구현` | k6 성능 테스트, Connection Pool 실험 |
| `12주차 고도화 구현` | GitHub Actions 테스트 자동화 |
| `오늘 학습 회고 작성` | 오늘 구현한 기능, 막힌 점, 배운 점 정리 |
| `면접 질문 생성` | 현재 코드 기준 면접 예상 질문 생성 |

---

# 5. 1주차 — Spring Boot 기본 구조 익히기

## 목표

Spring Boot API 서버의 기본 흐름을 이해한다.
DB, JPA, Security, JWT는 사용하지 않는다.

## 구현 프로젝트

```text
todo-api
```

## 사용 기술

```text
Java 17
Spring Boot 3.x
Gradle
Spring Web
Validation
Lombok
InMemory Map 저장소
```

## 구현 기능

1. Hello API
2. Todo 생성
3. Todo 목록 조회
4. Todo 단건 조회
5. Todo 수정
6. Todo 완료 처리
7. Todo 삭제
8. 존재하지 않는 Todo 예외 처리
9. Validation 실패 처리
10. GlobalExceptionHandler 기반 JSON 에러 응답

## 요구 API

```http
GET /api/hello
POST /api/todos
GET /api/todos
GET /api/todos/{id}
PATCH /api/todos/{id}
PATCH /api/todos/{id}/complete
DELETE /api/todos/{id}
```

## 권장 패키지 구조

```text
src/main/java/com/example/todo
├── controller
│   ├── HelloController.java
│   └── TodoController.java
├── service
│   └── TodoService.java
├── repository
│   ├── TodoRepository.java
│   └── InMemoryTodoRepository.java
├── domain
│   └── Todo.java
├── dto
│   ├── HelloResponse.java
│   ├── TodoCreateRequest.java
│   ├── TodoUpdateRequest.java
│   ├── TodoResponse.java
│   └── ErrorResponse.java
└── exception
    ├── TodoNotFoundException.java
    └── GlobalExceptionHandler.java
```

## `1주차 학습 내용 구현` 명령을 받았을 때 AI가 해야 할 일

AI는 아래 조건에 맞게 전체 코드를 생성한다.

```text
나는 Spring 백엔드를 처음 제대로 공부하는 컴퓨터공학과 3학년 학생이다.
이번 1주차 목표는 Spring Boot의 기본 구조를 이해하는 것이다.
DB, JPA, Security, JWT는 아직 사용하지 않는다.
InMemory Map<Long, Todo> 저장소 기반 Todo API를 만들어라.
Controller, Service, Repository, DTO, Domain, Exception을 분리해라.
각 클래스 상단에 왜 필요한 클래스인지 주석을 달아라.
주요 메서드에도 학습용 주석을 달아라.
마지막에는 Postman 테스트 예시와 학습 질문 10개를 제공하라.
```

## 1주차 완료 기준

- [ ] 서버가 실행된다.
- [ ] `GET /api/hello`가 성공한다.
- [ ] Todo 생성이 된다.
- [ ] Todo 목록 조회가 된다.
- [ ] Todo 단건 조회가 된다.
- [ ] Todo 수정이 된다.
- [ ] Todo 완료 처리가 된다.
- [ ] Todo 삭제가 된다.
- [ ] 존재하지 않는 Todo 요청 시 JSON 에러가 내려온다.
- [ ] Validation 실패 시 JSON 에러가 내려온다.
- [ ] Controller, Service, Repository, DTO, Domain 역할을 설명할 수 있다.
- [ ] 일부 코드를 지우고 다시 작성했다.
- [ ] README 또는 Velog에 1주차 회고를 작성했다.

## 1주차 다시 쓰기 순서

1. `TodoCreateRequest`
2. `TodoUpdateRequest`
3. `TodoResponse`
4. `Todo` 도메인 객체
5. `InMemoryTodoRepository`
6. `TodoService`의 create/update/complete 메서드
7. `TodoController`의 create/update/complete 메서드
8. `TodoNotFoundException`
9. `GlobalExceptionHandler`

## 1주차 스스로 답해야 할 질문

1. Controller는 왜 필요한가?
2. Service는 왜 필요한가?
3. Repository는 왜 필요한가?
4. DTO는 왜 필요한가?
5. Domain 객체는 어떤 역할인가?
6. Controller에서 Repository를 직접 부르면 왜 안 좋은가?
7. Todo 상태 변경을 Todo 내부 메서드로 처리하면 뭐가 좋은가?
8. TodoNotFoundException은 어디서 발생해야 하는가?
9. GlobalExceptionHandler는 왜 필요한가?
10. 다음 주 JPA로 바꾸면 어떤 코드가 가장 많이 바뀌는가?

---

# 6. 2주차 — JPA/MySQL 적용

## 목표

InMemory 저장소를 MySQL/JPA 기반 저장소로 바꾼다.

## 구현 기능

1. MySQL 연결
2. StudyRoom Entity 생성
3. StudyRoom CRUD API 구현
4. User Entity 생성
5. DTO와 Entity 분리
6. Validation 적용
7. Transaction 적용

## 요구 API

```http
POST /api/study-rooms
GET /api/study-rooms
GET /api/study-rooms/{id}
PATCH /api/study-rooms/{id}
DELETE /api/study-rooms/{id}
```

## `2주차 학습 내용 구현` 명령을 받았을 때 AI가 해야 할 일

- 1주차 구조를 유지하되 저장소를 JPA Repository로 전환한다.
- `Todo`를 유지해도 되지만, 최종 포트폴리오 주제인 StudyRoom 도메인으로 확장한다.
- Entity와 DTO를 분리한다.
- Service 계층에 `@Transactional`을 적용한다.
- MySQL 연결을 위한 `application.yml` 예시를 제공한다.
- JPA로 바꾸면서 1주차 InMemory 구조와 무엇이 달라졌는지 설명한다.

## 2주차 완료 기준

- [ ] MySQL에 연결된다.
- [ ] StudyRoom 데이터가 DB에 저장된다.
- [ ] StudyRoom CRUD가 동작한다.
- [ ] Entity와 DTO 차이를 설명할 수 있다.
- [ ] `@Transactional`을 어디에 붙였는지 설명할 수 있다.
- [ ] JPA Repository가 어떤 역할을 하는지 설명할 수 있다.

## 2주차 스스로 답해야 할 질문

1. Entity와 DTO는 왜 분리하는가?
2. JPA Repository는 어떤 코드를 대신 작성해주는가?
3. `@Transactional`은 왜 Service에 붙이는가?
4. 영속성 컨텍스트는 무엇인가?
5. InMemory Repository와 JpaRepository의 차이는 무엇인가?

---

# 7. 3주차 — 회원가입, 로그인, JWT

## 목표

인증/인가가 있는 백엔드 서버로 만든다.

## 구현 기능

1. 회원가입
2. 로그인
3. 비밀번호 암호화
4. JWT Access Token 발급
5. 내 정보 조회
6. 로그인한 사용자만 접근 가능한 API 구성

## 요구 API

```http
POST /api/auth/signup
POST /api/auth/login
GET /api/members/me
```

## `3주차 학습 내용 구현` 명령을 받았을 때 AI가 해야 할 일

- Spring Security와 JWT를 적용한다.
- `PasswordEncoder`를 사용해 비밀번호를 암호화한다.
- 회원가입과 로그인 흐름을 분리한다.
- JWT 필터가 요청에서 토큰을 읽고 인증 객체를 만드는 흐름을 설명한다.
- Security 설정이 너무 복잡해지지 않게 학습용으로 작성한다.
- Postman에서 Authorization 헤더로 테스트하는 방법을 제공한다.

## 3주차 완료 기준

- [ ] 회원가입이 된다.
- [ ] 로그인 시 JWT가 발급된다.
- [ ] JWT를 이용해 내 정보 조회가 된다.
- [ ] 비밀번호가 평문으로 저장되지 않는다.
- [ ] 인증과 인가 차이를 설명할 수 있다.
- [ ] Security Filter Chain 흐름을 대략 설명할 수 있다.

---

# 8. 4주차 — 예약 기능 구현

## 목표

스터디룸 예약 도메인의 핵심 기능을 구현한다.

## 구현 기능

1. 스터디룸 예약 생성
2. 내 예약 목록 조회
3. 예약 취소
4. 본인 예약만 취소 가능
5. 시작 시간이 종료 시간보다 늦으면 예외 처리
6. 이미 지난 시간 예약 불가

## 요구 API

```http
POST /api/reservations
GET /api/members/me/reservations
DELETE /api/reservations/{id}
```

## `4주차 학습 내용 구현` 명령을 받았을 때 AI가 해야 할 일

- User, StudyRoom, Reservation 관계를 설계한다.
- 예약 생성 로직을 Service 계층에 둔다.
- 본인 예약만 취소 가능하도록 권한 검증을 넣는다.
- 시간 검증 로직을 구현한다.
- 예외 케이스를 명확히 나눈다.

## 4주차 완료 기준

- [ ] 예약 생성이 된다.
- [ ] 내 예약 목록 조회가 된다.
- [ ] 예약 취소가 된다.
- [ ] 다른 사람의 예약은 취소할 수 없다.
- [ ] 잘못된 시간 요청은 실패한다.
- [ ] 예약 도메인 로직을 설명할 수 있다.

---

# 9. 5주차 — 예약 시간 중복 검증

## 목표

단순 CRUD가 아니라 실제 비즈니스 규칙을 구현한다.

## 핵심 규칙

같은 스터디룸에 대해 기존 예약과 새 예약 시간이 겹치면 예약을 실패시킨다.

시간 겹침 조건:

```text
existing.startTime < newEndTime
AND
existing.endTime > newStartTime
```

## 구현 기능

1. 시간 겹침 검증 Repository 쿼리
2. 중복 예약 예외 처리
3. 중복 예약 실패 테스트
4. 동시성 문제 가능성 정리

## `5주차 학습 내용 구현` 명령을 받았을 때 AI가 해야 할 일

- 예약 시간 중복 검증 쿼리를 작성한다.
- JPQL 또는 Spring Data JPA 쿼리 메서드를 사용한다.
- Service에서 예약 생성 전 중복 여부를 확인한다.
- 중복이 있으면 커스텀 예외를 던진다.
- 이 로직에 필요한 테스트 케이스를 제안한다.
- 동시 요청이 들어오면 어떤 문제가 생길 수 있는지 설명한다.

## 5주차 완료 기준

- [ ] 겹치는 시간대 예약이 실패한다.
- [ ] 겹치지 않는 시간대 예약은 성공한다.
- [ ] 중복 예약 예외 응답이 내려온다.
- [ ] 시간 겹침 조건을 말로 설명할 수 있다.
- [ ] 관련 테스트가 있다.

---

# 10. 6주차 — 리뷰, 찜, 검색

## 목표

예약 플랫폼을 서비스답게 만든다.

## 구현 기능

1. 리뷰 작성
2. 리뷰 목록 조회
3. 찜하기
4. 찜 취소
5. 내가 찜한 스터디룸 조회
6. 지역/수용인원/가격 검색
7. 페이징/정렬

## 요구 API

```http
POST /api/study-rooms/{id}/reviews
GET /api/study-rooms/{id}/reviews
POST /api/study-rooms/{id}/favorites
DELETE /api/study-rooms/{id}/favorites
GET /api/members/me/favorites
GET /api/study-rooms?region=&capacity=&minPrice=&maxPrice=&page=&size=
```

## `6주차 학습 내용 구현` 명령을 받았을 때 AI가 해야 할 일

- Review, Favorite 도메인을 설계한다.
- N:M 관계는 직접 ManyToMany를 쓰지 말고 Favorite 중간 엔티티로 푼다.
- 검색 조건과 페이징을 구현한다.
- 검색 기능이 9주차 EXPLAIN/인덱스 개선 대상이 될 수 있도록 설계한다.

## 6주차 완료 기준

- [ ] 리뷰 작성/조회가 된다.
- [ ] 찜하기/찜 취소가 된다.
- [ ] 내가 찜한 목록을 조회할 수 있다.
- [ ] 검색/필터링이 된다.
- [ ] N:M을 중간 엔티티로 푸는 이유를 설명할 수 있다.

---

# 11. 7주차 — 테스트, Swagger, 공통 응답

## 목표

돌아가는 코드를 검증 가능한 포트폴리오 코드로 바꾼다.

## 구현 기능

1. JUnit 테스트
2. 예약 중복 실패 테스트
3. 권한 없는 예약 취소 실패 테스트
4. Swagger/OpenAPI 문서화
5. 공통 응답 포맷
6. 공통 에러 포맷

## `7주차 학습 내용 구현` 명령을 받았을 때 AI가 해야 할 일

- 핵심 비즈니스 로직 중심으로 테스트를 작성한다.
- 모든 기능을 억지로 테스트하지 말고 중요한 케이스를 우선한다.
- Swagger 설정을 추가한다.
- 공통 응답 포맷을 정리한다.

## 7주차 완료 기준

- [ ] 회원가입 성공 테스트가 있다.
- [ ] 중복 이메일 실패 테스트가 있다.
- [ ] 로그인 성공 테스트가 있다.
- [ ] 예약 생성 성공 테스트가 있다.
- [ ] 예약 시간 중복 실패 테스트가 있다.
- [ ] 다른 사람 예약 취소 실패 테스트가 있다.
- [ ] Swagger 문서가 열린다.

---

# 12. 8주차 — Docker, 배포, README

## 목표

취업용 산출물로 마무리한다.

## 구현 기능

1. Dockerfile 작성
2. docker-compose로 Spring + MySQL 실행
3. 환경변수 분리
4. 배포 준비
5. README 완성
6. ERD 이미지 추가
7. API 명세 정리
8. 트러블슈팅 3개 작성

## `8주차 학습 내용 구현` 명령을 받았을 때 AI가 해야 할 일

- Dockerfile과 docker-compose.yml을 작성한다.
- 로컬에서 컨테이너로 실행하는 방법을 정리한다.
- README 초안을 작성한다.
- 프로젝트 소개, 기술 스택, ERD, API, 실행 방법, 트러블슈팅, 회고 섹션을 만든다.
- 이력서에 넣을 프로젝트 설명 문장을 작성한다.

## 8주차 완료 기준

- [ ] Docker로 애플리케이션이 실행된다.
- [ ] MySQL도 컨테이너로 실행된다.
- [ ] README가 완성되어 있다.
- [ ] ERD가 있다.
- [ ] API 명세가 있다.
- [ ] 트러블슈팅 3개 이상이 있다.
- [ ] 이력서용 설명 문장이 있다.

---

# 13. 9주차 — MySQL EXPLAIN과 인덱스 개선

## 목표

검색 API의 쿼리 성능을 개선한다.

## 구현 기능

1. 스터디룸 검색 API에 인덱스 적용
2. EXPLAIN으로 실행 계획 확인
3. 인덱스 적용 전/후 비교
4. Velog 성능 개선 글 작성

## `9주차 고도화 구현` 명령을 받았을 때 AI가 해야 할 일

- 검색 쿼리에 대해 EXPLAIN을 실행하는 방법을 안내한다.
- 인덱스 후보를 제안한다.
- 인덱스 적용 전/후 비교 항목을 정리한다.
- README/Velog에 넣을 성능 개선 기록 초안을 작성한다.

## 완료 기준

- [ ] EXPLAIN 결과를 확인했다.
- [ ] 인덱스를 적용했다.
- [ ] 적용 전/후 차이를 기록했다.
- [ ] 왜 해당 인덱스를 걸었는지 설명할 수 있다.

---

# 14. 10주차 — Redis 캐싱

## 목표

반복 조회가 많은 API에 캐싱을 적용한다.

## 구현 기능

1. 인기 스터디룸 TOP 10 API
2. Redis 캐싱
3. TTL 설정
4. 캐시 무효화 전략 정리
5. 응답 시간 비교

## `10주차 고도화 구현` 명령을 받았을 때 AI가 해야 할 일

- Redis 설정을 추가한다.
- 인기 스터디룸 조회 결과를 캐싱한다.
- TTL 설정 이유를 설명한다.
- 데이터 정합성 문제가 생길 수 있는 지점을 정리한다.

## 완료 기준

- [ ] Redis가 실행된다.
- [ ] 인기 스터디룸 API가 캐싱된다.
- [ ] TTL이 적용된다.
- [ ] 캐싱 전/후 차이를 기록했다.
- [ ] 캐시 정합성 이슈를 설명할 수 있다.

---

# 15. 11주차 — k6 성능 테스트와 Connection Pool

## 목표

부하 테스트로 API 병목을 관찰한다.

## 구현 기능

1. k6 설치 및 스크립트 작성
2. 예약 조회 API 부하 테스트
3. 동시 사용자 수 변화 실험
4. HikariCP 설정 확인
5. Connection Pool size 변경 실험

## `11주차 고도화 구현` 명령을 받았을 때 AI가 해야 할 일

- k6 테스트 스크립트를 작성한다.
- 테스트 결과를 읽는 방법을 설명한다.
- HikariCP 설정 항목을 설명한다.
- pool size 변경 전/후 비교 방법을 제안한다.

## 완료 기준

- [ ] k6 테스트가 실행된다.
- [ ] 응답 시간과 실패율을 확인했다.
- [ ] Connection Pool 설정을 바꿔봤다.
- [ ] 실험 결과를 기록했다.

---

# 16. 12주차 — GitHub Actions 테스트 자동화

## 목표

push 또는 PR 시 테스트가 자동으로 실행되게 한다.

## 구현 기능

1. GitHub Actions workflow 작성
2. JUnit 테스트 자동 실행
3. 테스트 실패 시 실패 처리
4. README에 CI 배지 또는 설명 추가

## `12주차 고도화 구현` 명령을 받았을 때 AI가 해야 할 일

- GitHub Actions workflow YAML을 작성한다.
- Gradle 기반 테스트 명령을 사용한다.
- MySQL이 필요한 테스트가 있다면 test profile 또는 testcontainers 사용 가능성을 설명한다.
- README에 테스트 자동화 내용을 정리한다.

## 완료 기준

- [ ] GitHub Actions가 실행된다.
- [ ] 테스트가 자동으로 돈다.
- [ ] 실패 시 workflow가 실패한다.
- [ ] README에 CI 설명이 있다.

---

# 17. 매일 사용하는 프롬프트 템플릿

## 오늘 구현할 기능 계획

```text
오늘은 [기능명]을 구현하려고 한다.
현재 주차는 [N주차]이고, 이 주차 범위를 넘는 기술은 사용하지 말아줘.

요구사항:
- [요구사항 1]
- [요구사항 2]
- [요구사항 3]

원하는 답변:
1. 구현할 파일 목록
2. 각 파일의 책임
3. 코드 초안
4. Postman 테스트 예시
5. 내가 직접 다시 써봐야 할 부분
6. 스스로 답해야 할 질문
```

## 코드 리뷰 요청

```text
현재 코드를 리뷰해줘.

리뷰 기준:
1. 현재 주차 학습 목표에 맞는가?
2. 너무 과한 구현은 없는가?
3. Controller, Service, Repository 책임이 분리되어 있는가?
4. DTO와 Domain/Entity가 분리되어 있는가?
5. 예외 처리가 적절한가?
6. 내가 반드시 이해해야 할 부분은 무엇인가?
7. 지우고 다시 써봐야 할 코드는 무엇인가?
```

## 에러 해결 요청

```text
아래 에러가 발생했다.
현재 주차는 [N주차]다.
현재 주차 범위를 넘는 해결책은 쓰지 말고, 원인을 학습자 관점에서 설명해줘.

에러 로그:
[여기에 붙여넣기]

원하는 답변:
1. 에러 원인
2. 어디 파일을 봐야 하는지
3. 최소 수정 방법
4. 왜 이렇게 고치는지
5. 다음에 같은 에러를 피하는 방법
```

## Velog 회고 요청

```text
오늘 구현한 내용을 Velog 회고 형식으로 정리해줘.

오늘 구현한 기능:
- [기능]

막힌 점:
- [막힌 점]

해결한 방법:
- [해결 방법]

글 구조:
1. 오늘의 목표
2. 구현한 기능
3. 코드 구조
4. 막힌 점과 해결
5. 배운 점
6. 다음 목표

문체는 너무 거창하지 않고, 실제 공부 기록처럼 써줘.
```

---

# 18. 최종 포트폴리오 README 구조

8주차에 README는 아래 구조를 목표로 한다.

```markdown
# StudyRoom Reservation API

## 1. 프로젝트 소개
## 2. 개발 동기
## 3. 기술 스택
## 4. 주요 기능
## 5. 아키텍처
## 6. ERD
## 7. API 명세
## 8. 실행 방법
## 9. 주요 구현 내용
## 10. 테스트
## 11. 트러블슈팅
## 12. 성능 개선
## 13. 회고
```

---

# 19. 최종 이력서 문장 예시

8주 기본 과정 완료 후:

```text
Spring Boot 기반 스터디룸 예약 플랫폼 백엔드를 개발했습니다.
JWT 인증/인가, JPA 기반 도메인 설계, 예약 시간 중복 검증,
공통 예외 처리, JUnit 테스트, Docker 기반 실행 환경을 구현했습니다.
```

12주 고도화 완료 후:

```text
Spring Boot 기반 예약 플랫폼 백엔드를 구현하고,
MySQL EXPLAIN을 통한 검색 쿼리 개선, Redis 캐싱,
k6 부하 테스트, GitHub Actions 테스트 자동화를 적용했습니다.
단순 CRUD를 넘어 성능과 운영 관점의 개선 과정을 Velog에 정리했습니다.
```

---

# 20. 학습 완료 기준

이 커리큘럼은 코드를 많이 작성하는 것이 목표가 아니다.
아래를 설명할 수 있으면 성공이다.

- Controller, Service, Repository의 역할
- DTO와 Entity/Domain을 분리하는 이유
- JPA 연관관계 설계 이유
- `@Transactional`이 필요한 이유
- JWT 인증 흐름
- 예약 시간 중복 검증 조건
- 예외 처리 흐름
- 테스트 코드가 검증하는 내용
- Docker로 실행하는 방법
- EXPLAIN 결과를 보고 인덱스를 고민한 과정
- Redis 캐싱의 장점과 정합성 문제
- k6 성능 테스트 결과 해석
- GitHub Actions 테스트 자동화 흐름

