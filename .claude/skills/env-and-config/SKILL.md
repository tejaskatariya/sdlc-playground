---
name: env-and-config
description: >
  Guides agents through 12-factor configuration discipline (env vars not in code), secrets
  management, feature flag lifecycle, environment parity (dev/staging/prod), and IaC conventions.
  Use when introducing new configuration, secrets, feature flags, or environment-specific values.
  Use when reviewing a PR that touches config files, env var usage, or infrastructure code.
  NOT for application-level configuration objects or settings screens — those are product features.
---

# Environment and Configuration

## Overview

Configuration mistakes — hardcoded secrets, environment-specific code paths, missing env vars in production, feature flags left permanently on — are among the most common sources of production incidents. The 12-factor app methodology provides a clean model: configuration belongs in the environment, not in the code. This skill operationalises that model with concrete checks and patterns for the Caizin pipeline.

## When to Use

- Introducing a new configuration value, secret, or feature flag
- Adding environment-specific behaviour to application code
- Reviewing a PR that adds or modifies `.env`, config files, or IaC code
- Preparing a release that requires new environment variables in staging or production
- Auditing feature flags that may have been forgotten after their launch window
- **NOT for:** product settings or user-configurable preferences (those are data, not config)
- **NOT for:** application startup configuration objects (e.g., dependency injection containers) — those follow application architecture patterns

## The 12-Factor Config Rules

### Rule 1 — Config in the environment, not in code

Config is anything that varies between deployments (dev, staging, prod). It should never appear in source code.

**Forbidden in source code:**
- Database connection strings
- API keys, secrets, credentials of any kind
- Environment-specific URLs (staging API endpoint, feature server address)
- Third-party service credentials
- Encryption keys or salts

**Required pattern:**
```
# Code reads from environment
connectionString = Environment.GetEnvironmentVariable("DATABASE_URL")

# .env.example committed to source (no real values)
DATABASE_URL=postgres://user:password@host:5432/dbname

# Actual .env files — in .gitignore, never committed
```

If a value varies between environments, it is config. If it is the same in all environments, it can be a constant in code.

### Rule 2 — Secrets management hierarchy

| Secret type | Where to store |
|---|---|
| Database credentials | Secrets manager (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault) |
| API keys | Secrets manager |
| Encryption keys | Key management service (AWS KMS, Azure Key Vault) — never in secrets manager |
| Service-to-service tokens | Short-lived tokens from identity service (not long-lived static secrets) |
| Local development secrets | `.env` file (gitignored) — generate from `.env.example` |

Never store secrets in:
- Source code (any language)
- Version control (even encrypted, unless the encryption key is managed separately)
- Log files
- Error messages returned to clients
- Database rows in plaintext (hash passwords; encrypt PII at rest)

When reviewing a PR: check for hardcoded credentials using pattern matching (`password=`, `api_key=`, `secret=`, connection string patterns, JWT secrets, base64-encoded strings in config).

### Rule 3 — Feature flag discipline

Feature flags enable safe deployments by decoupling release from deployment. But they accumulate as tech debt if not managed.

**Feature flag lifecycle:**

```
1. Create flag before the feature is merged (always-off by default)
2. Deploy with flag off → feature is inert in production
3. Enable flag for internal testing → validate in production environment
4. Gradual rollout (percentage-based if flag system supports it)
5. Full rollout → monitor for regressions
6. Remove flag from code and config within one sprint of full rollout
```

Rules for flags:
- Every flag must have an owner (the feature team)
- Every flag must have a planned removal date (or a condition: "remove after X% rollout for 2 weeks")
- Flags must not gate other flags (cascading flags are maintenance nightmares)
- Long-lived operational flags (kill switches, rate limiters) are not feature flags — document them as operational config

Audit flags in `engagement.yaml: feature_flags` if present, or in the flag management service. Any flag older than 60 days post-launch that is permanently enabled should be removed.

### Rule 4 — Environment parity

Dev, staging, and prod should be as similar as possible. Differences that are acceptable:

- Secrets (different credentials per environment — required)
- Scale (prod has more replicas — acceptable)
- DNS and endpoints (dev points to local services — acceptable)

Differences that cause incidents:

- Different database engines (SQLite in dev, Postgres in prod — behaviour diverges)
- Different OS or runtime versions
- Feature flags enabled in staging but not prod (or vice versa) without documentation
- Different dependency versions between environments

Check:
- Is the same Docker image (or equivalent runtime) used in staging and prod?
- Are environment-specific code paths guarded by config, not by `if (env == "development")`?
- Does the staging environment have the same schema as prod (minus data)?

### Rule 5 — IaC conventions

Infrastructure as Code follows the same principles as application code:
- Config in variables, not hardcoded in IaC templates
- Secrets referenced from secrets manager, not hardcoded in IaC
- IaC changes reviewed like code changes (they are high-risk-path changes by definition)
- Environments defined as separate IaC configurations (or parameterised by environment), not copy-pasted with manual edits

Flag any IaC file that contains hardcoded credentials, environment-specific magic values, or resource names that should come from variables.

## Workflow

### Step 1 — Inventory the new config

For every new configuration value introduced in a PR or design:

| Key | Type | Secret? | Varies by env? | Where stored | Default |
|---|---|---|---|---|---|
| `DATABASE_URL` | Connection string | Yes | Yes | Secrets manager | (none — required) |
| `CACHE_TTL_SECONDS` | Integer | No | No | `.env.example` / code constant | 3600 |
| `FEATURE_NEW_DASHBOARD` | Boolean | No | Yes | Feature flag service | false |

### Step 2 — Check for violations

- Any secret stored in source code or config file? → Block
- Any hardcoded environment-specific URL in code? → Block
- Any `if (env == "staging")` code path? → Flag — should be config-driven
- Any feature flag without an owner or removal plan? → Flag
- Any new env var not documented in `.env.example`? → Block

### Step 3 — Write the config checklist

Document new config in `artifacts/designs/{feature-slug}/config.md`:

```markdown
## Configuration — {feature-slug}

### New environment variables
| Key | Required | Secret | Default | Notes |
|---|---|---|---|---|
| DATABASE_URL | Yes | Yes | — | Add to staging and prod secrets manager |
| CACHE_TTL_SECONDS | No | No | 3600 | Add to .env.example |

### Feature flags
| Flag | Owner | Default | Removal condition |
|---|---|---|---|
| FEATURE_NEW_DASHBOARD | {team} | false | Remove 2 weeks after 100% rollout |

### Pre-deployment checklist
- [ ] All new secrets added to staging secrets manager
- [ ] All new secrets added to prod secrets manager (requires release-manager approval)
- [ ] .env.example updated with new non-secret vars
- [ ] Staging deployment verified with new config
- [ ] Feature flags set to correct default for release
```

## Common Rationalizations

| Rationalization | Reality |
|---|---|
| "It's just a dev secret, safe to commit" | Dev secrets become prod secrets when someone copies the config. Never commit secrets. |
| "We'll clean up the feature flag after launch" | "After launch" means never. Set the removal date before merging the flag. |
| "Staging uses SQLite, prod uses Postgres — it's fine" | SQLite and Postgres have different behaviours on constraints, transactions, and string handling. What works in dev may fail in prod. |
| "IaC is reviewed by the infra team, no need for the same standards" | IaC changes are high-risk-path changes. They require the same gate process as schema migrations. |

## Red Flags

- Any credential, API key, or connection string in source code or a committed config file
- `if (environment == "production")` logic in application code — should be a config value
- Feature flag that has been enabled for more than 90 days with no removal plan
- `.env` file committed to source control (even with fake values)
- New env var not documented in `.env.example`
- IaC resource with a hardcoded connection string or credential

## Verification

- [ ] Every new secret stored in secrets manager (not in code or `.env`)
- [ ] `.env.example` updated with all new non-secret environment variables
- [ ] Feature flags have owner and removal date recorded in config.md
- [ ] No environment-specific code paths (config-driven, not if/else on env name)
- [ ] Environment parity confirmed: same runtime, same DB engine, same schema
- [ ] IaC changes routed through `high-risk-path-commit` gate
- [ ] Config artefact written to `artifacts/designs/{feature-slug}/config.md`
- [ ] Pre-deployment secrets checklist complete before release
