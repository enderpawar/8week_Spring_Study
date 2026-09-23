# Day별 Velog 글 공식·공개 이미지 보강 명세 (2026-09-23)

## 1. 목적

각 Day 글에서 **순서·흐름·구조를 설명하는 개념 소절**에 공신력 있는 외부 그림을 찾아 넣는다. 예를 들면 요청 처리 순서, 내장 Tomcat 구조, DispatcherServlet 흐름, IoC 컨테이너의 Bean 생성, Flyway 마이그레이션 흐름, JDBC와 커넥션 풀, 영속성 컨텍스트와 엔티티 상태, flush·commit·rollback, AOP 프록시 같은 소절이다.

외부 그림은 **큰 구조**를 보여 주고, 기존 자체 UML은 **우리 코드의 흐름**을 보여 준다. 기존 그림은 교체하지 않고 새 그림을 **추가**한다.

이미지는 링크를 거는 대신 **다운로드해서 저장소에 둔다.** 원본 사이트가 URL을 바꿔도 깨지지 않게 하기 위해서다. Velog에는 지금처럼 드래그로 올린다.

## 2. 허용 출처 (우선순위순)

1. **공식 문서**
   - Spring (`docs.spring.io`, `spring.io`)
   - Apache Tomcat (`tomcat.apache.org`, Apache License 2.0)
   - Hibernate (`docs.jboss.org/hibernate`, `hibernate.org`)
   - Jakarta EE (`jakarta.ee`)
   - Flyway/Redgate 공식 문서
   - Oracle Java 공식 문서
2. **Wikimedia Commons**: CC 계열 라이선스인 경우만
3. **Stack Overflow 질문·답변 속 이미지**: CC BY-SA 4.0. 답변 URL과 작성자를 출처에 반드시 적는다.

**금지 출처**
- 개인 블로그(tistory, velog, Medium, 네이버 등)
- 튜토리얼 상업 사이트(Baeldung, GeeksforGeeks, javatpoint 등)
- 검색 결과 썸네일
- 출처나 라이선스를 확인할 수 없는 모든 이미지

## 3. 채택 조건 (전부 만족해야 한다)

1. **링크 확인**: `curl -s -o <file> -w "%{http_code} %{content_type}"`의 결과가 `200`이고 `image/*`여야 한다.
2. **내용 검수**: 다운로드한 이미지를 **Read로 직접 열어** 본다. 그 소절이 설명하는 메커니즘과 같은 것을 보여 주는지 확인한다.
   - 버전이나 경로가 다르면 채택하지 않는다.
   - 예: `@RestController` 글에 View 렌더링 경로가 들어간 MVC 그림은 거부한다.
3. **가독성**: Velog 본문 폭(약 760px)으로 줄여도 핵심 라벨을 읽을 수 있어야 한다. 생명선이 수십 개인 초대형 도식처럼 줄이면 읽을 수 없는 그림은 거부한다.
4. **본문 정합성**: 그림 속 용어·순서가 본문 설명과 충돌하지 않아야 한다. 충돌하면 거부하고 사유를 보고한다. 이 작업에서 본문은 고치지 않는다.
5. **글당 0~2장**: 맞는 그림이 없으면 넣지 않는다. 억지로 채우지 않는다. "적합한 공식 그림 없음"도 정상 결과다.

## 4. 저장과 삽입 형식

- **파일명**: `app/study_docs/assets/dayNN-web-<주제>.<원본 확장자>` (예: `day01-web-tomcat-request-process.png`)
  - 원본이 SVG면 PNG로 렌더링해 함께 둔다. 렌더링 명령은 `2026-09-23-velog-textbook-rewrite-spec.md` 6절을 따른다.
- **작업 폴더**: 다운로드와 렌더링은 스크래치패드의 `dayNN/` 하위 폴더에서만 한다. 다른 에이전트와 파일이 겹치지 않게 하기 위해서다.
  - 스크래치패드: `C:/Users/jinwoo/AppData/Local/Temp/claude/C--Users-jinwoo-OneDrive-------Spring-Study/35305389-4ef2-43e8-a9f0-01c7e0e8f05b/scratchpad`
- **삽입 위치**: 해당 H3 소절 안, 흐름 설명(`text` 블록이나 산문) 바로 뒤. 형식은 다음과 같다.

```markdown
그림을 소개하는 한 문장 (예: Tomcat 공식 문서는 요청이 Connector에서 Servlet까지 가는 경로를 다음처럼 그린다.)

![그림이 보여주는 흐름을 문장으로 쓴 대체텍스트](../../../assets/dayNN-web-주제.png)
<!-- velog 업로드: 이 줄 위 이미지 자리에 app/study_docs/assets/dayNN-web-주제.png 파일을 드래그해 교체 -->

*출처: [문서/답변 제목](원본 페이지 URL) — 저작권·라이선스 표기*
```

- 소개 문장 한 줄과 이미지 블록 외에는 본문을 수정하지 않는다.
- Stack Overflow 출처는 `— 작성자 이름, CC BY-SA 4.0`까지 적는다.

## 5. 담당

**Day 하나에 에이전트 하나**를 둔다. 담당 파일은 `app/study_docs/days/Week*/DayNN_*/velog_post.md` 한 개다. 그 글의 소절 목록을 먼저 뽑고, 흐름·구조 소절마다 후보를 탐색한다.

## 6. 금지 사항

- 담당 외 파일 수정 금지
- 기존 그림 삭제·교체 금지
- 커밋 금지
- 본문 설명 수정 금지

## 7. 완료 보고

1. 채택한 이미지: 파일명, 원본 이미지 URL, 출처 페이지 URL, 라이선스, 삽입한 소절 이름, 내용 검수 결과 한 줄
2. 검토했지만 거부한 후보: URL과 거부 사유
3. 그림을 넣지 않은 흐름 소절과 그 이유
