# [백엔드 기본기 Day8] Flyway — 적용된 마이그레이션을 왜 고칠 수 없나

Day4에서 `InMemoryReservationRepository`가 예약을 `ArrayList`에 담게 만들면서, 프로세스를 재시작하면 데이터가 사라진다는 부채를 기술부채 원장에 남겨뒀다. 오늘은 그 저장소를 교체하기 전에 데이터가 들어갈 테이블부터 만든 날이다. 스키마를 SQL 파일로 정의하고 Flyway가 그걸 어떻게 다루는지까지만 봤고, Entity 매핑과 JPA는 다음 Day들의 범위라 손대지 않았다.

> `V1__init.sql`에 `reservation` 테이블을 직접 쓰고 H2 파일 DB에 적용했다. 이미 적용된 그 파일에 주석 한 줄을 추가하고 다시 띄웠더니 체크섬 불일치로 컨텍스트가 뜨지 못해 테스트 10개 중 6개가 함께 깨졌고, 앱을 거치지 않고 DB에 직접 넣은 빈 문자열은 `NOT NULL` 컬럼을 그대로 통과했다. 두 결과 모두 예측과 달랐다. `@Transactional`과 Entity 매핑은 아직 도입하지 않은 상태다.

## 1. 개념 설명

오늘 정리한 용어부터.

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| 마이그레이션 | 스키마를 바꾸는 작업 하나를 담은 SQL 파일 | `V1__init.sql` |
| Flyway | 버전 붙은 SQL을 순서대로 한 번씩만 실행하고 이력·체크섬을 장부에 남기는 도구 | `implementation("org.flywaydb:flyway-core")` |
| 스키마 히스토리 | Flyway가 DB 안에 만드는 자기 관리용 장부 테이블 | `SELECT * FROM "flyway_schema_history"` |
| 체크섬 검증 | 적용된 파일이 바뀌었는지 해시로 대조하고 다르면 기동을 거부 | `Migration checksum mismatch for migration version 1` |
| `NOT NULL` | 값의 부재를 거부하는 제약. 길이 0인 값은 부재가 아니다 | `room_name VARCHAR(100) NOT NULL` |
| `AUTO_INCREMENT` | DB가 행마다 번호를 붙여주는 장치 | `id BIGINT NOT NULL AUTO_INCREMENT` |
| 식별자 접힘 | 따옴표 없는 SQL 식별자는 대문자로 접힌다 | `reservation` → `RESERVATION` |
| 네이밍 전략 | Hibernate가 camelCase를 snake_case로 단어 분리 | `requesterName` → `requester_name` |

**앞의 넷은 "스키마를 어떻게 관리하는가", 뒤의 넷은 "그 스키마가 자바 코드와 어떻게 어긋나는가"다.**

Flyway는 SQL을 생성하지도, 어디선가 옮겨오지도 않는다. 내가 손으로 쓴 파일을 실행하고 그 사실을 기록할 뿐이다. 그 기록이 있기 때문에 매 기동마다 이런 판단이 가능해진다.

```text
기동
  → flyway_schema_history 조회: "V1까지 적용됨"
  → db/migration 스캔: V1, V2 존재
  → V1은 건너뛰고 V2만 실행
  → 장부에 V2와 그 체크섬 기록
```

이 루프에서 세 가지가 따라온다. 같은 SQL이 두 번 돌지 않고, 빈 DB든 쓰던 DB든 결국 같은 상태에 도달하고, 이미 적용된 파일이 바뀌면 기동이 멈춘다.

세 번째가 오늘의 중심 질문이다. Hibernate의 `ddl-auto`처럼 Entity를 보고 스키마를 만들어주는 방식과 비교하면 방향이 반대다. `ddl-auto`는 자바가 원본이고 DB가 결과물이지만, Flyway를 쓰면 SQL 파일이 원본이고 자바는 거기에 맞춘다.

> **더 볼 것**
> - [Database Initialization :: Spring Boot](https://docs.spring.io/spring-boot/how-to/data-initialization.html): Boot가 Flyway를 언제 실행하고 어떤 속성을 읽는지
> - [Migrations - Redgate Flyway](https://documentation.red-gate.com/flyway/flyway-concepts/migrations): 버전 명명 규칙과 반복 실행 마이그레이션
> - 아직 안 본 것 — `flyway repair`, `CHECK` 제약, 커넥션풀 설정

## 2. 코드 구현

### `reservation` — 자바 필드 4개에서 컬럼 4개로

```sql
CREATE TABLE reservation (
     id BIGINT NOT NULL AUTO_INCREMENT,
     room_name VARCHAR(100) NOT NULL,
     -- 자바는 camelCase, SQL은 snake_case
     requester_name VARCHAR(50) NOT NULL,
     confirmed BOOLEAN NOT NULL DEFAULT FALSE,
     PRIMARY KEY (id)
);
```

`Reservation`의 `roomName`, `requesterName`은 자바에서 `final`이지만 컬럼에는 그 사실을 옮길 자리가 없었다. `VARCHAR`의 길이는 저장 공간이 아니라 계약으로 봤다. 이보다 긴 값은 잘못된 데이터라고 선언하는 쪽이 나중에 원인을 찾기 쉽다고 판단해서 방 이름 100, 사람 이름 50으로 뒀다.

`AUTO_INCREMENT`는 `InMemoryReservationRepository`의 `nextId++`가 하던 일을 DB로 옮긴 것이다. 다만 이 둘이 같지 않다는 게 실험에서 드러났다.

### 첫 기동은 문법 오류로 멈췄다

주석을 `#`으로 달았더니 파싱 단계에서 죽었다. `MODE=MySQL`을 켜뒀으니 MySQL 방언인 `#`도 받아줄 거라고 봤는데 아니었다.

```text
Error Code : 42000
Message : Syntax error in SQL statement
"... requester_name VARCHAR(50) NOT NULL, [*]#Java는 CamelCase ..."
```

`[*]`가 파서가 막힌 지점이다. 호환 모드는 타입과 함수 동작 일부를 맞춰줄 뿐 파서까지 MySQL로 바꾸지는 않는다는 걸 여기서 확인했다.

실패한 마이그레이션도 장부에는 `success = FALSE`로 남았고, 그 줄이 남아 있는 한 다음 기동이 다시 막힌다. 운영이라면 `flyway repair`로 실패 기록만 지워야 하지만, 데이터가 한 행도 없는 개발 DB라 파일을 지우고 처음부터 돌렸다.

### 오늘 확인한 것

| 확인 | 방법 | 결과 |
|---|---|---|
| `V1` 적용과 컬럼 정의 | `information_schema` 조회 | `RESERVATION` 4컬럼, 장부 `success = TRUE` |
| `NOT NULL`이 `NULL`을 막는가 | H2 콘솔에서 직접 INSERT | 거부(`23502`) |
| 체크섬 불일치 시 기동 | 주석 추가 후 `./gradlew test` | 컨텍스트 기동 실패, 10개 중 6개 실패 |

원복 후 테스트는 전원 통과했다. 커밋: [d2e9d55](https://github.com/enderpawar/8week_Spring_Study/commit/d2e9d557255a4bd3c9870f355383b3c04847ce7d)

H2 콘솔 조회와 INSERT 실험은 손으로 한 확인이고 자동 테스트로 고정하지는 않았다. 앱이 이 테이블을 실제로 읽고 쓰는 경로는 아직 없다.

## 3. 스스로 답한 질문

### 주석 한 줄은 SQL 동작에 영향이 없는데 왜 문제가 되나

처음에는 "주석은 SQL 동작에 영향이 없으니 통과"라고 예측했다. 결과는 반대였다.

```text
FlywayValidateException:
  Validate failed: Migrations have failed validation
  Migration checksum mismatch for migration version 1
```

틀린 이유는 Flyway가 SQL을 해석해서 비교한다고 생각한 데 있었다. 실제로는 파일 내용 전체의 해시 하나를 장부값과 대조한다. 해시는 입력이 조금만 달라도 완전히 다른 값이 나오므로 "이건 주석이라 의미 없음" 같은 판단은 애초에 하지 않는다.

이 융통성 없음이 기능이라는 쪽으로 이해가 바뀌었다. 주석을 봐주면 다음은 공백, 그다음은 컬럼 순서, 길이 변경이 되고, 어디까지 봐줄지 재는 순간 내 DB와 서버 DB가 같은 상태라는 보장이 사라진다. 그래서 규칙은 하나다 — 적용된 파일은 고치지 말고 `V2`를 새로 쌓는다. push한 커밋을 rebase하지 않는 것과 같다.

깨진 테스트 6개에 `reservationServiceBeanIsSingleton`처럼 SQL과 무관한 것이 섞여 있었던 것도 같은 이야기다. Flyway가 멈추면 Spring 컨텍스트 자체가 뜨지 못하므로 그 위에 얹힌 테스트가 전부 함께 죽는다.

### `@NotBlank`가 있는데 `NOT NULL`을 또 거는 건 중복 아닌가

처음 답은 "`@NotBlank`는 공백이 없음을 검사하는 거고 `NOT NULL`은 아예 값이 없음을 의미하는 거지"였다. 검사 대상이 다르다는 것까지는 맞았지만, 그래서 어느 쪽이 무엇을 못 막는지는 보지 못했다.

앱을 거치지 않고 H2 콘솔에서 직접 넣어봤다.

```sql
INSERT INTO reservation (room_name, requester_name) VALUES (NULL, '이진우');
-- NULL not allowed for column "ROOM_NAME"

INSERT INTO reservation (room_name, requester_name) VALUES ('', '이진우');
-- (Update count: 1)
```

빈 문자열은 들어갔다. `NOT NULL`이 보는 것은 "값이 있는가" 하나뿐이고, `''`는 값이 없는 게 아니라 길이 0인 값이 있는 상태다. 반대로 `@NotBlank`는 `''`를 막지만 HTTP를 거치지 않은 이 INSERT는 보지도 못했다.

교정된 기준은 둘이 중복이 아니라 못 막는 구멍이 서로 다르다는 것이다. 앱 검증은 사용자에게 400과 설명을 주는 UX 장치고, DB 제약은 경로와 무관하게 데이터 자체를 지키는 장치다.

### `final`로 선언하면 그 값은 못 바뀌는 것 아닌가

`roomName`이 `final`이니 저장된 값도 못 바꾼다고 답했다. "불변성을 정의하는 것"이라고 썼는데, 두 번 연속 같은 답을 했다.

앱을 띄우지 않은 채 H2 콘솔로만 실행해봤다.

```sql
UPDATE reservation SET room_name = 'B202';
-- 3 | B202
```

그냥 바뀌었다. `Reservation.java`는 컴파일도 실행도 되지 않았으니 `final`이 개입할 지점이 없었다. `final`은 자바 컴파일러가 JVM 메모리 안 변수의 재대입만 검사하는 규칙이고, 디스크에 있는 행은 검사 대상이 아니다. 앱을 완전히 종료해도 `data/studyroom.mv.db` 안의 값은 그대로 남는다.

재발 방지로는 "이 규칙을 강제하는 주체가 누구이고 언제 검사하나"를 먼저 묻기로 했다. `final`은 컴파일러가 컴파일 시점에, 컬럼 제약은 DB가 실행 시점에 검사한다. 이 질문은 `@NotBlank`와 `NOT NULL`에도 그대로 통했다.

## 4. 정리하며

오늘 바뀐 이해는 두 개다. 하나는 스키마 관리 도구가 똑똑할 필요가 없다는 것이다. Flyway는 SQL을 이해하지 못하고 해시만 비교하는데, 그 단순함이 오히려 "모든 환경이 같은 순서로 같은 상태에 도달한다"를 보장한다.

다른 하나는 검증과 제약이 작동하는 층을 구분하게 된 것이다. 오늘 틀린 두 예측이 같은 모양이었다. `final`이면 DB 값도 고정될 거라고 봤고, `@NotBlank`가 있으면 빈 문자열은 어디로도 못 들어올 거라고 봤다. 둘 다 "어떤 층에서 누가 검사하는가"를 안 물어서 생긴 오해였다. `MODE=MySQL`인데 `#` 주석이 막힌 것도 결국 같은 구분이 필요한 자리였다.

남은 것도 이 주제와 이어진다. `NOT NULL`은 `room_name = ''`인 행을 막지 못하는데, 지금 앱에는 그 경로가 없지만 배치나 다른 클라이언트가 붙으면 그대로 뚫린다. DB 수준에서 막으려면 `CHECK` 제약이 필요하고, **나중에 고칠 것**으로 분류해 Week B D7에 배정했다. `ArrayList` 저장소를 이 테이블로 교체하는 일은 Entity 매핑을 배우는 Week B D3에 그대로 남아 있다.

면접에서 받으면 다시 정리해봐야 할 질문 두 개를 남겨둔다.

- 운영 중인 서비스에서 이미 배포된 마이그레이션의 컬럼 타입을 바꿔야 한다면, 무중단으로 하려면 어떤 순서가 필요한가
- `AUTO_INCREMENT`가 실패한 트랜잭션의 번호를 되돌리지 않는 이유는 무엇이고, 그 성질이 문제가 되는 상황은 언제인가
