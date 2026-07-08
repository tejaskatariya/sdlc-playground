---
name: database-and-schema
description: >
  Guides agents through safe schema migration (additive-first, backward compatibility,
  zero-downtime patterns), index strategy, N+1 query detection, and soft-delete vs hard-delete
  trade-offs. Use when designing or modifying a database schema, writing a migration, reviewing a
  query plan, or deciding how to handle data deletion. NOT for ORM configuration or query
  optimisation (use performance-optimization after measuring). NOT for schema migration tooling
  setup — check technical.md for the project's migration tool.
---

# Database and Schema

## Overview

Schema migrations are the most operationally dangerous changes in a typical application deployment. Unlike code changes, schema changes are often difficult to roll back, can block queries during deployment, and can break code running against the old schema during a rolling deploy. This skill enforces an additive-first discipline that prevents the most common migration disasters.

The goal is zero-downtime migration by default, with a documented and approved path for the rare cases where that is not possible.

## When to Use

- Designing a new table or modifying an existing one
- Writing a database migration file
- Reviewing a PR that touches schema or query code
- Deciding between soft-delete and hard-delete for a data type
- Investigating an N+1 query pattern
- **NOT for:** ORM or query framework configuration — check `technical.md`
- **NOT for:** post-measurement query optimisation — use `performance-optimization`

## The Workflow

### Step 1 — Design for backward compatibility first

Before writing any migration, ask: can old code run against the new schema during a rolling deploy?

A rolling deploy means: for some window of time, both old code (v1) and new code (v2) are running simultaneously against the same database. Your schema change must be safe for both.

**Safe (additive):**
- Adding a new nullable column with a default
- Adding a new table
- Adding an index (note: can lock on some DBs — see Step 3)
- Adding a new enum value (some DBs require care — check)
- Renaming a column via two-step (add new, backfill, delete old across multiple releases)

**Unsafe (destructive):**
- Dropping a column that old code reads from
- Renaming a column (old code references the old name)
- Changing a column type in a breaking way
- Adding a NOT NULL constraint to a column that old code does not always set
- Dropping a table that old code references

If the change is destructive, use the expand/contract pattern across multiple releases:

```
Release N:   Add the new structure (expand)
Release N+1: Migrate data; update application code to use new structure
Release N+2: Remove the old structure (contract) — only when old code is no longer deployed
```

Never combine expand and contract in a single migration.

### Step 2 — Write the migration

Structure every migration with:
- **Up migration**: the change to apply
- **Down migration**: how to reverse it (required unless reversal is genuinely impossible)
- **Backward-compatibility analysis**: can the old code run against the new schema?

Document in the migration file or in `artifacts/designs/{feature-slug}/schema.md`:

```sql
-- Migration: add_notification_preference_to_users
-- Date: 2026-05-27
-- Backward compatible: YES — new nullable column; old code ignores it
-- Rollback: ALTER TABLE users DROP COLUMN notification_preference;

ALTER TABLE users
  ADD COLUMN notification_preference VARCHAR(20) DEFAULT 'email' NULL;
```

Flag destructive migrations explicitly. They require `high-risk-path-commit` gate approval.

### Step 3 — Index strategy

Indexes improve read performance and enforce uniqueness, but they have write overhead and (on many DBs) can lock the table during creation.

**When to add an index:**
- A column or column combination appears in a WHERE, JOIN, or ORDER BY clause in a query that will run frequently or on large tables
- A foreign key column (most ORMs do not add these automatically)
- A uniqueness constraint is required

**When NOT to add an index:**
- The table is small (< 10,000 rows) and full-table scans are fast
- The column has very low cardinality (e.g., a boolean) — most query planners will skip the index
- Write throughput is already a bottleneck — each index adds write overhead

**Zero-downtime index creation (PostgreSQL):**

```sql
-- Use CONCURRENTLY to avoid table lock
CREATE INDEX CONCURRENTLY idx_users_email ON users (email);
```

Note: `CREATE INDEX CONCURRENTLY` cannot run inside a transaction block. If your migration tool wraps statements in a transaction, execute index creation separately or disable the transaction for that step.

**Composite indexes:** order matters. Put the highest-cardinality column first, or the column that appears in equality filters (=) before the column in range filters (>, <, BETWEEN).

### Step 4 — N+1 detection

An N+1 query pattern occurs when code executes one query to load a list of N items, then executes N additional queries to load related data — once per item.

**How to detect:**
- Review query logs in development: any query that appears N times with different IDs in a single request cycle is a candidate
- Database query counts in test: if your test framework supports it, assert that a given endpoint executes fewer than N queries for a list of N items
- ORM lazy loading: any navigation property accessed inside a loop (e.g., `order.Customer` in a foreach over orders) is an N+1

**How to fix:**
- Eager loading (JOIN or include in the ORM query)
- Batch loading (DataLoader pattern for APIs, especially GraphQL)
- Denormalisation (if the relationship is read-heavy and write-infrequent)

Document the fix in `artifacts/designs/{feature-slug}/schema.md` if it affects the query plan.

### Step 5 — Soft delete vs hard delete

| Criterion | Soft delete | Hard delete |
|---|---|---|
| Regulatory requirement to permanently delete (GDPR Art 17, right to erasure) | Requires hard delete or full anonymisation | Preferred |
| Audit trail required (need to know something existed) | Preferred | Loses the record |
| Data referenced by other tables (FK constraints) | Soft delete avoids FK violation | Requires cascade or two-phase delete |
| Storage cost is a concern | Accumulates deleted rows — add purge job | Clean deletion |
| Query complexity acceptable | Adds `WHERE deleted_at IS NULL` to every query (use view or global filter) | No overhead |

**Soft-delete implementation requirements:**
- `deleted_at TIMESTAMPTZ NULL` column (not a boolean — records the when)
- Database-level filter or ORM global query filter to exclude soft-deleted rows by default
- Explicit query to access soft-deleted rows (for admin or audit purposes)
- Purge job if storage growth is a concern — purge after retention period

**If GDPR applies and soft-delete is used:**
- Soft delete alone does not satisfy the right to erasure. The record must be anonymised (all PII columns overwritten) or permanently deleted.
- Flag for lens-auditor --mode privacy when implementing a deletion mechanism for personal data.

### Step 6 — Write the schema artefact

Write or update `artifacts/designs/{feature-slug}/schema.md`:

```markdown
## Schema Changes — {feature-slug}

### New / modified tables
| Table | Column | Type | Nullable | Default | Index | Notes |
|---|---|---|---|---|---|---|
| users | notification_preference | VARCHAR(20) | YES | 'email' | — | New column |

### Migration strategy
- Type: Additive-only
- Backward compatible: YES
- Rolling deploy safe: YES
- Rollback: ALTER TABLE users DROP COLUMN notification_preference

### Query plan analysis
- New query: SELECT * FROM users WHERE notification_preference = 'sms'
- Table size: ~50k rows — index on notification_preference recommended (low cardinality — monitor)
- N+1 risk: notifications loaded with users in single JOIN — no N+1

### Deletion strategy (if applicable)
- Soft delete: {yes/no} — deleted_at column; global query filter applied
- GDPR: {yes/no} — anonymisation on deletion required; flag lens-auditor --mode privacy
```

## Common Rationalizations

| Rationalization | Reality |
|---|---|
| "The column rename is small, just do it in one migration" | Old code that reads the old column name breaks immediately. Use the expand/contract pattern. |
| "The NOT NULL constraint is needed, I'll add it directly" | Old code that does not set the column will fail inserts until deployment completes. Add nullable, backfill, then constrain. |
| "We'll add indexes after launch when we know the query patterns" | Missing indexes cause performance incidents on launch day when real data loads. Add what you can predict; add the rest before obvious bottlenecks. |
| "Soft delete is always safer than hard delete" | Soft delete accumulates data and complicates queries. Hard delete is correct for GDPR erasure requests. Choose deliberately. |

## Red Flags

- Migration drops or renames a column without expand/contract across releases
- `NOT NULL` added to a column with no default and no backfill migration
- Index created without `CONCURRENTLY` on a live table (causes lock)
- Navigation property accessed inside a loop in service code (N+1 pattern)
- Soft delete implemented with a boolean `is_deleted` rather than a timestamp
- No down migration — rollback is impossible

## Verification

- [ ] Schema change is additive-only, or expand/contract plan documented across releases
- [ ] Backward compatibility analysis written: can old code run against new schema?
- [ ] Destructive changes require `high-risk-path-commit` gate — flagged in migration
- [ ] Indexes added for FK columns and high-frequency query columns
- [ ] Large-table index creation uses `CONCURRENTLY` (or equivalent for non-Postgres DBs)
- [ ] N+1 patterns checked in service code touching new tables
- [ ] Soft-delete vs hard-delete decision made explicitly and documented
- [ ] GDPR deletion requirement noted and lens-auditor --mode privacy flagged if personal data
- [ ] Schema artefact written to `artifacts/designs/{feature-slug}/schema.md`
