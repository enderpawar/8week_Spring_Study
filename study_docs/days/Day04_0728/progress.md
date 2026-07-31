# Day04 (7/28, Week A D4) 진행상황 — 중단 지점

> 이어서 할 때 이 파일을 먼저 읽을 것. 커밋 `d6320e2`까지 반영됨(push 완료).

## 오늘 주제
- Full: Service/Repository 책임 분리
- Light: 인터페이스 Repository

## 한 것
- `ReservationRepository` 인터페이스 + `InMemoryReservationRepository` 구현체 생성
- `ReservationService` 생성(생성자 주입으로 `ReservationRepository` 사용)
- `ReservationController.reserve()`가 `reservationService.reserve(...)`를 호출하도록 변경
- `./gradlew test` green 확인, 커밋+push 완료

## 안 한 것 (다음 세션에서 이어갈 것)
- `ReservationController.cancel()`은 아직 옛날 방식 그대로(Controller가 직접 `new Reservation(...)` + `canceled()` 호출). Service로 옮기는 건 **독립 변형(scaffold fading 3단계)**으로 학습자가 오늘 만든 `reserve()` 패턴을 보고 스스로 하는 게 목표.
  - 힌트만: `ReservationService`에 `cancel(String roomName, String requesterName)` 메서드 추가 필요. 단, 지금 `Repository`엔 `save()`/`findAll()`만 있고 **ID로 조회**하는 메서드가 없어서, "취소"를 제대로 하려면 저장된 예약을 찾아 상태를 바꾸는 문제에 부딪힐 것 — 이게 다음 학습 포인트(식별자 필요성)로 자연스럽게 이어짐. 굳이 지금 먼저 알려주지 말고 부딪히게 둘 것.
- 인출 퀴즈(quiz.md) 아직 안 만듦
- vocab.md 아직 안 만듦
- explain-log.md 아직 안 씀 — 오늘 겪은 좋은 소재:
  - `ReservationController` 생성자를 `ClassName(Type param) { }` 형태(Kotlin 식)로 잘못 씀 → 자바는 필드 선언 + 별도 생성자가 필요하다는 것
  - `this.reservationService = reservationService;`에서 매개변수명이 `reservationSevice`(오타)였던 탓에 우변이 필드 자기 자신을 가리켜 "might not have been initialized" 에러 발생 — **오타가 논리 버그로 이어진 사례**
  - 패키지 선언(`com.example.studyroom.studyroom.service`)이 실제 폴더 경로와 안 맞으면 다른 파일에서 import 자체가 실패한다는 것 재확인(Day03에서 배운 것의 재적용)
- 복습큐.md 갱신 안 함 — 위 개념들 +2일로 등록 필요

## 환경 이슈 (미해결)
- IntelliJ가 `app/`을 Gradle 프로젝트로 import 안 한 상태(`8week_Spring_Study/` 루트를 통째로 연 것으로 보임 — `.idea/gradle.xml` 없음). 그래서 실시간 문법 검사가 전혀 안 됐고, 오늘 오타들을 전부 `gradlew compileJava` 실행 결과로만 잡았음.
  - **해결법**: IntelliJ에서 `File → Close Project` → `File → Open...` → `app/` 폴더(=`build.gradle` 있는 폴더) 선택 → Gradle sync 대기(JDK는 `ms-17` 선택). 이걸 하면 다음부터 타이핑 중 실시간으로 에러가 보임.

## 다음 세션 시작 방법
1. 위 IntelliJ 재설정부터 하기(안 했다면)
2. `cancel()`을 Service로 옮기는 독립 변형 진행
3. 오늘 배운 개념으로 인출 퀴즈 1~2문항
4. `vocab.md`, `quiz.md`, `explain-log.md`, `복습큐.md` 채우기
5. 끝나면 커밋+push (Day4 완결)
