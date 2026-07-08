---
name: system-architecture
description: >
  Guides agents through the architectural decision process: context analysis, option generation,
  trade-off evaluation, fitness function definition, and ADR writing. Use when a significant
  architectural choice must be made — technology selection, service decomposition, data model
  strategy, integration pattern. Also covers when to write an ADR vs when a code comment suffices.
  References skills/process/documentation-and-adrs/SKILL.md for ADR lifecycle rules. NOT for
  detailed API design (use api-and-interface-design) or schema design (use database-and-schema).
---

# System Architecture

## Overview

Architecture decisions made without a structured process tend to default to familiarity ("we've always used X") or recency bias ("I just read about Y"). This skill replaces instinct with a repeatable process: understand the context, generate real alternatives, evaluate trade-offs against explicit criteria, choose, and record the decision in a way that lets future engineers understand why — not just what.

The most important architectural skill is knowing when you are making an architectural decision at all. Many decisions that feel like implementation details are actually architecture (and vice versa). This skill includes that diagnostic.

## When to Use

- Choosing between two or more non-trivially different technical approaches (database engine, message broker, caching strategy, auth architecture, service decomposition boundary)
- A decision whose consequences will be costly to reverse (data model choices, API contracts, infrastructure dependencies)
- A new pattern is being introduced into the codebase that does not follow existing conventions
- A performance, security, or compliance constraint forces a specific architecture
- **NOT for:** API endpoint design (use `api-and-interface-design`)
- **NOT for:** schema column and index design (use `database-and-schema`)
- **NOT for:** decisions that are easily reversed and have no cross-cutting consequences (write a code comment, not an ADR)

## Is This an Architectural Decision?

Before starting the process, confirm this is an architectural decision. An architectural decision:

- Has long-lived consequences (months or years, not sprint-to-sprint)
- Affects multiple components, teams, or system properties simultaneously
- Is costly or disruptive to reverse
- Constrains or enables a class of future decisions

If none of these apply, write a code comment, a short PR description, or a TODO — not an ADR. ADRs have a maintenance cost; reserve them for decisions that warrant it.

Rule of thumb: "Could a senior engineer who joins the team in 12 months make a significantly different decision without knowing this choice was made?" If yes, write an ADR.

## The Workflow

### Step 1 — Establish context

Write the context section before generating any options. The context must answer:

1. **What situation or constraint prompted this decision?** (Not what you want to build — what forced the decision.)
2. **What qualities matter most here?** — List 3–5 architectural qualities in priority order (e.g., consistency > availability; security > developer experience; low operational complexity > performance).
3. **What are the constraints?** — Budget, team expertise, existing tech stack, regulatory requirements, existing contracts.
4. **What is the impact of getting this wrong?** — This calibrates how much analysis is warranted.

Poor context example: "We need to choose a caching layer."
Good context example: "We need to cache the results of ML inference calls that cost $0.04/call and take 2s at p95. Cache lifetime must not exceed the model's staleness guarantee (24h). The team has no operational experience with Redis. The service will run on AWS. Cache misses are acceptable but cache-induced data staleness is not."

### Step 2 — Generate options

List at least three options. Include:
- The obvious choice (the one everyone will suggest first)
- The status quo / do-nothing option (even if it seems obviously inadequate — explain why you ruled it out)
- At least one alternative that is genuinely different in approach, not just a variation of the obvious choice

For each option, write one paragraph: what it is, how it works in this context, and its primary advantage.

Do not yet argue for or against. Just describe.

### Step 3 — Evaluate against architectural qualities

Use the qualities you prioritised in Step 1 as your evaluation criteria. Build a trade-off table:

```markdown
| Option | Consistency | Operability | Dev experience | Cost | Security |
|---|---|---|---|---|---|
| Option A | High | High | Medium | High | High |
| Option B | Medium | Low | High | Low | High |
| Option C | High | Medium | Medium | Medium | High |
```

Weight the columns by the priority order you established in Step 1. The highest-weighted column should drive the decision unless another column has a disqualifying result.

### Step 4 — Define fitness functions

A fitness function is a measurable criterion that tells you whether the chosen architecture continues to satisfy its requirements over time. Define at least two:

```
Fitness functions for this decision:
1. Cache hit rate must remain > 80% under normal load (measured: p95 latency of inference endpoint)
2. No cache staleness violation: any cached value must be invalidated within 24h of model update
   (measured: time between model deployment and cache-busting event)
```

Fitness functions are the basis for architecture monitoring. Without them, you cannot detect when the architecture starts to drift from its intended properties.

### Step 5 — Write the ADR

Follow the ADR format from `skills/process/documentation-and-adrs/SKILL.md`. At minimum:

```markdown
# ADR: {title}

- **Date:** {ISO date}
- **Status:** proposed → accepted → superseded
- **Deciders:** {roles, not names}

## Context
{Step 1 output}

## Decision
{What are we doing? Concrete — not "use a cache" but "use AWS ElastiCache (Redis 7) with a 6-hour TTL,
LRU eviction, and a cache-aside pattern implemented in the InferenceService layer."}

## Alternatives considered
| Alternative | Reason rejected |
|---|---|
| {Option B} | {Specific reason — references trade-off table} |
| {Option C} | {Specific reason} |
| Do nothing | {Why status quo is not acceptable} |

## Consequences
- **Good:** {concrete improvements}
- **Bad / risks:** {concrete risks — do not write "complexity may increase"}
- **Technical debt created:** {if any, with remediation plan}

## Fitness functions
1. {Measurable criterion 1}
2. {Measurable criterion 2}

## Implementation constraints
{What the implementing engineer must not deviate from}
```

Write to `artifacts/designs/{feature-slug}/adr.md`.

### Step 6 — When to write a code comment vs an ADR

| Write an ADR | Write a code comment |
|---|---|
| Decision will persist for months or years | Decision is scoped to a single function or module |
| Multiple teams are affected | Only the current feature is affected |
| Reverting would require coordinated migration | Reverting is a one-commit change |
| The decision constrains future options | The decision is independent |
| A future engineer would reasonably make a different choice | The choice is obvious given the code context |

## Patterns

### Pattern: The strangler-fig boundary

When decomposing a monolith or refactoring an existing architecture, define the strangler-fig boundary in the ADR:

- What is the old system's scope that is being replaced?
- What is the new system's scope?
- Where do they co-exist, and what is the co-existence contract?
- When does the old system stop receiving new traffic (the strangling event)?

### Pattern: Architecture Decision Review Board (for high-impact decisions)

For decisions with very high reversal cost, add a peer-review step before marking the ADR "accepted":
- Share the ADR draft with at least one senior engineer outside the feature team
- Incorporate feedback
- Mark as "accepted" only after review

Document the reviewers (by role) in the ADR's "Deciders" field.

## Common Rationalizations

| Rationalization | Reality |
|---|---|
| "Everyone knows why we chose this, no need to document" | The person who knows why will leave. The ADR stays. |
| "We'll try this and change it if it doesn't work" | Some architectural choices cannot be changed without a migration. Know which ones before you commit. |
| "I listed two options — the one we chose and the one we rejected" | Two options means you didn't look hard enough. The best alternative is almost never the first one you reject. |
| "This is just an implementation detail" | If you are uncertain whether it is an implementation detail, it is an architectural decision. |

## Red Flags

- ADR has only one alternative listed (the chosen option)
- ADR context section explains what was built, not why it was forced upon you
- Architectural decision made in a PR description with no ADR
- Fitness functions absent — no way to know if the architecture is still working
- ADR status is "proposed" for more than 30 days without resolution

## Verification

- [ ] Context section answers: what forced this decision, what qualities matter, what are the constraints
- [ ] At least three options generated (including status quo)
- [ ] Trade-off table completed against named architectural qualities
- [ ] Fitness functions defined (at least two, both measurable)
- [ ] ADR written using standard format and saved to `artifacts/designs/{feature-slug}/adr.md`
- [ ] Decision vs code-comment threshold evaluated — ADR is warranted for this decision
- [ ] HIL gate `approval-required` raised for design sign-off

## References

- `skills/process/documentation-and-adrs/SKILL.md` — ADR lifecycle rules, when to update vs supersede, drift resolution
