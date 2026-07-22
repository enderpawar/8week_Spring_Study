# 3주차 학습 가이드: Spring Security와 JWT

이번 주차의 핵심은 코드를 외우는 것이 아니라 다음 한 문장을 이해하는 것입니다.

> 로그인에 성공하면 서버가 JWT를 발급하고, 이후 요청마다 JWT를 검사해 “누가 보낸 요청인지” 확인한다.

## 1. 먼저 알아야 할 단어

### 인증(Authentication)과 인가(Authorization)

- **인증**은 “당신은 누구인가?”를 확인하는 과정입니다. 로그인과 JWT 검사가 인증입니다.
- **인가**는 “당신이 이 행동을 해도 되는가?”를 결정하는 과정입니다. 예를 들어 일반 회원은 관리자 페이지에 들어갈 수 없게 하는 것이 인가입니다.

이 프로젝트는 회원 여부까지 확인하므로 인증을 구현했습니다. 역할별 권한 구분은 아직 하지 않으므로 복잡한 인가는 다음 단계의 주제입니다.

### 해시와 BCrypt

> BCrypt : 비밀번호를 안전하게 저장하기 위한 단방향 해시 알고리즘

비밀번호를 DB에 원문으로 저장하면 DB가 유출되었을 때 모든 비밀번호가 바로 노출됩니다. `PasswordEncoder.encode()`는 비밀번호를 원래 값으로 되돌리기 어려운 해시로 바꿉니다.

#### 해시와 암호화의 차이

- **암호화(Encryption)**: 올바른 Key가 있으면 원문으로 복호화할 수 있습니다.
- **해시(Hash)**: 원문 복원을 목적으로 하지 않는 단방향 변환입니다.

서버는 사용자의 원래 비밀번호를 다시 알아낼 필요가 없습니다. 로그인 때 입력한 값이 가입할 때 입력한 값과 같은지만 확인하면 되므로 비밀번호에는 단방향 해시가 적합합니다.

#### BCrypt가 비밀번호 저장에 적합한 이유

일반적인 빠른 해시 함수는 파일 무결성 확인에는 유용하지만, 공격자가 수많은 비밀번호 후보를 매우 빠르게 대입할 수도 있습니다. BCrypt는 비밀번호 저장을 위해 다음 기능을 함께 제공합니다.

1. **Salt**: 같은 비밀번호도 매번 다른 해시가 나오게 하는 무작위 값
2. **Cost factor**: 한 번 계산하는 데 필요한 작업량을 조절하는 숫자
3. **의도적으로 느린 계산**: 공격자가 대량의 후보를 시험하는 속도를 낮춤

Salt가 없으면 같은 비밀번호를 사용하는 회원들의 해시도 같아지고, 미리 계산된 해시 목록인 Rainbow Table 공격에 더 취약해집니다. BCrypt는 `encode()`를 호출할 때 Salt를 자동 생성하며, 그 Salt를 완성된 BCrypt 문자열 안에 함께 저장합니다. 따라서 별도의 Salt Column은 필요하지 않습니다.

#### BCrypt 결과 문자열 읽기

Spring Security의 `BCryptPasswordEncoder`가 만든 값은 일반적으로 다음과 비슷한 60자 문자열입니다.

```text
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
 │  │  └──────────────────────┬──────────────────────────┘
 │  │                      Salt + Hash
 │  └─ Cost factor
 └──── BCrypt 버전
```

- `$2a$`: 사용한 BCrypt 형식의 버전 표시
- `10`: Cost factor입니다. 대략 `2^10`에 비례하는 계산 비용을 의미합니다.
- 나머지: 비밀번호 검증에 필요한 Salt와 계산 결과

이 문자열에 Salt와 Cost가 들어 있으므로 로그인할 때 별도 설정을 기억할 필요가 없습니다. Cost를 높이면 공격은 어려워지지만 정상적인 회원가입과 로그인도 느려집니다. 이 프로젝트의 `new BCryptPasswordEncoder()`는 Spring Security의 기본 Cost를 사용합니다.

#### 같은 비밀번호인데 결과가 다른 이유

```java
String first = passwordEncoder.encode("password123");
String second = passwordEncoder.encode("password123");

// 무작위 Salt가 다르므로 일반적으로 false
boolean sameHash = first.equals(second);
```

```mermaid
flowchart LR
    Password["password123"] --> SaltA["무작위 Salt A"]
    Password --> SaltB["무작위 Salt B"]
    SaltA --> BCryptA["BCrypt + Cost"]
    SaltB --> BCryptB["BCrypt + Cost"]
    BCryptA --> HashA["서로 다른 해시 A"]
    BCryptB --> HashB["서로 다른 해시 B"]
```

두 결과가 다르다고 해서 검증할 수 없는 것은 아닙니다. `matches()`는 저장된 문자열에서 Salt와 Cost를 읽어 입력 비밀번호를 같은 조건으로 계산한 뒤 결과를 비교합니다. 저장된 해시를 복호화하는 과정은 없습니다.

```java
boolean correct = passwordEncoder.matches(
        "사용자가 로그인 때 입력한 비밀번호",
        user.getPassword() // DB에 저장된 BCrypt 문자열
);
```

```mermaid
flowchart TD
    Input["로그인 때 입력한 비밀번호"] --> Matches["PasswordEncoder.matches()"]
    Stored["DB의 BCrypt 문자열<br/>버전 + Cost + Salt + Hash"] --> Matches
    Matches --> Rehash["저장된 Salt와 Cost로<br/>입력값을 다시 계산"]
    Rehash --> Compare{"계산 결과가 같은가?"}
    Compare -->|예| Success["비밀번호 일치"]
    Compare -->|아니요| Fail["비밀번호 불일치"]
```

따라서 아래처럼 새로 `encode()`한 문자열끼리 직접 비교하면 안 됩니다.

```java
// 잘못된 비교: encode()마다 Salt가 달라진다.
passwordEncoder.encode(inputPassword).equals(user.getPassword());

// 올바른 비교
passwordEncoder.matches(inputPassword, user.getPassword());
```

#### BCrypt가 해결하지 않는 것

BCrypt를 사용해도 짧고 흔한 비밀번호 자체가 강해지는 것은 아닙니다. 또한 전송 중인 비밀번호, 로그에 출력된 비밀번호, 탈취된 JWT까지 보호하지는 않습니다. 실제 서비스에서는 HTTPS, 적절한 비밀번호 정책, 로그인 시도 제한, 비밀번호 로그 금지 등을 함께 적용해야 합니다.

비밀번호를 잊어버린 경우에도 저장된 해시에서 원문을 복구할 수 없습니다. 본인 확인 후 새 비밀번호의 해시로 교체하는 비밀번호 재설정 절차가 필요합니다.

### 세션과 Stateless

세션 방식은 로그인 상태를 서버 메모리나 저장소에 보관합니다. JWT 방식은 인증에 필요한 정보를 서명된 토큰에 담아 클라이언트가 매 요청에 보냅니다. 이 프로젝트는 서버에 로그인 상태를 저장하지 않는 `STATELESS` 방식입니다.

“JWT를 쓰면 서버가 아무것도 조회하지 않는다”는 뜻은 아닙니다. 이 프로젝트의 필터는 토큰에서 이메일을 얻은 뒤 현재 회원을 DB에서 다시 조회합니다.

### JWT(JSON Web Token)

JWT는 점(`.`)으로 나뉜 세 부분으로 구성됩니다.

```text
Header.Payload.Signature
```

- Header: 서명 알고리즘 정보
- Payload: 이메일(`sub`), 발급 시각(`iat`), 만료 시각(`exp`) 같은 정보
- Signature: 중간 내용이 변경되지 않았음을 확인하는 서명

Payload는 암호문이 아니어서 누구나 읽을 수 있습니다. 비밀번호나 주민번호 같은 비밀 정보를 넣으면 안 됩니다. 서버의 비밀키는 서명을 만들고 검증하는 데 사용되므로 Git에 실제 운영 키를 올리면 안 됩니다.

### Bearer 토큰

로그인 후 받은 JWT는 다음 HTTP 헤더에 넣습니다.

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

`Bearer`는 “이 토큰을 가진 사람”이라는 의미입니다. 토큰을 훔친 사람도 만료 전까지 사용할 수 있으므로 HTTPS, 짧은 만료 시간, 안전한 저장이 중요합니다.

### Filter와 Security Filter Chain

Filter는 요청이 Controller에 도착하기 전에 실행되는 관문입니다. 여러 Filter가 순서대로 이어진 구조가 Filter Chain입니다. JWT 확인을 Controller마다 반복하지 않고 하나의 Filter에서 처리합니다.

```text
HTTP 요청
  → Security Filter Chain
  → JwtAuthenticationFilter
  → SecurityContext에 인증 정보 저장
  → 접근 허용/거부 판단
  → Controller
```

### SecurityContext와 Authentication

`Authentication`은 현재 요청을 보낸 사용자의 신원과 권한을 나타내는 객체입니다. `SecurityContext`는 현재 요청 동안 그 객체를 보관하는 공간입니다. JWT Filter가 인증 객체를 넣으면 Controller의 `@AuthenticationPrincipal`이 현재 회원 이메일을 꺼낼 수 있습니다.

## 2. 전체 구조도

아래 구조도는 Mermaid 문법으로 작성했습니다. GitHub, IntelliJ의 Mermaid 지원 Markdown 미리보기, VS Code 확장 등에서 그림으로 볼 수 있습니다. 미리보기가 지원되지 않는 편집기에서는 코드 블록으로 보일 수 있습니다.

### 인증과 인가의 큰 흐름

```mermaid
flowchart LR
    A["회원가입<br/>비밀번호 BCrypt 해시"] --> B["로그인<br/>이메일·비밀번호 확인"]
    B --> C["JWT 발급<br/>클라이언트가 보관"]
    C --> D["API 요청<br/>Authorization: Bearer JWT"]
    D --> E["인증 Authentication<br/>JWT 서명·만료·사용자 확인"]
    E --> F["인가 Authorization<br/>요청 URL에 접근 가능한가?"]
    F -->|허용| G["Controller 실행"]
    F -->|거부| H["401 또는 403 응답"]
```

여기서 로그인과 JWT 검증은 **인증**, 인증된 사용자가 특정 API에 들어가도 되는지 판단하는 것은 **인가**입니다.

### 401과 403이 결정되는 지점

```mermaid
flowchart TD
    A["보호된 API 요청"] --> B{"유효한 JWT가 있는가?"}
    B -->|아니요| C["401 Unauthorized<br/>신원을 확인할 수 없음"]
    B -->|예| D["Authentication 생성"]
    D --> E{"필요한 권한이 있는가?"}
    E -->|아니요| F["403 Forbidden<br/>신원은 알지만 권한이 없음"]
    E -->|예| G["Controller 실행"]
```

현재 프로젝트는 `MEMBER` 역할을 부여하지만 API별 역할 제한은 아직 두지 않았습니다. 따라서 주로 보게 되는 실패 응답은 JWT가 없거나 잘못됐을 때의 401입니다. 403 분기는 이후 관리자 권한 등을 추가할 때 사용합니다.

### Spring Security 내부 구성

```mermaid
flowchart LR
    Client["클라이언트"] -->|HTTP 요청| Chain["Security Filter Chain"]

    subgraph Security["Spring Security 영역"]
        Chain --> JwtFilter["JwtAuthenticationFilter"]
        JwtFilter --> Provider["JwtTokenProvider<br/>서명·만료 검증"]
        JwtFilter --> UserDetails["CustomUserDetailsService<br/>회원 조회"]
        Provider --> JwtFilter
        UserDetails --> JwtFilter
        JwtFilter --> Context["SecurityContext<br/>Authentication 보관"]
        Context --> Access["SecurityConfig<br/>접근 허용 여부 판단"]
    end

    Access -->|허용| Controller["Controller"]
    Access -->|거부| Error["401 / 403"]
```

`SecurityConfig`가 모든 일을 직접 처리하는 것은 아닙니다. 어떤 URL을 공개할지와 필터 순서를 설정하고, 실제 토큰 처리는 `JwtAuthenticationFilter`와 `JwtTokenProvider`가 담당합니다.

## 3. 요청별 코드 흐름

### 회원가입

```mermaid
sequenceDiagram
    actor Client as 클라이언트
    participant Controller as AuthController
    participant Service as AuthService
    participant Encoder as PasswordEncoder
    participant Repository as UserRepository
    participant DB as DB

    Client->>Controller: POST /api/auth/signup
    Controller->>Service: signup(request)
    Service->>Repository: existsByEmail(email)
    Repository->>DB: 이메일 중복 조회
    DB-->>Repository: 중복 여부
    Service->>Encoder: encode(비밀번호 원문)
    Encoder-->>Service: BCrypt 해시
    Service->>Repository: save(User)
    Repository->>DB: 회원 저장
    Service-->>Controller: MemberResponse (비밀번호 제외)
    Controller-->>Client: 201 Created
```

직접 따라갈 파일: `AuthController` → `AuthService` → `User` → `UserRepository`

### 로그인

```mermaid
sequenceDiagram
    actor Client as 클라이언트
    participant Controller as AuthController
    participant Service as AuthService
    participant Repository as UserRepository
    participant Encoder as PasswordEncoder
    participant JWT as JwtTokenProvider

    Client->>Controller: POST /api/auth/login
    Controller->>Service: login(request)
    Service->>Repository: findByEmail(email)
    Repository-->>Service: User
    Service->>Encoder: matches(입력 비밀번호, 저장된 해시)
    alt 비밀번호가 일치함
        Encoder-->>Service: true
        Service->>JWT: createToken(email)
        JWT-->>Service: accessToken
        Service-->>Controller: TokenResponse
        Controller-->>Client: 200 OK + JWT
    else 비밀번호가 틀림
        Encoder-->>Service: false
        Service-->>Client: 401 Unauthorized
    end
```

로그인은 회원가입과 달리 DB를 변경하지 않습니다. 그래서 `AuthService.login()`에는 쓰기용 `@Transactional`이 없습니다.

### 내 정보 조회

```mermaid
sequenceDiagram
    actor Client as 클라이언트
    participant Filter as JwtAuthenticationFilter
    participant JWT as JwtTokenProvider
    participant UserDetails as CustomUserDetailsService
    participant Context as SecurityContext
    participant Access as 접근 권한 검사
    participant Controller as MemberController
    participant Service as MemberService

    Client->>Filter: GET /api/members/me + Bearer JWT
    Filter->>JWT: isValid(token)
    JWT-->>Filter: 서명·만료 검증 결과
    alt 유효한 토큰
        Filter->>JWT: getEmail(token)
        JWT-->>Filter: 이메일
        Filter->>UserDetails: loadUserByUsername(email)
        UserDetails-->>Filter: UserDetails
        Filter->>Context: Authentication 저장
        Filter->>Access: 요청 전달
        Access->>Controller: 인증된 요청 허용
        Controller->>Service: findMe(email)
        Service-->>Controller: MemberResponse
        Controller-->>Client: 200 OK + 내 정보
    else 토큰 없음 또는 유효하지 않음
        Filter->>Access: 인증 정보 없이 요청 전달
        Access-->>Client: 401 Unauthorized
    end
```

토큰이 없거나 잘못되면 Controller까지 도착하지 못하고 `401 Unauthorized`가 반환됩니다.

## 4. 파일별 역할 지도

| 파일 | 한 가지 책임 |
|---|---|
| `SecurityConfig` | 공개 URL과 보호 URL, 세션 정책, 필터 순서 설정 |
| `JwtAuthenticationFilter` | 요청 헤더에서 JWT를 찾아 인증 객체 생성 |
| `JwtTokenProvider` | JWT 생성, 서명 검증, 이메일 추출 |
| `CustomUserDetailsService` | 이메일로 Spring Security용 회원 정보 조회 |
| `AuthService` | 회원가입과 로그인 업무 규칙 |
| `MemberService` | 인증된 내 정보 조회 |
| `PasswordEncoder` | 비밀번호 해시와 비교 |

## 5. 추천 학습 순서

1. `requests.http`로 회원가입 → 로그인 → 내 정보 조회를 직접 실행합니다.
2. `User.password`에 원문이 아닌 `$2a$...` 형태의 값이 저장되는지 확인합니다.
3. `SecurityConfig`에서 `/api/auth/**`만 `permitAll()`인 이유를 확인합니다.
4. `JwtAuthenticationFilter`에 중단점을 걸고 요청 헤더, token, email, Authentication을 순서대로 관찰합니다.
5. 토큰을 한 글자 바꾸거나 Authorization 헤더를 빼서 401 응답을 확인합니다.
6. `AuthApiIntegrationTests`의 테스트 이름을 읽고 각 완료 기준과 연결합니다.

## 6. 다시 작성해 보는 순서

한 번에 Security 전체를 지우지 말고 아래 순서로 작은 성공을 반복하세요.

1. `SignupRequest`, `MemberResponse`, `User.password`
2. `PasswordEncoder` Bean과 `AuthService.signup()`
3. `LoginRequest`, `TokenResponse`, `AuthService.login()`
4. `JwtTokenProvider`
5. `CustomUserDetailsService`
6. `JwtAuthenticationFilter`
7. `SecurityConfig`
8. `MemberController.me()`

각 단계 뒤에 테스트를 실행하면 어느 단계에서 문제가 생겼는지 찾기 쉽습니다.

## 7. 자주 헷갈리는 질문

**JWT 안의 이메일을 사용자가 바꾸면 어떻게 되나요?**  
내용을 바꾸면 서명이 맞지 않아 검증에 실패합니다. 단, 비밀키가 유출되면 공격자가 올바른 서명을 만들 수 있습니다.

**로그아웃 API가 왜 없나요?**  
현재 구조는 서버에 세션을 저장하지 않으므로 클라이언트가 토큰을 버리는 것이 기본 로그아웃입니다. 즉시 강제 로그아웃이나 탈취 토큰 차단에는 별도의 차단 목록 또는 Refresh Token 설계가 필요합니다.

**401과 403은 무엇이 다른가요?**  
401은 신원을 확인하지 못한 상태이고, 403은 신원은 확인했지만 해당 행동의 권한이 없는 상태입니다.

**CSRF를 왜 껐나요?**  
브라우저 쿠키 기반 세션이 아니라 Authorization 헤더의 토큰을 사용하는 stateless API이기 때문입니다. 나중에 JWT를 쿠키에 저장한다면 CSRF 대책을 다시 검토해야 합니다.

## 8. 스스로 답해 볼 완료 질문

1. 비밀번호를 암호화가 아니라 단방향 해시로 저장하는 이유는 무엇인가?
2. `encode()` 결과를 직접 비교하지 않고 `matches()`를 쓰는 이유는 무엇인가?
3. JWT Payload에 비밀번호를 넣으면 안 되는 이유는 무엇인가?
4. Filter에서 만든 Authentication은 어디에 저장되는가?
5. 인증되지 않은 요청은 왜 Controller까지 도착하지 않는가?
6. 인증과 인가의 차이를 본인의 예시로 설명할 수 있는가?

이 여섯 질문에 코드 없이 답할 수 있으면 3주차의 핵심 흐름을 이해한 것입니다.
