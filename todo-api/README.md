# Todo API

Spring Boot 1주차 학습용 InMemory Todo API입니다.

DB, JPA, Security, JWT는 아직 사용하지 않고 `Map<Long, Todo>` 저장소로 Controller, Service, Repository, DTO, Domain, Exception 계층을 익히는 데 집중합니다.

## 기술 스택

- Java 17
- Spring Boot 3.5.3
- Gradle
- Spring Web
- Spring Validation
- Lombok
- JUnit 5, MockMvc

## 파일 구조

```text
src/main/java/com/example/todo
├── controller
│   ├── HelloController.java
│   └── TodoController.java
├── domain
│   └── Todo.java
├── dto
│   ├── ErrorResponse.java
│   ├── HelloResponse.java
│   ├── TodoCreateRequest.java
│   ├── TodoResponse.java
│   └── TodoUpdateRequest.java
├── exception
│   ├── GlobalExceptionHandler.java
│   └── TodoNotFoundException.java
├── repository
│   ├── InMemoryTodoRepository.java
│   └── TodoRepository.java
└── service
    └── TodoService.java
```

## 실행

```bash
./gradlew bootRun
```

Windows PowerShell에서는 다음 명령을 사용합니다.

```powershell
.\gradlew.bat bootRun
```

## 테스트

```bash
./gradlew test
```

Windows PowerShell에서는 다음 명령을 사용합니다.

```powershell
.\gradlew.bat test
```

## API

### Hello

```http
GET /api/hello
```

응답 예시:

```json
{
  "message": "Hello Todo API"
}
```

### Todo 생성

```http
POST /api/todos
Content-Type: application/json

{
  "title": "Spring Controller 공부",
  "description": "요청과 응답 흐름 이해하기"
}
```

### Todo 목록 조회

```http
GET /api/todos
```

### Todo 단건 조회

```http
GET /api/todos/1
```

### Todo 수정

```http
PATCH /api/todos/1
Content-Type: application/json

{
  "title": "Spring Service 공부",
  "description": "비즈니스 흐름 분리하기"
}
```

### Todo 완료 처리

```http
PATCH /api/todos/1/complete
```

### Todo 삭제

```http
DELETE /api/todos/1
```

## 에러 응답 예시

존재하지 않는 Todo:

```json
{
  "timestamp": "2026-07-09T16:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Todo를 찾을 수 없습니다. id=999",
  "path": "/api/todos/999",
  "errors": {}
}
```

Validation 실패:

```json
{
  "timestamp": "2026-07-09T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "요청 값이 올바르지 않습니다.",
  "path": "/api/todos",
  "errors": {
    "title": "제목은 필수입니다."
  }
}
```

## 직접 다시 작성해볼 파일 순서

1. `TodoCreateRequest`
2. `TodoUpdateRequest`
3. `TodoResponse`
4. `Todo`
5. `TodoRepository`
6. `InMemoryTodoRepository`
7. `TodoService`
8. `TodoController`
9. `TodoNotFoundException`
10. `GlobalExceptionHandler`

## 스스로 답해볼 질문

1. Controller는 왜 필요한가?
2. Service는 왜 필요한가?
3. Repository는 왜 필요한가?
4. DTO를 Domain 객체와 분리하는 이유는 무엇인가?
5. Todo 상태 변경을 `Todo` 내부 메서드로 처리하면 어떤 장점이 있는가?
6. Controller에서 Repository를 직접 호출하면 어떤 문제가 생길 수 있는가?
7. `TodoNotFoundException`은 어느 계층에서 발생시키는 것이 자연스러운가?
8. `GlobalExceptionHandler`를 사용하면 Controller 코드가 어떻게 달라지는가?
9. Validation 실패와 존재하지 않는 Todo 요청은 HTTP 상태 코드가 왜 다른가?
10. 다음 주차에 JPA로 바꾸면 어떤 파일이 가장 많이 바뀔까?
