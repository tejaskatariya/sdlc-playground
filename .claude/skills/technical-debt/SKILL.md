---
name: technical-debt
description: >
  Identifies, quantifies, and prioritises technical debt across four categories: architecture,
  dependency, test, and documentation debt. Produces a debt register with severity, effort-to-fix,
  and a prioritisation matrix. Guides the decision of when to pay down debt vs accept it.
  Use when a feature retro surfaces debt, when onboarding to a codebase, when planning a
  refactor sprint, or when the team is consistently slowed by a known structural problem.
  NOT for sprint-level estimation (use the estimation skill) or for security hardening
  (use the security-and-hardening skill).
---

# Technical Debt

## Overview

Technical debt is not a vague feeling that the code is messy. It is a specific, enumerable set of structural decisions that are now costing more than they save. This skill makes debt legible — named, measured, and prioritised — so the team can make deliberate trade-offs rather than absorbing hidden drag indefinitely.

The output is a debt register: a living artefact that tracks what is owed, what it costs, and when to pay it back.

## When to Use

- After a retro surfaces repeated slowdowns in the same code area
- When onboarding to an unfamiliar codebase and needing a structural map
- Before planning a refactor sprint or an architecture migration
- When a feature estimate is inflated because of known structural friction
- **NOT for:** sprint-level task estimation (use the estimation skill); security vulnerability triage (use the security-and-hardening skill); dependency vulnerability scanning (that is a CI/CD concern)

## The Four Debt Categories

### Category 1 — Architecture debt

Structural decisions that now limit the system's ability to change. Examples: a monolith that was not designed for the load it now carries; a shared DB across services that prevents independent deploys; circular dependencies between modules.

Detection signals: every feature touches the same three files; a change in one service requires coordinated releases with two others; local reasoning is impossible without reading four layers.

### Category 2 — Dependency debt

Libraries, frameworks, or platforms that are outdated, unsupported, or mismatched for current requirements. Examples: a major version behind on a framework; a transitive dependency with a known vulnerability; a library that is no longer maintained.

Detection signals: `dotnet outdated` or `npm audit` warnings; upgrade PRs that are perpetually deferred; code that works around a known bug in an old library version.

### Category 3 — Test debt

Gaps in the test suite that make refactoring expensive or risky. Examples: untested business-critical paths; tests that are green but do not assert anything meaningful; no integration tests for a DB layer that has changed three times.

Detection signals: fear of changing a specific file; bugs that reach production in areas with existing tests; test suite takes 20 minutes but covers less than 40% of critical paths.

### Category 4 — Documentation debt

Missing or stale context that slows onboarding and decision-making. Examples: an ADR that was written but never updated after the decision changed; a README that describes a setup process that no longer works; no context doc for a critical service.

Detection signals: questions that come up repeatedly in code review; new engineers take more than a day to get a feature running locally; decisions revisited because no one can find the original rationale.

## The Workflow

### Step 1 — Enumerate the debt

For each of the four categories, list every known debt item. Source from:
- Retro action items marked "tech debt"
- Code comments containing `TODO`, `FIXME`, `HACK`, or `DEBT`
- Team memory (explicit: ask; do not assume)
- Codebase scan for detection signals above

Format each item as:

```
ID:          debt-{category-short}-{seq}  (e.g. debt-arch-001)
Category:    architecture | dependency | test | documentation
Description: One sentence — what the debt is
Location:    File path, module, or service name
Introduced:  Date or "unknown"
Cost signal: How is this debt currently manifesting? (e.g. "adds 2h to every DB migration PR")
```

### Step 2 — Score each item

Score every debt item on two dimensions:

**Risk** — what happens if this debt is not paid?

| Score | Meaning |
|---|---|
| 1 | Cosmetic — no measurable impact on velocity or reliability |
| 2 | Friction — slows specific tasks; tolerable for now |
| 3 | Drag — consistently slows features in this area; growing |
| 4 | Blocking — prevents a class of change; or carries reliability risk |
| 5 | Critical — active reliability impact or blocking a committed deliverable |

**Effort** — what does it cost to fix?

| Score | Meaning |
|---|---|
| 1 | Hours — one engineer, one PR |
| 2 | Days — one engineer, less than a week |
| 3 | Weeks — one or two engineers, one sprint |
| 4 | Quarter — team-level effort, careful sequencing required |
| 5 | Multi-quarter — programme-level; do not start without explicit budget |

### Step 3 — Prioritisation matrix

Plot items on a Risk × Effort grid:

```
         Effort →
         1    2    3    4    5
Risk  5 | P1  P1  P1  P2  P2
↓     4 | P1  P1  P2  P2  P3
      3 | P1  P2  P2  P3  defer
      2 | P2  P2  P3  defer defer
      1 | P3  defer defer defer accept
```

- **P1 (pay now):** High risk, manageable effort. Schedule in next sprint.
- **P2 (pay soon):** Escalating cost or moderate risk. Schedule within quarter.
- **P3 (track):** Low risk or high effort. Keep in register; revisit each quarter.
- **defer:** Not worth scheduling until risk score rises.
- **accept:** Formally accept: note that this is a deliberate choice, not an oversight.

### Step 4 — Remediation planning

For each P1 and P2 item, write a remediation entry:

```markdown
### debt-arch-001 — {description}

**Approach:** {specific steps to fix — not vague ("refactor") but concrete ("extract NotificationService from OrderService, remove the shared DB call in line 247")}
**Prerequisite:** {What must be done first? e.g. "test coverage for NotificationService must reach 80% before extraction"}
**Sequencing risk:** {What breaks if this is done wrong? What is the safe order?}
**Owner:** {Role — not individual name}
**Estimated effort:** {from Step 2 score, translated to calendar time}
```

### Step 5 — Write the debt register

Write to `artifacts/tech-debt/{engagement-slug}-debt-register.md`:

```markdown
# Debt Register — {engagement-slug}

Last updated: {date}

## Summary

| Category | Items | P1 | P2 | P3 | Accepted |
|---|---|---|---|---|---|
| Architecture | N | N | N | N | N |
| Dependency | N | N | N | N | N |
| Test | N | N | N | N | N |
| Documentation | N | N | N | N | N |

## Debt items

{Full table with ID, Category, Description, Risk, Effort, Priority, Status}

## Remediation plans

{P1 and P2 remediation entries from Step 4}

## Accepted debt

{Items formally accepted with rationale — "we know; we choose not to fix it now; revisit if risk score rises"}
```

## When to pay down vs accept

Pay down debt **now** when:
- Risk score is 4 or 5 and effort is 1–3 (P1)
- The next feature in the roadmap lands directly in the debt area
- A new engineer is joining and the debt will crater their onboarding

Accept debt **deliberately** when:
- The system is being replaced within the quarter
- The cost of paying it down exceeds the cost of the remaining drag before replacement
- Risk score is 1 and effort is 3+ (no meaningful benefit)

Never accept debt **silently** — a debt item with no priority decision is not accepted, it is invisible. Invisible debt is the kind that causes incidents.

## Common Rationalizations

| Rationalization | Reality |
|---|---|
| "We'll pay it down after the next release" | The next release adds more debt. Schedule it or it does not happen. |
| "It's too hard to estimate the effort" | Score it 4 or 5 and note the uncertainty. An imprecise score is better than no score. |
| "It's not that bad — we work around it" | Working around it is the cost. That cost accumulates every sprint. |
| "We accept this debt" (with no formal record) | Acceptance without a record is not acceptance. It is avoidance. |
| "This is a documentation problem, not real debt" | Documentation debt has a measurable cost: onboarding time, repeated questions, revisited decisions. |

## Red Flags

- Debt register not updated after a retro that surfaced a structural slowdown
- All debt items marked P3 (no P1s) — either the codebase is unusually healthy or the scoring is too lenient
- P1 items present with no remediation plan and no scheduled sprint slot
- "Accepted" debt with no rationale — accepted by whom, when, and why?
- Test debt score of 1 in an area that has shipped multiple regressions

## Verification

- [ ] All four categories enumerated; none silently skipped
- [ ] Every debt item has a Risk score and an Effort score
- [ ] Every item has an explicit priority decision (P1/P2/P3/defer/accept)
- [ ] P1 and P2 items have remediation plans with owners and estimated effort
- [ ] Accepted items have a written rationale
- [ ] Debt register written to `artifacts/tech-debt/{engagement-slug}-debt-register.md`
- [ ] Register reviewed with team (not just produced by the agent alone)
