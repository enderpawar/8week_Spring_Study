# Study Repository Instructions

## Source of truth

The active plan is **`app/study_docs/FUNDAMENTALS_ROADMAP.md`** — a 5-week, evidence-based backend *fundamentals* track for a CS junior new to Spring. Read it before guiding any study session. Check its session-resume progress checklist before using calendar dates, and resume from the first unchecked required Day.

The previous portfolio-oriented plan (`Study_plan.md`, `LEARNING_ROADMAP.md`, `LEARNING_GUIDE_TEMPLATE.md`) is archived under `past_docs/` and is superseded. Do not drive new work from it; consult it only for historical context.

## Repository layout

- `app/` — the practice project: built, tested, and used for hands-on units.
- `archive/week1`, `archive/week2` — frozen week1·2 code. Read-only; never edit.
- `week_review/` — workspace for reviewing/summarizing weeks 1–2 (velog posts, term notes, code-dissection guide).
- `app/study_docs/` — active roadmap, principle notes (`spring-core-notes.md`), interview notes.
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

## Technical writing headings — mandatory for Codex

Before Codex creates or edits any Velog post or concept article, read **`app/study_docs/VELOg_POST_TEMPLATE.md` in full** and apply its writing and quality-gate rules. Do not copy an older post's style when it conflicts with the current template.

Every H1, H2, H3, and H4 in Velog posts, concept articles, and learner-facing concept notes must be a **noun phrase styled like a textbook table of contents**. This is an absolute rule for Codex. Do not use interrogative or narrative headings containing forms such as `왜`, `어떻게`, `무엇인가`, `~인가`, `~일까`, `~하는가`, `~되는가`, `~한다`, or `~아니다`.

Even in a Q&A section, keep the heading nominal, such as `Q1. flush와 commit의 역할 구분`, and place the actual question as a sentence in the body. Before finishing a writing task, search all headings and remove any non-nominal form.

Concept sections must not stop at a terminology table. Explain **motivation → execution sequence → distinction from similar concepts → guarantees and limitations → connection to current project code**, using verified examples and counterexamples from the repository.

For every Velog or Full concept post, evaluate visual support before completion. When the topic contains execution flow, branching, hierarchy, component relationships, comparison, or state change, include at least one relevant visual. Search official documentation first and verify the image directly; if no official visual matches the repository's actual flow, create and render a repository-owned SVG plus PNG. Never use decorative stock art, unverified search thumbnails, or unattributed blog images.

## Guided learning mode

When the learner says `week N 시작`, `week N 공부 시작`, or otherwise asks to study interactively:

- The agent runs routine PowerShell commands, builds, tests, file searches, and result checks on the learner's behalf.
- Do not make the learner copy or memorize shell commands unless command-line operation itself is the current learning objective.
- Report only the meaningful outcome of each command and explain how it connects to the concept being studied.
- Keep the learner focused on reading code, predicting behavior, answering short questions, and writing `[직접 작성]` explanations.
- Teach one small concept or experiment at a time. Wait for the learner's answer before advancing when the answer is part of the exercise.
- If a command needs an external prerequisite or an action only the learner can perform, explain exactly why and give the minimum required instruction.
