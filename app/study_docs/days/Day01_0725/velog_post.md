# [백엔드 기본기 Day1] 컨트롤러 하나로 들여다본 요청→응답 왕복

> 새 5주 로드맵의 첫날. `app/`을 빈 스켈레톤으로 새로 시작하면서, 컨트롤러 하나로 "요청이 어떻게 응답이 되는가"를 손으로 짚어봤다. 별거 아닌 것 같았는데, 상태코드를 파고들다 보니 하루치 분량이 나왔다.

## 1. 완성예제 — `/hello` 엔드포인트

가장 단순한 형태부터 타이핑했다.

```java
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, StudyRoom!";
    }
}
```

`curl.exe -i http://localhost:8080/hello`로 호출하니 `200 OK`가 나왔다. 예상은 했지만, "왜 200인지"는 막상 설명하려니 막혔다.

## 2. "왜 200이 나왔지?" — 첫 오답과 교정

질문을 받았을 때 처음엔 **"HTTP 요청 반환 형식이 DTO로 미리 저장되어있어서 그런가?"** 라고 답했다. 완전히 틀린 방향이었다 — DTO(응답 데이터 형태)와 상태코드(응답 메타데이터)는 애초에 관련 없는 개념이었다.

교정된 답은 이거였다.

> HTTP 상태코드는 5개 계열이다 — 1xx(정보) / 2xx(성공) / 3xx(리다이렉션) / 4xx(클라이언트 오류) / 5xx(서버 오류). 컴퓨터 네트워크 시간에 배운 400(Bad Request)·404(Not Found)는 4xx 계열이고, 200은 2xx 계열의 대표다. **컨트롤러 메서드가 예외 없이 정상적으로 반환하면, Spring은 "특별히 잘못됐다는 신호가 없다 = 성공"으로 간주해서 기본값 200을 붙인다.** 400/404가 뜨려면 명시적으로 "이상하다"는 신호가 필요하다.

이 신호를 눈으로 확인하려고 등록 안 한 경로를 호출해봤다.

```
curl.exe -i http://localhost:8080/nope
→ 404 Not Found
```

핸들러가 아예 매핑되어 있지 않으니 Spring이 자동으로 404를 반환했다. "기본 성공 vs 명시적 실패 신호"라는 대조가 이때 확실해졌다.

이 항목은 정답을 본 직후 바로 재설명한 거라 진짜 인출인지 확신이 안 서서, 복습큐에 **+1일 재시험**으로 등록해뒀다.

## 3. 빈칸 예제 — 상태코드를 직접 명시하기

이번엔 200을 자동으로 받는 대신, `ResponseEntity`로 직접 상태코드를 명시해봤다.

```java
@GetMapping("/health")
public ResponseEntity<String> health() {
    return ResponseEntity
            .status(HttpStatus.OK)
            .body("OK");
}
```

`HttpStatus.OK`를 채우는 게 빈칸이었는데, 400=`BAD_REQUEST`, 404=`NOT_FOUND`처럼 이름 있는 상수가 있을 거란 생각으로 IDE 자동완성에서 `HttpStatus.`까지 치고 찾아냈다.

여기서 곁가지 질문 하나가 나왔다 — **"그냥 `OK`만 써도 되는 이유는?"** IDE가 `import static org.springframework.http.HttpStatus.OK;`를 자동으로 넣어줘서 그런 거였다. static import는 클래스 이름 없이 static 멤버를 바로 쓰게 해주는 Java 문법일 뿐, `HttpStatus.OK`와 완전히 같은 걸 가리킨다.

## 4. 독립 변형 — `/bye` 201 Created, 그리고 컴파일 에러 4개

혼자 힌트 없이 `/bye` 엔드포인트를 만들어서 201을 반환하라는 과제였는데, 작성한 코드가 컴파일부터 안 됐다. 코드 리뷰를 받고서야 이유를 하나씩 알았다.

1. **미완성 구문** — 자동완성 탐색하다 지우지 않은 `HttpStatus.` 한 줄이 그대로 남아있었다.
2. **import 누락** — `ResponseEntity`는 import했는데 `HttpStatus`는 빠져 있었다.
3. **세미콜론 누락** — `.body("OK")` 뒤에 `;`이 없었다.
4. **메서드 시그니처 중복** — `health()`라는 이름을 두 메서드에 똑같이 써서 "이미 정의된 메서드"라는 에러가 났다. `@GetMapping` 경로가 다르다고 메서드 이름까지 같아도 되는 게 아니었다 — **컴파일러는 애노테이션을 안 보고 이름+파라미터만 보고 중복을 판단한다.**

네 개를 다 고치고 나서야 `/bye` 호출 시 `201 Created`가 제대로 나왔다.

## 5. bootRun은 코드 수정할 때마다 다시 돌려야 한다

디버깅하면서 자연스럽게 알게 된 것 — **코드를 고쳐도 실행 중인 애플리케이션에는 반영되지 않는다.** 실행 중인 JVM 프로세스는 이미 로드된 클래스를 그대로 들고 있기 때문에, `.java` 파일을 수정했으면 `./gradlew bootRun`을 다시 돌려야 반영된다. (참고로 `spring-boot-devtools`를 쓰면 저장 시 자동 재시작되는 기능도 있다는데, 이건 오늘 범위 밖이라 존재만 알아둔다.)

## 정리하며

오늘의 핵심은 **"응답은 body와 상태코드로 이뤄진 계약이고, 상태코드는 기본값이거나 개발자가 의도적으로 선택하는 값"** 이라는 것. 별거 아닌 컨트롤러 하나였는데, 상태코드 계열 개념·ResponseEntity·static import·메서드 시그니처 컴파일 규칙까지 얽혀서 예상보다 알차게 하루가 채워졌다.

---
#Spring #SpringBoot #Backend #TIL #백엔드기본기로드맵 #HTTP상태코드 #ResponseEntity

오늘 공부한 소스코드: [8week_Spring_Study/app](https://github.com/enderpawar/8week_Spring_Study/tree/master/app)
