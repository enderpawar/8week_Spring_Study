# 3주차: Spring Security와 JWT 인증

2주차의 JPA/MySQL 스터디룸 API에 회원가입, 로그인, JWT 인증을 추가한 프로젝트입니다. 기존 스터디룸 API도 이제 JWT가 있어야 사용할 수 있습니다.

## 실행과 테스트

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "본인의 MySQL 비밀번호"
$env:JWT_SECRET = "32바이트보다-충분히-긴-나만의-비밀키"
.\gradlew.bat bootRun
```

자동 테스트는 MySQL 없이 H2로 실행됩니다.

```powershell
.\gradlew.bat test
```

API 호출 순서는 [requests.http](requests.http)를 참고하세요.

| Method | URL | 인증 | 역할 |
|---|---|---|---|
| POST | `/api/auth/signup` | 불필요 | 회원가입 |
| POST | `/api/auth/login` | 불필요 | JWT 발급 |
| GET | `/api/members/me` | Bearer JWT | 내 정보 조회 |
| ALL | `/api/study-rooms/**` | Bearer JWT | 기존 CRUD |

처음 접하는 용어와 전체 흐름은 [LEARNING_GUIDE.md](LEARNING_GUIDE.md)를 먼저 읽는 것을 권장합니다.
