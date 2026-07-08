---
description: Interactively author feature artifacts (spec, tech-spec, tasks) and open the docs PR
argument-hint: <feature-name>
---

Read `.sdlc-factory/prompts/artifacts.md` and follow its conventions for
feature "$ARGUMENTS". This is a staged, resumable flow:

0. **Setup.** Read .sdlc-factory/constitution.md FIRST and ground the whole
   interview in it: never re-ask what it already fixes (tech stack,
   architecture, testing policy), and flag any requirement that conflicts
   with it — the constitution is amended via /sdlc-constitution, not
   overridden by a spec. Validate the feature name against the kebab-case
   rule. Read `artifactsDir` from .sdlc-factory/config.yml. Create or check out branch
   `artifacts/$ARGUMENTS` from the default branch. Inspect
   `<artifactsDir>/$ARGUMENTS/` and RESUME at the first missing artifact —
   for artifacts that already exist, offer revise-or-continue and never
   re-ask what they already answer.

1. **Spec.** Interview me one question at a time (skills: interview-me,
   spec-driven-development): problem, users, goals and non-goals, acceptance
   criteria, constraints. Draft `spec.md`, iterate until I sign off, then
   write it and commit ("docs($ARGUMENTS): add spec").

2. **Tech spec.** Read spec.md and explore the codebase for affected areas.
   Grill me on the design trade-offs (skills: system-architecture, grill-me):
   propose options, I choose. Draft `tech-spec.md`, sign-off, write, commit.

3. **Tasks.** Break the work down (skill: planning-and-task-breakdown) into
   an ordered `- [ ]` checklist per the conventions; if `generatedCodeDir`
   has no build project yet, task 1 must be the app-skeleton bootstrap (see
   the conventions). Verify every acceptance criterion is covered. Sign-off,
   write `tasks.md`, commit.

4. **Docs PR.** Push the branch and verify
   `git diff <default-branch>...HEAD --name-only` touches ONLY
   `<artifactsDir>/$ARGUMENTS/` — stop and tell me if not. Open the PR with
   `gh pr create` titled "Artifacts: $ARGUMENTS". Then remind me: review and
   merge the PR, and run `/sdlc-submit $ARGUMENTS` to create the issue.
