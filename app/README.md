# app — 기본기 학습 프로젝트 (새 시작점)

이 프로젝트는 **비어 있는 최소 스켈레톤**이다. `study_docs/FUNDAMENTALS_ROADMAP.md`를 따라 주차별로 계층을 직접 쌓아 올린다.

## 현재 상태 (Week A 시작 전)

- Spring Boot 3.5.3 · Java 17
- 의존성: `web`, `validation` (+ test) — 웹 계층만
- 소스: `StudyRoomApiApplication`(기동 진입점) + 컨텍스트 로드 테스트 하나

데이터 계층(JPA·H2·Flyway)은 Week B, 시큐리티·JWT는 Week D에서 **직접 추가**한다. 롬복은 일부러 넣지 않았다(생성자·DTO를 직접 쓰며 이해하기 위해).

## 실행

```bash
cd app
./gradlew test        # 스켈레톤이 뜨는지 확인 (green이어야 함)
./gradlew bootRun     # 앱 기동
```

## 막혔을 때 참고

완성된 이전 버전(인증·JWT·StudyRoom CRUD·Spring Core 실험)은 **`archive/app_v1_reference/`** 에 읽기 전용으로 동결돼 있다. 답을 베끼지 말고, 정말 막혔을 때 한 부분만 열어 본다.
