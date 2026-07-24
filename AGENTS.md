# Study Repository Instructions

## Week command routing

When the user asks `week N 구현해`, `N주차 구현해`, or another Week-specific implementation request:

1. Read `Study_plan.md` completely.
2. Read the matching Week section and the shared rules in sections 3–6.
3. Read `study_docs/LEARNING_GUIDE_TEMPLATE.md` completely.
4. Inspect the existing Week directory before editing; preserve user changes and passing tests.
5. Follow the diagnose → implement → lecture material → evidence and verification workflow defined in `Study_plan.md`.
6. Update or create that Week's `README.md`, `LEARNING_GUIDE.md`, `requests.http`, tests, and required evidence report.
7. Do not declare the Week complete unless its checklist is actually satisfied.

`bridge 구현해` follows the Bridge section of `Study_plan.md`. `week 0 구현해` follows the Week 0 section.

## Repository layout

From Week 4 onward there is exactly one codebase.

- `app/` — the only project that is built, migrated, containerized, and deployed. Accumulate features here.
- `docs/weekN/` — that week's evidence report and learning guide.
- `archive/week1..3/` — frozen learning history. Read-only; never edit.
- Root `README.md` — the portfolio entry point for hiring managers, not the study plan.

Never create a new `weekN/` project directory for Week 4 or later, and never copy `app/` to start a new week. See section 5.5 of `Study_plan.md`.

## Learning constraints

- Keep work inside the requested Week's scope.
- **The learner does not watch video lectures.** Never plan work around watching a course. Teach Spring/JPA internals by writing small runnable experiments and tests that prove one behavior each. The owned PDFs are a lookup reference for when something is unclear, not required reading, and "finish the material" is never a completion criterion.
- Explain unfamiliar terms in Korean and connect them to actual files and methods.
- Use Mermaid diagrams for architecture, decisions, and request sequences.
- Never fabricate measurements, test results, deployment status, retrospectives, or learner answers.
- Do not overwrite content marked `[직접 작성]`.
