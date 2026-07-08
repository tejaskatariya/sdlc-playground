---
name: estimation
description: >
  Produces effort estimates for implementation plans: T-shirt sizing, phase-level story points,
  dependency mapping, and risk-adjusted totals. Tracks estimation accuracy over time via retro
  feedback to improve future estimates. Use when a plan is being written and needs an effort
  estimate before eng-manager approval. NOT for capacity planning across multiple features
  (use the capacity-plan skill when capacity planning across features is needed) or for project scheduling.
---

# Estimation

## Overview

Estimates are not commitments — they are probability distributions with explicit uncertainty. This skill produces estimates that are grounded in the actual plan (not vibes), carry visible confidence levels, and feed back into a learning loop so accuracy improves across engagements.

The skill plugs into `plan-mode`: the estimator agent calls it after a plan's phases are defined, before the plan is submitted for eng-manager approval.

## When to Use

- After a phased implementation plan exists (`artifacts/plans/{feature-slug}.md`)
- Before the plan approval HIL gate fires (the estimate is part of the plan artefact)
- When a plan is revised and the estimate needs updating
- **NOT for:** estimating a vague idea before a plan exists (write the plan first); not for sprint capacity planning across features; not a substitute for reading the actual codebase

## The Workflow

### Step 1 — Read the plan

Read `artifacts/plans/{feature-slug}.md`. For each phase:
- What is being changed?
- Which files/layers are touched?
- What are the dependencies (other phases, other features, external systems)?
- What is the acceptance criterion for this phase being done?

If the plan is too vague to estimate (phases not broken down, ACs absent), stop and ask the plan author to refine.

### Step 2 — Read historical estimates (if available)

Check `artifacts/retros/` for past retros that include estimation accuracy data. If any features in the same domain have been shipped before, note the actual vs estimated ratio:

```
Historical accuracy for this tech area: estimated Xd → actual Yd (ratio: Z)
Apply a {Z}x adjustment factor to raw estimates below.
```

If no history exists: note that estimates carry higher uncertainty and widen confidence intervals.

### Step 3 — Estimate each phase

For each plan phase, apply a two-pass approach:

**Pass 1 — T-shirt size**

| Size | Description | Typical range |
|---|---|---|
| XS | Single function/method change, no new dependencies | < 2h |
| S | One component, clear implementation path, well-tested area | 2–4h |
| M | Multiple components or a new integration; some unknowns | 0.5–1d |
| L | Cross-layer change, new external dependency, or unfamiliar area | 1–3d |
| XL | Significant architecture change, multiple unknowns, or new domain | 3–5d |
| XXL | Uncertainty too high to estimate; break down further | n/a — must split |

**Pass 2 — Story points (Fibonacci)**

Translate T-shirt size to story points using the team's calibration (default below; override in `engagement.yaml`):

```
XS → 1    S → 2    M → 3    L → 5    XL → 8    XXL → requires breakdown
```

**Confidence level**

For each phase estimate, assign a confidence level:

| Confidence | Condition |
|---|---|
| High (±20%) | Well-understood area, similar work done before, all inputs clear |
| Medium (±40%) | Some unknowns; similar work done in a different context |
| Low (±60%) | New area, unclear dependencies, or design not fully settled |

### Step 4 — Risk-adjust the total

Sum raw estimates. Apply adjustments:

| Risk factor | Adjustment |
|---|---|
| External dependency (third-party API, another team's work) | +20% per dependency |
| Unfamiliar codebase area | +30% |
| Schema migration involved | +25% |
| No existing tests in the area | +20% |
| First time this pattern is used in this engagement | +15% |
| Historical under-estimation ratio > 1.5x | +25% |

Cap total risk adjustment at +80% (if higher, the feature probably needs re-scoping).

### Step 5 — Write the estimate block

Append to `artifacts/plans/{feature-slug}.md`:

```markdown
## Estimation

| Phase | T-shirt | Points | Confidence | Notes |
|---|---|---|---|---|
| 1 — {name} | M | 3 | High | Well-understood area |
| 2 — {name} | L | 5 | Medium | New external API |
| 3 — {name} | S | 2 | High | Standard CRUD |

**Raw total:** {N} points  
**Risk adjustments:** +{X}% (external API dependency, unfamiliar area)  
**Risk-adjusted total:** {N} points (~{low}–{high} points at ±{confidence}%)  
**Calendar estimate:** {N}–{M} days at {assumed velocity} points/day  
**Historical accuracy factor:** {ratio or "no history — first feature in this domain"}  

### Dependencies
- Phase 2 blocked by: {external API credentials from client — ETA: {date}}
- Phase 3 blocked by: Phase 2 merge

### Risks flagged
- {Risk 1 and its mitigation}
- {Risk 2}
```

### Step 6 — Flag XXL phases

If any phase was sized XXL, stop and flag it before submitting the plan:

```
⚠️ Estimation blocked — Phase {N} is too large to estimate reliably.
Reason: {why it's XXL}
Action required: Break Phase {N} into sub-phases, or run /design again to resolve the unknowns.
```

Do not proceed with an XXL phase in the plan. It is a planning defect.

## Patterns

### Pattern: Anchoring from a similar past feature

If a similar feature was shipped before:

```
Comparable feature: {feature-slug} — estimated {N}pts, actual {M}pts (ratio: {M/N})
Applying {M/N}x adjustment to this estimate.
Baseline (unadjusted): {N}pts → Adjusted: {N * M/N}pts
```

### Pattern: Splitting an XXL phase

Ask these questions to split:
1. Can we ship partial value (Phase A without Phase B)?
2. Is there a seam at a layer boundary (DB vs service vs API vs UI)?
3. Is there a seam at a dependency boundary (internal work vs external integration)?

An XXL phase almost always splits at one of these three seams.

## Common Rationalizations

| Rationalization | Reality |
|---|---|
| "I'll estimate after I've looked at the code more" | The estimate is what forces you to look at the code. Do it now, update if wrong. |
| "It's impossible to estimate this" | It is possible to estimate uncertainty. Name the confidence interval and the unknowns. That *is* the estimate. |
| "Points don't map to days so why bother" | The ratio of estimates to actuals over time is the calibration data. Without consistent units, there is no learning loop. |
| "Risk adjustments are just padding" | They are documented assumptions. Undocumented assumptions are what cause surprises. |
| "Phase 3 is an XXL but I'll just say L and we'll see" | An underestimated XXL is a commitment you cannot keep. Flag it; split it; or explicitly accept the uncertainty. |

## Red Flags

- Estimate has no confidence level
- All phases are sized M (suspiciously uniform — real work has variance)
- XXL phase in the plan without a flag or breakdown
- No dependency map (dependencies are the most common source of actual-vs-estimate divergence)
- Estimate written without reading the existing codebase or design artefacts

## Verification

- [ ] Every plan phase has a T-shirt size, story points, and confidence level
- [ ] Risk adjustments are itemised (not a single unexplained multiplier)
- [ ] All XXL phases are flagged and blocked from proceeding
- [ ] Dependencies enumerated with owners and ETAs where known
- [ ] Estimate block appended to `artifacts/plans/{feature-slug}.md`
- [ ] Historical accuracy factor noted (or noted as "no history")
