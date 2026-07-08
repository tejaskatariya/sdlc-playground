# Feature artifacts workflow (shared rules)

The chat-driven pre-pipeline phase produces durable feature artifacts, lands
them via their own PR, then hands a feature issue to the FSM. Lifecycle:

    /sdlc-define <feature>  ->  docs-only PR  ->  human merges
    ->  /sdlc-submit <feature>  ->  issue with stage:plan + status:queued
    ->  sdlc-factory start #<n>

## Conventions

- Feature name: kebab-case matching `^[a-z0-9][a-z0-9-]{1,62}$`. Refuse
  anything else and show this rule.
- Artifacts directory: read `artifactsDir` from .sdlc-factory/config.yml
  (default `artifacts`). All files for a feature live in
  `<artifactsDir>/<feature>/`.
- The three artifacts, in authoring order:
  1. `spec.md` — Problem, Goals & non-goals, User stories, Acceptance
     criteria, Constraints, Open questions.
  2. `tech-spec.md` — Architecture overview, Affected areas, Data &
     interfaces, Test strategy, Risks, Decisions. Implementation code lands
     under `generatedCodeDir` from config.yml — say so here.
  3. `tasks.md` — ordered `- [ ]` checklist; every task independently
     committable, TDD-able, with a one-line acceptance hint; together the
     tasks must cover every acceptance criterion in spec.md. If
     `generatedCodeDir` contains no build project yet, task 1 MUST be:
     bootstrap the runnable application skeleton per the constitution's tech
     stack (build files, wrapper, entry point, source/test layout, build
     verified).
- Docs branch: `artifacts/<feature>`, cut from the default branch. (If a
  branch literally named `artifacts` exists, git refuses the name — report
  that instead of working around it.)
- Issue body reference line (machine-parsed by the engine, first line wins):
  `artifacts: <artifactsDir>/<feature>/`
- Artifacts are strictly read-only for pipeline sessions — nothing is ever
  written under the feature's artifacts folder after it merges. Amendments go
  via a new docs PR to the default branch, authored by humans.
- Progress comment: the implement session maintains ONE comment on the issue,
  identified by the hidden marker `<!-- sdlc-factory:progress -->`, holding
  the tasks.md checklist with `[x]` for completed tasks. It is created
  before task 1, updated after every task, and doubles as the crash-resume
  cursor.
- The project constitution (.sdlc-factory/constitution.md) binds every
  artifact: specs and tech-specs must conform to it. Conflicts are resolved
  by amending the constitution first (via /sdlc-constitution), never by
  ignoring it.

## Conduct

- These commands run in chat (Slack / web UI), not necessarily a terminal:
  interview the user in the conversation, one question at a time, and never
  rely on terminal-side interaction. Every `gh`/git call must be
  non-interactive.
- Get explicit sign-off on each artifact before writing and committing it.
- Never re-interview what an existing artifact already answers.
