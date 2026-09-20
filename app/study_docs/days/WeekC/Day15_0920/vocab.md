# Day15 (9/20, Week C D1) 용어

주제: 트랜잭션 경계 · commit · rollback · `readOnly`

| 용어 | 한줄뜻 | 오늘 코드/관찰 |
|---|---|---|
| 트랜잭션 경계 | 여러 DB 작업을 전부 성공하거나 전부 취소되는 하나의 작업 단위로 묶는 범위 | `ReservationService.cancel()`의 조회 → 상태 변경 → 반영 전체에 `@Transactional` 적용 |
| commit | 트랜잭션에서 수행한 변경을 최종 확정하는 것 | `cancel()`이 정상 반환한 테스트에서 `UPDATE` 후 재조회 값이 유지됨 |
| rollback | commit되지 않은 트랜잭션 변경을 취소하는 것 | `RuntimeException`이 경계 밖으로 전달된 뒤 재조회하자 기존 `confirmed=true`, `cancelReason=null` 유지 |
| flush | 영속성 컨텍스트의 변경을 SQL로 DB에 보내는 것. commit과 같지 않다 | 강제 `flush()`로 `UPDATE`가 출력된 뒤에도 예외로 rollback됨 |
| `readOnly = true` | 조회 전용 의도를 표현하고 JPA 구현체가 최적화에 활용할 수 있는 힌트 | 쓰기 권한을 제거하거나 모든 `INSERT`·`UPDATE`를 보장해서 차단하는 기능은 아님 |

## 실제 서비스 경계

```java
@Transactional
public Reservation cancel(Long id, String cancelReason) {
    Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new ReservationNotFoundException(id));
    reservation.cancel(cancelReason);
    return reservation;
}
```

조회된 Entity는 트랜잭션 안에서 영속 상태이므로 별도 `save()` 없이 변경 감지가 `UPDATE`를 만든다.
