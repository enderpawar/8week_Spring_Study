# Study Repository Instructions

## Source of truth

The active plan is **`study_docs/FUNDAMENTALS_ROADMAP.md`** — a 5-week, evidence-based backend fundamentals track for a CS junior new to Spring. Read it before guiding any study session.

At the start of every session, read the **session-resume progress checklist** in that roadmap. Calendar dates never override verified progress. Resume from the first unchecked required Day, and never skip IoC/DI, JPA, transactions, validation, error handling, authentication, testing, or debugging.

## Repository layout

- `src/` — the Spring Boot practice code and tests.
- `study_docs/FUNDAMENTALS_ROADMAP.md` — active roadmap, progress checklist, completion rules.
- `study_docs/days/` — daily vocabulary, quiz, explain log, progress, and technical blog evidence.
- `study_docs/복습큐.md` — spaced-retrieval queue and next due dates.
- `study_docs/VELOg_POST_TEMPLATE.md` — dual-purpose review and portfolio technical-blog standard.
- `study_docs/spring-core-notes.md` — principle-retrieval worksheet.
- `study_docs/interview-notes.md` — verified interview explanations accumulated from completed units.

This renewed repository is self-contained. Do not depend on files outside the repository or drive new work from archived plans in the previous repository.

## Learning method

Follow the format defined in `study_docs/FUNDAMENTALS_ROADMAP.md`:

- **Scaffold fading**: worked example → completion (fill-in) → independent. Do not ask a novice to reproduce a behavior from scratch first.
- **Retrieval + spacing**: retrieve without notes after each unit; re-quiz at +2/+7/+14 days; correct wrong answers before repeating them.
- **Predict → run → explain the gap** for mechanism concepts such as SQL, transactions, and proxies.
- **Connect to CS knowledge**: connect each concept to prior CS knowledge, such as transactions↔ACID, indexes↔B-tree, and hashing↔BCrypt.
- Provide the concept explanation and worked examples. Do not require paid courses or make the learner memorize shell commands unless CLI operation is the current objective.
- Explain unfamiliar terms in Korean and connect them to actual files and methods.
- Never fabricate measurements, test results, deployment status, retrospectives, learner answers, or design alternatives that were not considered.
- Never overwrite content marked `[직접 작성]`.

## Guided learning mode

When the learner says `오늘 학습 시작`, `DayN 시작`, `week N 시작`, `week N 공부 시작`, or otherwise asks to study interactively:

1. Read the roadmap progress checklist and identify the first unchecked required Day. The checklist wins over any Day number the learner names.
2. Read `study_docs/복습큐.md` and run due retrieval or +1-day error retests first. When due items exceed one session's capacity, follow that file's 「밀렸을 때 규칙」: take only 5–7 items (all error retests first) and never push due dates back — the backlog size is itself evidence. Leftovers go to the D7 buffer.
3. Inspect that Day's documents, current code, and tests.
4. Run routine PowerShell commands, builds, tests, file searches, and result checks on the learner's behalf.
5. Keep the learner focused on code reading, behavior prediction, short answers, and `[직접 작성]` explanations.
6. Teach one small concept or experiment at a time and wait for the learner's answer when it is part of the exercise.
7. At completion, update the Day evidence, review queue, roadmap checkbox, and explicit next starting point. Mark completion only when code/tests and required documents support it.
8. Write `velog_post.md` with `study_docs/VELOg_POST_TEMPLATE.md`; theory and verified evidence must precede implementation retrospection.
   - Weekly exception: for every D6 cumulative retrieval + D7 buffer pair, keep the Day evidence files separate but publish one combined `study_docs/velog/` post titled `[백엔드 기본기 DAY N & DAY N+1] N주차 마무리 시험`. If they occur on separate days, open the draft on D6 and finalize it after D7. Use the template's D6·D7 exam-retrospective structure instead of forcing the normal concept-post H2s.

9. **At the end of each week (after D7), promote that week's worked examples**: append the week's patterns to `study_docs/CODE_PATTERNS.md` and the matching fill-in drills to `study_docs/PATTERN_DRILLS.md`. Skeletons must come from working code in `src/`; mark anything that does not yet compile `⚠️ 미검증`. `❌ 흔한 실수` entries must be the learner's own actual errors from that week's `explain-log.md`, quoted verbatim — never generic advice. Do not include drill answers. See `CLAUDE.md` for the full rule.

If a command needs an external prerequisite or an action only the learner can perform, explain why and provide only the minimum required instruction.

