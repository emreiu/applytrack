# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

applytrack (working title) is a multi-user web app for managing, enriching and evaluating job
applications — see `docs/BACKLOG.md` for the full product description. The repository is
currently **planning-only**: there is no application source code yet, only the backlog, the
derived data-model docs, and repo scaffolding (issue template, an empty IntelliJ Java module in
`.idea/`). There is no build tool (no `pom.xml`/`build.gradle`), no test suite, and no lint config
yet, so there are no build/lint/test commands to run today.

`docs/BACKLOG.md` is the source of truth for scope and design decisions; treat it (not this file)
as authoritative if the two ever disagree, and update this file when it drifts.

## Planned stack (from E1 — not yet implemented)

Per `E1-S01` in the backlog, the foundation epic specifies:
- Backend: Maven, Java 21, Spring Boot 3.5, package structure cut by **business domain**, not by
  technical layer (e.g. not `controller`/`service`/`repository` packages).
- Frontend: Angular + TypeScript.
- `docker-compose.yml` for local PostgreSQL; `/actuator/health` health endpoint.
- Flyway migrations under `db/migration`, named `V<nr>__<description>.sql`; migrations are never
  edited after the fact, only added (`E1-S03`).
- Testcontainers-backed integration tests, with unit tests for domain logic and integration tests
  for persistence/API (`E1-S06`).
- CI builds and tests both backend and frontend on every PR; red build blocks merge (`E1-S07`).

Do not assume any of this exists until it's actually been set up — check for `pom.xml` /
`build.gradle` / `package.json` first.

## Technical conventions (backlog §2.5 — apply once code exists)

- Java class/attribute names are English; database objects are English `snake_case` (deliberately
  monolingual, unlike the German docs, since the product may go public).
- Primary keys are `UUID`, never sequential integers.
- Timestamps are `OffsetDateTime`, stored in UTC.
- Every persistent entity carries audit fields: `created_at`, `created_by`, `updated_at`,
  `updated_by` (via `AuditorAware`).
- Money is `BigDecimal` plus a separate currency field — never `double`.
- Enum-like fields are stored as `VARCHAR` with a check constraint, not as numeric ordinals.
- Every entity with a user reference has a mandatory (non-nullable) `user_id` FK (`E3-S01`).

## Core domain architecture

The data model lives in `docs/data-model/` (Mermaid ER diagrams, one file per bounded area,
`00-overview.md` plus `01`–`07`), derived from Part B of `docs/BACKLOG.md`. Read both together —
the backlog explains *why*, the data-model docs give the concrete schema. Recurring decisions to
preserve when extending either:

- **Status is an event log, not a field.** `Application.current_status` is a denormalized read
  value; the real source of truth is the append-only, immutable `ApplicationStatusEvent` chain.
  Corrections happen via a new reversing event, never by editing a past one (`E6-S02`/`E6-S03`).
- **Tenant isolation is enforced, not assumed.** Every business entity is scoped to `user_id`,
  resolved server-side from the security context — never taken from a request parameter. A
  cross-tenant lookup by ID must return `404`, not `403`, so foreign resource existence is never
  leaked (`E3-S01`).
- **Versioned data is referenced by version, not by parent.** `ApplicationDocument` points at a
  specific `DocumentVersion`; a later edit to the document never changes what was actually
  submitted (`E7-S04`).
- **AI output is a proposal until a human accepts it.** `ExtractionJob`/`ExtractionProposal` are
  staging entities; `JobPosting`/`CandidateProfile` records are only created on
  `review_action = ACCEPTED|EDITED`. AI processing is gated behind its own separate, revocable
  consent (`E3-S02`), and the fully manual path must keep working without it (`E8`).
- **`Skill` is the one system-wide (non-user-scoped) entity** in an otherwise per-user model; only
  the *proposal* of a new skill is user-attributed (`E9`, data-model note "Regel B.4-1").
- Constraints Mermaid ER syntax can't express (uniqueness, exclusive-or FKs, check constraints) are
  documented in prose in each data-model file's "Zu beachten" section, destined for a Flyway
  migration, not the diagram.

## Roles

`Gast` (anonymous) / `Bewerber` = `ROLE_USER` (full access to own data only) /
`Administrator` = `ROLE_ADMIN` (platform operation, user management, skill taxonomy, budgets).
Admins are **deliberately excluded from business content** (application data, documents) — this is
a privacy decision enforced technically, not just by convention (backlog §3).

## Explicitly out of scope (backlog §C.5)

Don't design toward: automated application submission, job-posting aggregation/crawling from
portals, success/callback probability prediction, AI-generated application documents,
multi-user collaboration on the same data, native mobile apps.

## Working style: what to implement vs. explain

This is a learning project. I'm using it to deepen my Java/Spring Boot skills alongside my day
job, and I'm deliberately writing significant parts of the backend myself rather than delegating
everything.

- **Infrastructure and boilerplate** (E1, repetitive CRUD scaffolding, config, wiring): implement
  directly, no need to ask first.
- **Core domain logic that encodes a business rule** (e.g. the status transitions in `E6-S02`, the
  tenant-isolation logic in `E3-S01`, the extraction proposal/accept flow in `E8`): do **not**
  write code unprompted. Propose an approach, explain the trade-offs, and wait for explicit
  confirmation before implementing.
- If I ask you to review code I've already written, give direct, concrete feedback — like a code
  review, not unearned praise. Point out what's actually wrong or risky, not just what's fine.
- When unsure which category something falls into, ask rather than guessing.

## Git conventions

- Branch naming: `<type>/<short-description>` (`feat/`, `fix/`, `docs/`, `chore/`, `spike/`).
- GitHub Flow: short-lived branches, merged back to `main` via pull request, deleted after merge.
- Commit messages follow Conventional Commits, written in **English**, regardless of docs/backlog
  being in German.
- Never commit directly to `main`, except for trivial doc-only changes. Always work on a branch
  and open a PR.
- **Be proactive about git hygiene, don't wait to be asked:**
    - When a logical unit of work is complete (a story's acceptance criteria are met, or a natural
      stopping point is reached), suggest committing — don't just leave changes uncommitted and move
      on silently.
    - Always propose a specific Conventional Commits-formatted message with the suggestion; don't
      just say "you should commit" without drafting the message.
    - If a new task doesn't fit the currently checked-out branch's scope, suggest creating a new
      branch (with a name following the convention above) before starting, or switching to an
      existing one if it already covers the work.
    - Flag it if we've drifted onto a branch whose name no longer matches what's actually being
      built, so the branch can be renamed or the work split.

## Backlog & issue conventions

- Story format: `**Als** <Rolle> **möchte ich** <Funktion>, **damit** <Nutzen>.` with checkable
  acceptance criteria; stateful behavior uses Gherkin (*Gegeben/Wenn/Dann*).
- Priority: `M` (must, v1) / `S` (should) / `C` (could). Effort: `S` (≤4h) / `M` (0.5–2d) /
  `L` (3–5d) / `XL` (>5d — split it).
- Story IDs (`E<Epic>-S<Nr>`) are stable and never reused.
- Definition of Done includes: all ACs met, automated tests (unit for domain logic, integration
  for persistence/API), DB changes as a Flyway migration, API documented via OpenAPI, merged via
  PR to `main`.
- New backlog work uses `.github/ISSUE_TEMPLATE/user-story.md`, which expects an Epic id, a
  `E?-S??` backlog id, and the affected entities — cross-reference those against
  `docs/data-model/`.
- Epics build on each other in order (`E1`→`E2`→`E3` before any business epic; see backlog §4 for
  the full milestone/epic dependency table) — don't design later-epic features assuming
  earlier-epic infrastructure (auth, tenant isolation, Flyway, error handling) isn't there yet.