---
description: Create the FSM feature issue for merged artifacts
argument-hint: <feature-name>
---

Read `.sdlc-factory/prompts/artifacts.md`, then create the feature issue
for "$ARGUMENTS":

1. Read `artifactsDir` from .sdlc-factory/config.yml. Verify spec.md,
   tech-spec.md and tasks.md exist ON THE DEFAULT BRANCH via
   `gh api repos/{owner}/{repo}/contents/<artifactsDir>/$ARGUMENTS/<file>?ref=<default-branch>`.
   If any is missing, check `gh pr list --head artifacts/$ARGUMENTS` and
   tell me whether the artifacts PR is still open — then STOP.

2. Compose the issue: title from the feature; body with a one-paragraph
   summary taken from spec.md, links to the three files on the default
   branch, the machine line `artifacts: <artifactsDir>/$ARGUMENTS/`, and
   any `depends-on: #N` lines I name.

3. Show me the draft FIRST. Only after I approve, create it with
   `gh issue create --label "stage:plan" --label "status:queued"`.

4. Report the issue number and the hand-off command:
   `sdlc-factory start #<n>`.
