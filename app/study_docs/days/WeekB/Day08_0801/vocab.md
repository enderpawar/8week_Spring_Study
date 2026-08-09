# Day08 (8/1 계획 → 8/5 완료, Week B D1) 단어장 — Flyway·스키마 정의

형식: **용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어**

## Full 루프 — Flyway와 마이그레이션

| 용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어 |
|---|---|---|---|---|
| 마이그레이션(migration) | 스키마를 바꾸는 작업 하나를 담은 SQL 파일 | 상태 전이(state transition)를 파일 단위로 고정 | `V1__init.sql` | Flyway, DDL |
| Flyway | 버전 붙은 SQL 파일을 순서대로 한 번씩만 실행하고, 실행 이력과 체크섬을 DB 장부에 남기는 도구 | git의 커밋 이력 관리와 같은 구조(적용 지점 기록 + 과거 변조 차단) | `implementation("org.flywaydb:flyway-core")` | 장부, 체크섬 |
| `flyway_schema_history` | Flyway가 DB 안에 만드는 자기 관리용 장부 테이블 | HEAD 포인터 — "어디까지 적용됐나"를 DB 자신이 기억 | `SELECT * FROM "flyway_schema_history"` | installed_rank, success |
| `success = FALSE` 행 | 실패한 마이그레이션도 장부에 남는다. 다음 기동을 막는다 | 불확정 상태에서 진행 거부(fail-fast) | 장부 조회 시 `1 / init / FALSE` | flyway repair |
| 체크섬(checksum) 검증 | 이미 적용된 파일이 바뀌었는지 해시로 대조. 다르면 기동 거부 | 해시 함수의 눈사태 효과 — 1비트만 달라도 값이 완전히 달라짐 | `Migration checksum mismatch for migration version 1` | 해시, BCrypt(Week D D1) |
| 마이그레이션 불변 규칙 | 적용된 파일은 고치지 않고 `V2`를 새로 쌓는다 | push한 커밋을 rebase하지 않는 것과 동일 | `V2__widen_room_name.sql` | 버전 관리 |
| 파일명 규칙 | `V<버전>__<설명>.sql`, 언더바 **2개** | 파서가 버전과 설명을 나누는 구분자 | `V1__init.sql` | `db/migration/` |
| `db/migration/` | Flyway가 스캔하는 고정 경로 | 규약 우선 설정(convention over configuration) | `src/main/resources/db/migration/` | classpath |
| `ddl-auto` | Hibernate가 Entity를 보고 스키마를 만들/검사하게 하는 옵션. JPA 표준 아님 | 원본이 자바 쪽에 있는 방향 | `validate`(Week B D7 예정) | Hibernate, JPA |

## Light — DDL과 제약

| 용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어 |
|---|---|---|---|---|
| DDL | 테이블 구조를 정의하는 SQL(`CREATE`/`ALTER`/`DROP`) | 타입 선언에 해당 | `CREATE TABLE reservation (...)` | DML |
| `NOT NULL` | 값의 부재를 거부하는 제약 | 데이터 무결성 최후 방어선 | `room_name VARCHAR(100) NOT NULL` | @NotBlank, CHECK |
| `NULL` vs `''` | `NULL`은 값 부재, `''`는 길이 0인 값. **다른 상태** | 옵셔널 타입과 빈 컬렉션의 차이 | `NOT NULL`은 `''`를 통과시킨다 | CHECK 제약 |
| `DEFAULT` | INSERT에서 값을 생략하면 채워지는 기본값 | 기본 인자(default argument) | `confirmed BOOLEAN NOT NULL DEFAULT FALSE` | 도메인 기본값 |
| `AUTO_INCREMENT` | DB가 행마다 번호를 붙여주는 장치 | `nextId++`를 DB가 대신 함 | `id BIGINT NOT NULL AUTO_INCREMENT` | PK, 시퀀스 |
| 번호 소실 | 실패·롤백된 INSERT도 번호를 소비한다. 되돌리지 않는다 | 되돌리려면 채번 구간을 직렬화해야 함 → 동시성 손해 | 실패 후 다음 행 `id = 2` | 동시성, 트랜잭션 |
| `PRIMARY KEY` | 행을 유일하게 식별하는 컬럼 | 후보키 선정, 유일성 보장 | `PRIMARY KEY (id)` | UNIQUE, 인덱스 |
| `VARCHAR(n)` | 가변 길이 문자열. **길이는 계약** | 입력 도메인의 상한 선언 | `VARCHAR(100)` — 표준명 `CHARACTER VARYING` | 제약 |
| 앱 검증 vs DB 제약 | 앱 검증은 UX(친절한 400), DB 제약은 무결성(모든 경로 차단) | 계층별 방어(defense in depth) | `@NotBlank` ↔ `NOT NULL` | 중복 아님 |

## Light — 식별자와 이름

| 용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어 |
|---|---|---|---|---|
| 식별자 대소문자 접힘 | 따옴표 없는 SQL 식별자는 대문자로 접힌다 | 어휘 분석 단계의 정규화 | `reservation` → `RESERVATION` | quoted identifier |
| 따옴표 식별자 | `"이름"`으로 감싸면 대소문자가 보존된다 | 리터럴 취급 | Flyway 장부가 `"flyway_schema_history"`로 소문자 유지 | 접힘 |
| 네이밍 전략 | Hibernate가 camelCase를 snake_case로 자동 변환 | 경계에서의 이름 번역 | `requesterName` → `requester_name` | 자바↔DB 경계 |
| `final` vs 컬럼 제약 | `final`은 JVM 메모리 안 변수 재대입만 막는다. DB 행과 무관 | 컴파일 시점 규칙 vs 실행 시점 데이터 제약 | 앱 꺼진 상태의 `UPDATE`는 그대로 성공 | 불변성, 작용 범위 |

## 관찰 — 인프라

| 용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어 |
|---|---|---|---|---|
| BOM / 의존성 관리 | 라이브러리 버전 조합을 미리 맞춰둔 목록 | 버전 충돌 회피 | 버전 없이 `implementation("org.flywaydb:flyway-core")` | dependency-management |
| BOM 미관리 아티팩트 | 목록에 없으면 버전을 직접 적어야 한다 | 가정이 깨지는 지점 | `dependencies` 리포트의 `FAILED` | dependencyInsight |
| DataSource 자동 구성 | jdbc + DB 드라이버가 클래스패스에 있으면 Boot가 알아서 만든다 | 규약 기반 추론 | 설정 없이도 HikariPool 기동 | auto-configuration |
| HikariCP | Spring Boot 기본 커넥션 풀 | 자원 풀링 | `starter-jdbc`에 딸려 옴 | 커넥션풀(Week C D3) |
| H2 file 모드 | DB를 파일에 저장 → 재시작해도 데이터 유지 | 휘발성 메모리 vs 영속 저장 | `jdbc:h2:file:./data/studyroom` | 영속성 |
| `MODE=MySQL` | 동작 호환성 일부만 맞추는 모드. 파서는 H2 그대로 | 에뮬레이션의 한계 | `#` 주석은 여전히 문법 오류 | 방언(dialect) |
