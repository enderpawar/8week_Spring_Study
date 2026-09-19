# Week A·B 재복습 오답 기록 — 2026-09-19

> 한 달 휴지기 후 Week A부터 재수강한 세션의 기록이다.
> 학습자가 실제로 답한 내용만 기록하며, 재확인 전에는 통과로 표시하지 않는다.

## 현재 진행 위치

- Week A D1 요청→응답: 복습 완료
- Week A D2 DTO와 Domain 분리: 복습 완료
- Week A D3 Bean Validation·전역 오류 처리: 복습 완료
- Week A D4 Service/Repository 책임 분리: 복습 완료
- Week A D5 IoC·DI·Singleton 무상태: 복습 완료
- Week A 누적 연결 확인: 통과
- 다음 시작점: Week B D1 Flyway·DB 제약

## 실제 오답

| 번호 | 질문 | 최초 답변 요지 | 교정 기준 | 상태 |
|---|---|---|---|---|
| 1 | ID로 기존 예약을 실제 조회하는 책임은 어느 계층인가? | Service — 비즈니스 로직과 관련된 내용이므로 Service가 처리 | 실제 저장·조회는 Repository의 책임이다. Service는 조회를 포함한 업무 흐름을 조율한다. | 교정 후 통과 |
| 2 | 조회 결과가 없을 때 `ReservationNotFoundException`을 발생시키는 판단은 어느 계층의 책임인가? | Repository — DB에서 예약을 조회하는 인터페이스의 역할이므로 Repository가 처리 | Repository는 `Optional`로 있음/없음만 반환한다. 없음에 비즈니스 의미를 부여하여 도메인 예외로 바꾸는 것은 Service의 책임이다. | 교정 후 통과 |

## 교정 후 재확인

- 2026-09-19: `ID로 실제 조회 = Repository`, `없음을 ReservationNotFoundException으로 변환 = Service`로 구분해 답했다.
- 경계 정리: Repository는 조회 결과를 `Optional.empty()`로 보고하고, Service가 그 결과에 비즈니스 의미를 부여해 예외를 발생시킨다.

## Week A 마무리 확인

- 빈 `roomName` 요청은 `@NotBlank` 제약을 `@Valid`가 실행하는 단계에서 실패하며, 전역 예외 처리기가 400 응답으로 변환하고 Service는 호출되지 않는다고 답했다.
- Singleton Bean은 여러 요청이 같은 인스턴스를 공유하므로 요청별 값을 인스턴스 필드에 저장하면 값이 섞일 수 있다고 설명했다.
