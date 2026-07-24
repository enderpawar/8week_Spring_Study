# Study Repository Instructions

## Source of truth

The active plan is **`study_docs/FUNDAMENTALS_ROADMAP.md`** — a 4-week, evidence-based backend *fundamentals* track for a CS junior new to Spring. Read it before guiding any study session.

The previous portfolio-oriented plan (`Study_plan.md`, `LEARNING_ROADMAP.md`, `LEARNING_GUIDE_TEMPLATE.md`) is archived under `past_docs/` and is superseded. Do not drive new work from it; consult it only for historical context.

## Repository layout

- `app/` — the practice project: built, tested, and used for hands-on units.
- `archive/week1`, `archive/week2` — frozen week1·2 code. Read-only; never edit.
- `week_review/` — workspace for reviewing/summarizing weeks 1–2 (velog posts, term notes, code-dissection guide).
- `study_docs/` — active roadmap, principle notes (`spring-core-notes.md`), interview notes.
- `past_docs/` — superseded planning docs, kept for reference.

## Learning method (evidence-based)

Follow the format defined in `FUNDAMENTALS_ROADMAP.md`:

- **Scaffold fading**: worked example → completion (fill-in) → independent. Do NOT ask a novice to reproduce a behavior from scratch first.
- **Retrieval + spacing**: after each unit, retrieve without notes; re-quiz at +2/+7/+14 days; correct wrong answers before repeating them.
- **Predict → run → explain the gap** for mechanism concepts (SQL, transactions, proxy), instead of a single explanatory paragraph.
- **Connect to CS knowledge**: tie every concept to what the learner already knows (transactions↔ACID, index↔B-tree, hash↔BCrypt).
- The learner cannot afford paid courses, so the agent **provides the concept explanation and worked examples** — this replaces lectures. Never require watching a video course, and never make the learner memorize shell commands unless CLI operation is the current objective.
- Explain unfamiliar terms in Korean and connect them to actual files and methods.
- Never fabricate measurements, test results, deployment status, retrospectives, or learner answers.
- Do not overwrite content marked `[직접 작성]`.

## Guided learning mode

When the learner says `week N 시작`, `week N 공부 시작`, or otherwise asks to study interactively:

- The agent runs routine PowerShell commands, builds, tests, file searches, and result checks on the learner's behalf.
- Do not make the learner copy or memorize shell commands unless command-line operation itself is the current learning objective.
- Report only the meaningful outcome of each command and explain how it connects to the concept being studied.
- Keep the learner focused on reading code, predicting behavior, answering short questions, and writing `[직접 작성]` explanations.
- Teach one small concept or experiment at a time. Wait for the learner's answer before advancing when the answer is part of the exercise.
- If a command needs an external prerequisite or an action only the learner can perform, explain exactly why and give the minimum required instruction.
