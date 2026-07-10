# 1주차 코드 해부 가이드 — todo-api

> 목적: 이미 구현된 InMemory Todo API를 어떤 순서로, 어떤 질문을 던지며 뜯어볼지 정리한 가이드.
> Study_plan.md의 "1주차 다시 쓰기 순서"·"스스로 답해야 할 질문"과 함께 사용한다.

---

## 1. 요청이 흘러가는 순서대로 읽기

한 번에 전체 파일을 읽지 말고, `POST /api/todos` 요청 하나가 들어왔을 때 코드가 실행되는 순서를 따라간다.

1. **`TodoController.java:29-33`** — `@Valid @RequestBody`로 JSON을 `TodoCreateRequest`로 바꾸고, `todoService.create()`만 호출한다. Controller는 검증된 요청을 넘기고 결과를 그대로 반환할 뿐, 로직이 하나도 없다는 것을 확인한다.
2. **`TodoService.java:20-24`** — `new Todo(...)`로 도메인 객체를 만들고 → repository에 저장 → `TodoResponse.from()`으로 변환한다. "HTTP도 모르고 저장 방식도 모르는" 계층이라는 게 핵심이다.
3. **`TodoRepository.java`** (인터페이스) vs **`InMemoryTodoRepository.java`** — Service는 인터페이스만 알고, 구현체는 `ConcurrentHashMap` + `AtomicLong`을 쓴다. 왜 `HashMap`이 아니라 `ConcurrentHashMap`인지, `assignId`가 왜 `AtomicLong.getAndIncrement()`를 쓰는지 생각해본다 (동시 요청 대비).
4. **`Todo.java`** — `update()`, `complete()` 메서드가 도메인 객체 안에 있다 (`Todo.java:33-42`). Service나 Controller가 `todo.setTitle(...)` 식으로 필드를 직접 건드리지 않는 이유를 스스로 설명해본다.
5. 실패 케이스: **`TodoNotFoundException.java`** → **`GlobalExceptionHandler.java:19-23`**. `TodoService.getTodo()` (`TodoService.java:57-60`)에서 예외를 던지면 어떻게 JSON 에러로 바뀌는지 끝까지 추적해본다.

---

## 2. DTO 3종을 나란히 비교하기

`TodoCreateRequest`, `TodoResponse`, `ErrorResponse`를 열어놓고 비교한다.

- `TodoCreateRequest`는 `record` + `@NotBlank`/`@Size`로 **입력 검증**만 책임진다.
- `TodoResponse`는 `Todo.from(todo)` 정적 메서드로 **도메인 → 응답 변환**을 담당한다 (`TodoResponse.java:16-25`).
- `ErrorResponse`는 성공/실패와 무관하게 **일정한 JSON 모양**을 강제한다.

**질문**: 만약 `TodoResponse` 없이 `Todo` 도메인 객체를 그대로 `@ResponseBody`로 반환하면 어떤 문제가 생길까? (Study_plan.md 금지사항에 "Entity/Domain을 응답으로 직접 노출하지 않는다"가 왜 있는지와 연결된다.)

---

## 3. 직접 실행하면서 확인하기

1. `./gradlew bootRun`으로 서버를 띄운다.
2. Postman/curl로 순서대로 테스트한다.
   - `POST /api/todos` (성공)
   - `POST /api/todos` (title 빈 문자열 → 400 확인)
   - `GET /api/todos/{존재하지 않는 id}` (404 + `ErrorResponse` 모양 확인)
   - `PATCH /api/todos/{id}/complete`
   - `DELETE /api/todos/{id}`
3. `TodoApiIntegrationTests.java`를 열어서 방금 손으로 한 테스트가 어떤 테스트 메서드와 대응되는지 매칭해본다 (예: `validationError()` 테스트가 title 빈 값 케이스).

---

## 4. 지우고 다시 써보기

Study_plan.md의 순서를 그대로 따른다.

1. `TodoCreateRequest`
2. `TodoUpdateRequest`
3. `TodoResponse`
4. `Todo` 도메인 객체
5. `InMemoryTodoRepository`
6. `TodoService`의 create/update/complete 메서드
7. `TodoController`의 create/update/complete 메서드
8. `TodoNotFoundException`
9. `GlobalExceptionHandler`

파일 내용을 지우고 시그니처만 남긴 뒤 직접 채워본다. 막히면 컴파일 에러 메시지를 그대로 붙여넣고 "1주차 범위 안에서" 힌트를 요청한다.

---

## 5. 스스로 답해야 할 질문 (실제 코드 기준)

- `TodoController`가 `TodoRepository`를 직접 호출하면 안 되는 이유를 `TodoService.getTodo()` 코드를 근거로 설명할 수 있는가?
- `Todo.assignId()`가 `if (this.id != null) throw ...`로 방어하는 이유는? (`Todo.java:26-29`)
- `InMemoryTodoRepository`가 `ConcurrentHashMap`을 쓰는데도 완벽하게 동시성 안전하지 않은 지점이 있을까? (`save()`의 id 할당과 `store.put` 사이)
- `GlobalExceptionHandler`가 없다면 `TodoNotFoundException`이 터졌을 때 클라이언트는 어떤 응답을 받을까?
- 2주차에 JPA로 바꾸면 `InMemoryTodoRepository`는 사라지고 `TodoRepository`는 `JpaRepository`를 상속하게 된다. `TodoService`의 코드는 얼마나 바뀔까? (거의 안 바뀐다는 게 인터페이스 분리의 핵심 — 미리 예측해본다.)

---

## 6. 막히면

Study_plan.md의 명령어를 사용한다.

- `1주차 코드 해부`
- `1주차 코드 리뷰`
- `면접 질문 생성`
