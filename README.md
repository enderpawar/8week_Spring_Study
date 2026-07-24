# StudyRoom API — 백엔드 기본기 학습 저장소

> 스터디룸 예약 백엔드를 실습 대상으로, **백엔드 기본기**를 4주에 다지는 학습 저장소.
> Spring Boot 3 · Java 17 · JPA · MySQL.

## 저장소 구조

| 경로 | 내용 |
|---|---|
| `app/` | 실습 대상 프로젝트 (빌드·테스트·실습이 이뤄지는 곳) |
| `archive/week1`, `archive/week2` | 동결된 week1·2 코드 (읽기 전용) |
| `week_review/` | week1·2 회고·정리 작업 공간 (velog 글, 용어 정리, 코드 해부 가이드) |
| `study_docs/` | 현재 로드맵·원리 노트·면접 노트 |
| `past_docs/` | 이전 포트폴리오 계획 문서 (신규 로드맵으로 대체, 보관용) |

## 학습 진행 방법

전체 순서와 학습법은 **[study_docs/FUNDAMENTALS_ROADMAP.md](study_docs/FUNDAMENTALS_ROADMAP.md)** 를 본다.
학습과학 근거(완성예제→빈칸→독립→인출→간격·교차→예측·실행·차이설명)와 4주 유닛 일정이 그 문서에 있다.

이전 포트폴리오 중심 계획(`Study_plan.md`, `LEARNING_ROADMAP.md`)은 `past_docs/`에 보관돼 있다.

## 실행

```bash
cd app
./gradlew test
```
