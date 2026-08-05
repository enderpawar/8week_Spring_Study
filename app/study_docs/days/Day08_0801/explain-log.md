# Day08 (8/1 계획 → 8/5 완료, Week B D1) 예측→실행→차이 기록

주제: Flyway `V1__init`으로 `reservation` 스키마 정의

## 실험 1 — 첫 마이그레이션 기동

**예측(학습자):** (B) 기동 실패, Flyway가 SQL 오류 보고

**실행:**
```
Database: jdbc:h2:file:./data/studyroom (H2 2.3)
Schema history table does not exist yet
Creating Schema History table ...
Current version of schema "PUBLIC": << Empty Schema >>
Migrating schema "PUBLIC" to version "1 - init"
ERROR: Migration ... to version "1 - init" failed!

Error Code : 42000
Message    : Syntax error in SQL statement
  "... requester_name VARCHAR(50) NOT NULL, [*]#Java는 CamelCase ..."
```

**차이:** 예측 적중. 원인은 `#` 주석 — 표준 SQL 주석은 `--`이고 `#`은 MySQL 방언이다. `MODE=MySQL`이 걸려 있었는데도 막혔는데, 호환 모드는 타입·함수·NULL 처리 같은 **동작 호환성 일부**만 맞추고 파서는 H2 파서 그대로이기 때문이다. 오류 메시지의 `[*]`가 파서가 막힌 정확한 위치를 가리킨다.

**부수 관찰:** 실패했는데도 장부에는 기록이 남았다.
```
installed_rank | version | description | success
-1             | null    | 장부 생성    | TRUE
1              | 1       | init        | FALSE
```
`success = FALSE`가 남아 있으면 다음 기동이 막힌다. 운영에서는 `flyway repair`, 개발에서는 DB 파일 삭제로 처리한다. 이번에는 데이터가 없어 파일을 지웠다.

## 실험 2 — `NOT NULL`이 무엇을 막는가

앱을 거치지 않고 H2 콘솔에서 직접 INSERT. `@NotBlank`는 이 경로를 볼 수 없다.

**예측(학습자):** ① `NULL` → 거부(B) ② `''` → 막힌다

**실행:**
```sql
INSERT INTO reservation (room_name, requester_name)
VALUES (NULL, '이진우');
-- JdbcSQLIntegrityConstraintViolationException:
-- NULL not allowed for column "ROOM_NAME"

INSERT INTO reservation (room_name, requester_name)
VALUES ('', '이진우');
-- (Update count: 1)

SELECT id, '['||room_name||']', requester_name, confirmed FROM reservation;
-- 2 | [] | 이진우 | FALSE
-- CHAR_LENGTH(room_name) = 0
```

**차이:** ①은 맞고 ②는 틀렸다. `NOT NULL`이 검사하는 것은 "값이 있는가" 하나뿐이고, `''`는 **값이 없는 게 아니라 길이 0인 값이 있는 것**이다. 따라서 `@NotBlank`와 `NOT NULL`은 서로 못 막는 구멍이 다르다.

| 값 | `@NotBlank` | `NOT NULL` |
|---|---|---|
| `null` | 막음 | 막음 |
| `""` / `"   "` | 막음 | 통과 |
| DB 직접 접근 | 못 봄 | 막음 |

중복이 아니라 층이 다르다. 앱 검증은 UX(친절한 400), DB 제약은 무결성(모든 경로 차단). `''`까지 DB에서 막으려면 `CHECK` 제약이 필요하다(→ 기술부채 등록).

**부수 관찰:** 두 번째 행의 `id`가 1이 아니라 **2**였다. 실패한 첫 INSERT가 번호를 소비하고 사라졌다. 번호를 되돌리려면 채번 구간을 직렬화해야 해서 동시 INSERT가 전부 대기하게 되므로, `AUTO_INCREMENT`는 "빠짐없이 연속"이 아니라 "겹치지 않음"만 보장한다. 또 `confirmed`를 INSERT에 넣지 않았는데 `DEFAULT FALSE`가 채워졌다.

## 실험 3 — 체크섬 검증

이미 성공적으로 적용된 `V1__init.sql` 끝에 **주석 한 줄만** 추가하고 재기동.

**예측(학습자):** (A) 주석은 SQL 동작에 영향이 없으니 통과

**실행:**
```
FlywayValidateException:
  Validate failed: Migrations have failed validation
  Migration checksum mismatch for migration version 1

10 tests completed, 6 failed
```

**차이:** 예측 빗나감. Flyway는 SQL을 해석해 비교하지 않고 **파일 내용 전체의 체크섬**을 장부값과 대조한다. 해시는 입력이 조금만 달라도 완전히 다른 값이 나오므로 "주석이라 의미 없음" 같은 판단 자체를 하지 않는다.

깨진 테스트 6개 중 `reservationServiceBeanIsSingleton`, `reserveReturns400ForBlankBodyFields`처럼 SQL과 무관한 것들이 포함됐다. Flyway 실패로 **Spring 컨텍스트 자체가 뜨지 못해** 그 위의 테스트가 전부 함께 죽은 것이다.

**의미:** 이 융통성 없음이 기능이다. 주석을 봐주기 시작하면 공백은, 컬럼 순서는, 길이 변경은 어디까지 봐줄지의 문제가 되고, "내 DB와 서버 DB가 같은 상태"라는 보장이 무너진다. 적용된 마이그레이션은 불변이며, 바꾸려면 `V2`를 새로 쌓는다 — push한 커밋을 rebase하지 않는 것과 같은 규칙이다.

**원복 후 재실행:** BUILD SUCCESSFUL, 테스트 전원 green.

## 실험 4 — `final`의 작용 범위 (오답 교정용)

**예측(학습자):** (A) 컴파일 에러

**실행:** 앱을 띄우지 않은 상태에서 H2 콘솔로만 실행
```sql
INSERT INTO reservation (room_name, requester_name)
VALUES ('A101','jinwoo');
-- 3 | A101

UPDATE reservation SET room_name='B202';
-- 3 | B202
```

**차이:** 예측 빗나감. 그냥 바뀌었다. `Reservation.java`는 컴파일도 실행도 되지 않았으므로 `final`이 개입할 지점이 없었다. `final`은 자바 컴파일러가 **JVM 메모리 안 변수의 재대입**만 검사하는 규칙이고, 디스크에 저장된 행은 검사 대상이 아니다. DB에서 "한 번 쓰면 못 바꿈"을 강제하려면 트리거·권한 같은 별개의 DB 장치가 필요하며 이 트랙 범위 밖이다.

## 최종 상태 확인

```
TABLE_NAME
RESERVATION                 ← 따옴표 없이 만들어 대문자로 접힘
flyway_schema_history       ← Flyway가 따옴표로 감싸 소문자 유지

version | description | type  | success
null    | 장부 생성    | TABLE | TRUE
1       | init        | SQL   | TRUE

COLUMN_NAME    | DATA_TYPE         | LEN | NULL? | DEFAULT
ID             | BIGINT            |     | NO    |
ROOM_NAME      | CHARACTER VARYING | 100 | NO    |
REQUESTER_NAME | CHARACTER VARYING | 50  | NO    |
CONFIRMED      | BOOLEAN           |     | NO    | FALSE
```

한 DB 안에 대문자 테이블과 소문자 테이블이 공존하는 이유가 식별자 접힘이다. 실제로 조회 중 두 번 걸렸다(`FLYWAY_SCHEMA_HISTORY not found (candidates are: "flyway_schema_history")`, `table_name='reservation'` → 0행).

실험 데이터는 모두 삭제해 `reservation`은 0행 상태로 남겼다.

## 오늘 막힌 지점 (진행 기록)

- `org.flywaydb:flyway-database-h2`를 추가하라는 지시를 따랐으나 **존재하지 않는 아티팩트**였다. Flyway 10에서 DB별 모듈이 분리된 것은 맞지만 전부는 아니고, H2·HSQLDB 등은 `flyway-core`에 남아 있다(jar 안에 `H2Database.class` 확인). MySQL은 Week E에서 `flyway-mysql`을 실제로 추가하게 된다.
- 진단 과정에서 `dependencies`의 `FAILED` 한 줄만 보고 "버전 미지정"으로 추정했으나 실제 원인은 "아티팩트 부재"였다. `dependencyInsight`가 `Could not find ...`로 원인을 특정해줬다. 증상만 보고 원인을 단정하지 말 것 — Week E D2 디버깅과 연결.

## [직접 작성] 오늘 배운 것을 내 문장으로

<!-- 아래는 학습자가 직접 채운다. 비워두지 말 것. -->

- Flyway를 한 문장으로:
- `@NotBlank`와 `NOT NULL`을 둘 다 두는 이유:
- `final`이 DB 값을 못 지키는 이유:
