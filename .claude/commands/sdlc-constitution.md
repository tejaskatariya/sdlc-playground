---
description: Author or amend the project constitution interactively
---

Work on `.sdlc-factory/constitution.md` — the non-negotiable law that every
Claude session loads via the CLAUDE.md import line. Read it first, then pick
the mode:

**Authoring mode** (the file still contains `[FILL-ME` markers):

1. Interview me one question at a time (skills: interview-me,
   spec-driven-development), section by section: Product -> Tech stack ->
   Architecture principles -> Testing policy -> Code style -> Git & delivery
   workflow. Never re-ask what an already-filled section answers. Keep it
   principles, not prose — this is loaded into every session.
2. Draft the complete document, iterate until I sign off, then write it with
   every [FILL-ME] marker removed and commit on the current branch
   ("docs: fill project constitution").

**Amendment mode** (no markers left — the constitution is standing law):

1. Gather the amendment the same way: interview, draft the exact diff, get my
   sign-off.
2. Never push amendments straight to the default branch. Create branch
   `constitution/<slug>`, commit, verify the diff touches ONLY
   `.sdlc-factory/constitution.md`, and open a pull request with
   `gh pr create` for human approval.

**Either mode, afterwards:** verify the repo's CLAUDE.md contains the plain
text line `@.sdlc-factory/constitution.md` outside any code fence — add it
if missing — and suggest running `sdlc-factory doctor`.

Conduct: this runs in chat (Slack / web UI) — interview in the conversation,
never rely on a terminal-side human, and keep every gh/git call
non-interactive.
