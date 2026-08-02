# Day07 (2026-08-02 완료, Week A D7) 예측·검증 로그

> 상태: **완료**. Day04에서 남긴 예측과 기술부채를 코드·단위 테스트·MockMvc 테스트로 검증했다.

## 에이전트가 구현하고 검증한 사실

| 확인할 동작 | 코드의 결정 | 자동 검증 |
|---|---|---|
| 취소 후 저장소 원소 수 | 신규 ID만 `add`, 기존 ID는 `set` | `cancelUpdatesExistingReservationWithoutAddingDuplicate()` |
| 없는 번호 취소 | `Optional.empty()`를 `ReservationNotFoundException`으로 변환 | `cancelThrowsDomainExceptionWhenReservationDoesNotExist()` |
| 없는 예약의 오류 응답 | 예외 처리기가 404와 안전한 메시지로 변환 | `notFoundExceptionBecomes404Response()` |
| 예상 밖 예외의 내부 정보 | 상세 원인은 로그, HTTP 본문은 일반 메시지 | `unexpectedExceptionDoesNotExposeInternalMessage()` |
| 유효한 예약 요청 | Controller가 요청을 받아 성공 문자열 반환 | `reserveReturnsSuccessResponseForValidBody()` |
| 빈 DTO 필드 | `@Valid`가 Controller 실행 전에 거절 | `reserveReturns400ForBlankBodyFields()` |
| 없는 예약 HTTP 요청 | 도메인 예외를 전역 처리기가 404로 변환 | `cancelReturns404WhenReservationDoesNotExist()` |
| `id=0` 취소 요청 | `@Positive` 메서드 검증으로 Service 전에 거절 | `cancelReturns400WhenIdIsNotPositive()` |

기존 `save()`가 같은 객체 참조를 다시 `add()`하는 문제는 Day04에서 코드로 발견했지만 당시 목록 크기를 자동 검증하지 않았다. Day07에서는 동작을 고친 뒤 `findAll().size() == 1`을 테스트로 고정했다. 수정 전 실패 테스트를 실행한 기록은 없으므로 red 단계가 있었다고 쓰지 않는다.

## 선택 추가 설명

아래 영역은 완료 조건이 아니라 다음 복습에서 설명을 더 다듬기 위한 선택 기록이다.

| 개념 | 내 예측 | 테스트 실행 결과 | 차이 설명 |
|---|---|---|---|
| 기존 ID의 예약에 `store.add()`를 다시 호출하면 생기는 일 | [직접 작성] | [직접 작성] | [직접 작성] |
| 없는 예약을 `null`로 반환한 뒤 `cancel()`을 호출하면 생기는 일 | [직접 작성] | [직접 작성] | [직접 작성] |

## CS 연결

- 메모리 리스트의 “추가/교체” 구분은 DB에서 PK로 INSERT 대상과 UPDATE 대상을 구분하는 문제와 연결된다.
- `Optional`은 값 부재를 반환형에 드러낸다. 숨은 `null` 상태를 호출자에게 명시적으로 처리하게 한다.
- 내부 예외와 외부 오류 응답의 분리는 정보 은닉과 시스템 경계의 문제다.
- MockMvc 검증은 Controller 메서드만 호출하는 단위 테스트와 달리 URL 매핑·검증·예외 리졸버·JSON 변환을 함께 지난다.
