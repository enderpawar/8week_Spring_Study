# StudyRoom API

> 스터디룸 예약 백엔드. Spring Boot 3 · Java 17 · JPA · MySQL.
>
> **이 README는 Week 9에서 채용 담당자용 포트폴리오 진입점으로 완성됩니다.** 지금은 자리 표시입니다.

## 저장소 구조

| 경로 | 내용 |
|---|---|
| `app/` | 실제 빌드·배포되는 단 하나의 프로젝트 (인증 → 예약 → 동시성 → 검색 → 배포로 누적) |
| `docs/weekN/` | 주차별 설계 결정·측정 보고서 (증거) |
| `archive/week1`, `archive/week2` | 학습 기록. 동결됨(읽기 전용) |
| `study_docs/` | 학습 로드맵, Spring/JPA 원리 노트, 면접 노트 |
| `Study_plan.md` | 전체 실행 명세 |

## 학습 진행 방법

전체 순서와 각 구간의 학습법은 **[study_docs/LEARNING_ROADMAP.md](study_docs/LEARNING_ROADMAP.md)** 를 본다.

## 실행 (개발 중)

```bash
cd app
./gradlew test
```

<!-- Week 9 TODO: 실배포 URL, 아키텍처 다이어그램, 핵심 기능 스크린샷/수치, 실행 방법, 기술 선택 근거 -->
