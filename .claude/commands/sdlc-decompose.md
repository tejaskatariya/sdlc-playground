---
description: Decompose an epic issue into FSM-labeled story issues with dependency edges
argument-hint: <epic-issue-number>
---

Read `.sdlc-factory/prompts/decomposer.md` and follow it for epic #$ARGUMENTS:
fetch the epic body with `gh issue view`, propose the story breakdown with
the depends-on DAG for my approval FIRST, and only after I approve create the
issues with `gh issue create` (labels `stage:plan` + `status:queued`,
`depends-on: #N` lines in the bodies).
