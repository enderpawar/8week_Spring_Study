# Day01 (7/25, Week A D1) 단어장 — 요청→응답 왕복

형식: **용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어**

| 용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어 |
|---|---|---|---|---|
| HTTP 상태코드 5계열 | 1xx정보/2xx성공/3xx리다이렉션/4xx클라이언트오류/5xx서버오류 | 네트워크 시간의 400·404가 4xx 계열 | `HttpStatus.OK`, `HttpStatus.CREATED` | ResponseEntity, @ResponseStatus |
| 기본 응답 상태코드 | 컨트롤러가 예외 없이 정상 반환하면 Spring이 자동으로 200 부여 | "이상신호 없음 = 성공"이라는 암묵적 계약 | `return "Hello";` → 200 | 명시적 상태코드 |
| 404 자동 반환 | 매핑된 핸들러가 없는 경로 요청 시 Spring이 자동으로 404 | 라우팅 테이블에 매칭 실패 | `/nope` 호출 → 404 | DispatcherServlet |
| ResponseEntity | 응답 body + 상태코드를 함께 제어하는 래퍼 타입 | HTTP 응답 = 상태줄 + 헤더 + 바디 | `ResponseEntity.status(HttpStatus.OK).body("OK")` | HttpStatus |
| static import | 클래스 이름 없이 static 멤버를 바로 쓰게 해주는 Java 문법 | 이름공간(namespace) 축약 | `import static ...HttpStatus.OK;` → `OK` | import |
| 메서드 시그니처 중복 | 같은 클래스에 이름+파라미터가 동일한 메서드 2개 → 컴파일 에러 | 컴파일러는 애노테이션이 아니라 시그니처만 봄 | `health()` 2개 선언 시 에러 | 오버로딩 |
| bootRun 재시작 | 코드 수정 후 반영하려면 애플리케이션 재시작 필요(기본 동작) | 실행 중인 JVM 프로세스는 이미 로드된 클래스를 안 바꿈 | `./gradlew bootRun` 재실행 | DevTools(핫리로드, 추후) |
