# Week B 확정 템플릿 적용 명세 (2026-09-23)

기본 규칙은 `reviews/2026-09-23-velog-template-rollout-weekA-spec.md`를 그대로 따른다. 이 문서는 **Week A 이후 바뀐 점과 Week B 전용 규칙**만 적는다. 둘이 충돌하면 이 문서가 우선한다.

## 1. 먼저 읽을 것

1. `app/study_docs/VELOg_POST_TEMPLATE.md` 의 `# 제목` 절부터 끝까지
2. 기준 예시 두 편을 **처음부터 끝까지**
   - `days/WeekA/Day01_0725/velog_post.md`
   - `days/WeekA/Day05_0729/velog_post.md` (소절이 많은 Full 글의 예)
3. Week A 명세 2~3절

## 2. Week A 명세에서 바뀐 점

- **「이 글의 순서」 목록을 만들지 않는다.** 서두는 제목 → 도입 2~3문장 → 요약 인용블록 → `> **오늘의 흐름**`(+ 이전 Day / 다음 Day) → 바로 `## 1. 개념 설명`.
- 제목 접두사는 이미 `[Spring Study Day N]`으로 바뀌어 있다. 건드리지 않는다.
- **용어 한줄뜻 제외 목록 (반드시 지킨다):**
  - 예외 클래스(`...Exception`), 결과·오류 객체(`BindingResult`, `FieldError` 등), 단순 API 타입(`ResponseEntity`, `Optional`, `JdbcTemplate`의 개별 메서드 등)
  - 라이브러리·제품 이름 자체(Jackson, H2, HikariCP를 "제품 소개"로만 넣는 경우)
  - Java 문법, SQL 문법 키워드(`AUTO_INCREMENT`, `NOT NULL` 등)
  - **넣을 것:** Spring·Spring Boot·JPA·DB 접근의 이론·구조 용어. 예: Schema Migration, Checksum, DataSource, Connection Pool, Persistence Context, Entity, First-Level Cache, Dirty Checking, flush, Transaction Boundary, ORM, Repository
  - 5~8개로 제한한다.

## 3. Week B 전용 규칙

- 담당 글의 `## 1. 개념 설명` 앞쪽에 있는 `dayNN-overview-*` 그림 블록(소개 문장, 이미지, 출처 줄)은 **4절 `### 1) 전체 흐름 다시 보기`로 옮긴다.** overview 그림이 없는 Day는 자기 `dayNN-*` UML 중 전체 흐름에 가장 가까운 것을 4절 1)에 **다시** 쓰지 말고, 보고서에 "overview 없음"이라고만 적는다(그림은 오케스트레이터가 따로 그린다).
- 1절 소절이 8개 이상이면 **6~7개로 합친다.** 합칠 때 내용은 버리지 않고 가장 가까운 소절의 본문이나 보장 범위 박스로 옮긴다. 합친 내역을 보고한다.
- 코드 발췌는 그날 커밋의 실제 파일에서 `git show <sha>:<path>`로 읽은 것만. SQL 마이그레이션 파일(`V1__*.sql` 등)도 코드로 취급한다.
- 이 작업에서 외부 이미지 검색·다운로드는 하지 않는다.

## 4. 금지 사항

- 담당 `velog_post.md` 한 개 외 수정 금지. 커밋·푸시 금지. `app/src` 수정 금지.
- 기존 이미지 삭제 금지(위치 이동만 허용).
- 새 질문·새 실험 결과·측정하지 않은 수치 창작 금지.

## 5. 완료 보고 (짧게)

1. 새 H3 목록(1~4절)
2. 삭제·이동·합친 요소
3. 용어 한줄뜻에 넣은 용어
4. 사실 확인 중 발견한 기존 글의 오류와 처리
5. 커밋 근거가 없는 코드가 글에 있다면 그 위치
