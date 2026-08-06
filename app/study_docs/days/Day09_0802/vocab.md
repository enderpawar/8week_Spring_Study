# Day09 (8/2 계획 → 8/6 진행, Week B D2) 용어

주제: JDBC로 Repository 구현 — "왜 JPA인가"의 전제 만들기

## 1. JDBC 4단계

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| `DataSource` | 커넥션 풀을 감싼 표준 인터페이스. "연결 하나 빌려줘"의 창구 | `private final DataSource dataSource;` |
| 커넥션 풀 | 미리 열어둔 DB 연결을 빌려주고 반납받는 저장소. Boot 기본은 HikariCP | 자동 구성 Bean |
| `Connection` | DB와의 세션 하나. TCP 소켓 + 인증이 끝난 상태 | `dataSource.getConnection()` |
| `PreparedStatement` | 구조가 먼저 굳은 SQL문. 값은 `?` 자리에 따로 바인딩 | `con.prepareStatement(sql)` |
| 파라미터 바인딩 | 값을 SQL 문자열에 이어붙이지 않고 자리에 채우는 것. SQL Injection 차단의 근거 | `ps.setLong(1, id)` — **1부터 시작** |
| `ResultSet` | 결과 행을 가리키는 **커서**. 결과 전체를 담은 컬렉션이 아니다 | `ps.executeQuery()` |
| `rs.next()` | 커서를 다음 행으로 **이동**하고 행이 있으면 `true` | `while (rs.next())` |
| `rs.getXxx("컬럼명")` | 지금 커서가 있는 행에서 값만 읽는다. 이동하지 않는다 | `rs.getBoolean("confirmed")` |

**핵심 구분: `next()`는 행을 넘길 때만, `getXxx()`는 값을 읽을 때만.**
`mapRow`처럼 "이미 정해진 한 행을 객체로 바꾸는" 코드에서 `next()`를 부르면 행이 하나 걸러 하나씩 사라진다. 컴파일도 되고 예외도 안 나는 종류의 버그다.

## 2. 자원과 예외

| 용어 | 한줄뜻 |
|---|---|
| try-with-resources | 소괄호 안에서 연 자원을 블록 이탈 시 **역순으로** 자동 `close()`. 예외 여부와 무관 |
| 커넥션 누수 | 반납하지 않은 커넥션이 쌓여 풀이 마르는 것. 서버 전체가 멈춘다 |
| checked 예외 | 잡거나 던진다고 선언하지 않으면 **컴파일이 안 되는** 예외. `SQLException`이 그것 |
| 예외 원인(cause) | `new IllegalStateException(msg, e)`의 `e`. 안 넘기면 스택트레이스가 잘려 원인을 잃는다 |
| `RETURN_GENERATED_KEYS` | INSERT 후 DB가 붙인 `AUTO_INCREMENT` 값을 돌려받는 옵션 |

## 3. 컨테이너 / 설정

| 용어 | 한줄뜻 | 오늘 만난 모습 |
|---|---|---|
| `NoUniqueBeanDefinitionException` | 주입할 타입의 후보 Bean이 2개 이상이라 고를 수 없음 | `expected single matching bean but found 2` |
| fail fast | 모호하면 임의로 고르지 않고 즉시 죽는다 | 조용히 InMemory가 주입됐다면 재시작마다 데이터가 사라지는 걸 몇 주 뒤에 발견했을 것 |
| 외부화 설정 | 같은 jar를 환경만 바꿔 배포하기 위해 설정을 코드 밖에서 주입하는 것 | `SPRING_DATASOURCE_URL` |
| 설정 우선순위 | 커맨드라인 > **OS 환경변수** > `application-{profile}.yml` > `application.yml` | 환경변수가 yml을 이겼다 |
| 환경변수 → 프로퍼티 | `SPRING_DATASOURCE_URL` → `spring.datasource.url` (대문자·`_` → 소문자·`.`) | — |
| 단위 테스트 | 스프링 없이 `new`로 조립. DB 없음, 즉시 끝남 | `ReservationServiceTest` |
| 통합 테스트 | `@SpringBootTest`로 컨테이너를 띄움. Bean 스캔·DB 연결 발생 | `StudyRoomApiApplicationTests` |

## 4. 저장 계약

| 용어 | 한줄뜻 |
|---|---|
| `save()`의 계약 | "저장한다"가 아니라 **"id가 없으면 신규(INSERT), 있으면 갱신(UPDATE)"** |
| `executeUpdate()`의 반환값 | 영향받은 행 수. UPDATE에서 `0`이면 그 id가 DB에 없다는 뜻 |

인터페이스는 시그니처(이름·인자·반환타입)만 강제하고 **의미는 강제하지 못한다.** 오늘 `JdbcReservationRepository`는 컴파일도 되고 테스트도 전부 통과했지만 계약의 절반(갱신)을 빠뜨린 채였다.

## 5. 아직 안 다룬 것 (D2 잔여)

- `JdbcTemplate` / `JdbcClient` — 위 반복 코드를 줄이는 스프링 도구
- JPA / Hibernate / Spring Data JPA 의 차이 (명세 / 구현체 / 그 위의 추상화)
