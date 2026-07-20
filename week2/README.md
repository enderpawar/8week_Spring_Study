# 2주차 — StudyRoom JPA/MySQL API

1주차의 Controller → Service → Repository 구조를 유지하면서, 메모리의 `Map` 대신 MySQL과 Spring Data JPA에 데이터를 저장합니다.

## 구현 내용

- StudyRoom 생성·전체 조회·단건 조회·부분 수정·삭제
- `StudyRoom`, `User` Entity와 API DTO 분리
- Bean Validation 및 공통 오류 응답
- Service의 조회 트랜잭션과 쓰기 트랜잭션 분리
- 실제 실행은 MySQL, 자동 테스트는 H2 사용

## MySQL 준비

MySQL에서 데이터베이스를 먼저 만듭니다.

```sql
CREATE DATABASE study_room
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

PowerShell에서 접속 정보를 환경변수로 지정한 뒤 실행합니다. `DB_URL`을 생략하면 `localhost:3306/study_room`을 사용합니다.

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "본인의 MySQL 비밀번호"
.\gradlew.bat bootRun
```

`application.yml`의 `ddl-auto: update` 설정 때문에 실행 시 `study_rooms`, `users` 테이블이 생성됩니다. 이 설정은 학습용이며 운영 환경에서는 Flyway 같은 마이그레이션 도구를 사용하는 편이 안전합니다.

## API

```text
POST   /api/study-rooms
GET    /api/study-rooms
GET    /api/study-rooms/{id}
PATCH  /api/study-rooms/{id}
DELETE /api/study-rooms/{id}
```

실행 후 [requests.http](requests.http)의 요청을 순서대로 실행해 CRUD를 확인할 수 있습니다.

## 테스트

```powershell
.\gradlew.bat test
```

테스트는 별도 MySQL 없이 메모리 H2를 MySQL 호환 모드로 실행합니다.

## 1주차와 달라진 점

- `StudyRoom` Entity는 DB 테이블과 매핑되는 영속 객체입니다. 요청·응답 DTO는 API 계약만 표현하므로 DB 구조가 그대로 노출되지 않습니다.
- `JpaRepository`를 상속하면 기본 CRUD 구현과 SQL 실행을 Spring Data JPA가 제공합니다.
- Service 클래스는 기본적으로 `@Transactional(readOnly = true)`를 사용하고, 생성·수정·삭제 메서드만 쓰기용 `@Transactional`로 재정의했습니다.
- 수정 메서드에서 `save()`를 다시 호출하지 않아도 트랜잭션이 끝날 때 JPA의 변경 감지가 UPDATE SQL을 실행합니다.
- 영속성 컨텍스트는 한 트랜잭션 안에서 Entity를 관리하는 공간이며, 같은 Entity의 동일성 보장과 변경 감지를 담당합니다.

`User` Entity와 Repository는 3주차 회원가입·로그인 기능의 기반만 마련한 상태이며, 이번 주에는 인증 API를 구현하지 않습니다.
