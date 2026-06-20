# AGENTS.md

This file gives coding agents project-specific context and guardrails for this repository.

## Project Overview

This is a steel quality control system for inspection entry, automatic quality judgment, judgment explanation, reinspection, rejudgment, concession acceptance, quality certificate data, statistics, and permission audit.

Current stack:

- Backend: Java 8, Spring Boot 2.7.18, MyBatis-Plus, MySQL 5.7, Redis, Sa-Token, Knife4j, Lombok.
- Frontend: Vue 3, TypeScript, Vite, Vue Router 4, Pinia, Axios, Element Plus, ECharts.
- Database: MySQL schema and seed scripts live under `backend/scripts/`.
- OpenSpec: project specs live under `openspec/`; use OpenSpec changes for substantial feature work.

## Repository Layout

```text
backend/
  pom.xml
  scripts/
    init-schema.sql
    init-dict-data.sql
    init-test-data.sql
    init-menu-rbac.sql
    migrations/
  src/main/java/com/jhict/quality/
    common/       shared config, API result, exceptions, audit, utilities
    controller/   REST controllers
    dto/          request DTOs
    engine/       quality judgment engine and matching logic
    entity/       database entities
    enums/        business enums
    mapper/       MyBatis-Plus mappers
    scheduler/    scheduled jobs
    service/      api interfaces and implementations
    vo/           response view objects
  src/main/resources/
    application.yml
    application-dev.yml
    logback-spring.xml

frontend/
  package.json
  src/
    api/          HTTP API wrappers
    components/   reusable Vue components
    composables/  shared composition utilities
    directives/   custom directives
    layouts/      layout components
    router/       static/dynamic route setup
    store/        Pinia stores
    styles/       global styles
    types/        shared frontend types
    views/        page modules

openspec/
  config.yaml
  specs/
  changes/
```

## Local Development

Initial setup is documented in `README.md`.

Typical run flow:

```bash
# terminal 1: create SSH tunnel when needed
ssh -L 3307:127.0.0.1:3306 -L 6380:127.0.0.1:6379 -N -f ubuntu@<remote-server-ip>

# terminal 2: backend
cd backend
mvn spring-boot:run -Dspring.profiles.active=dev

# terminal 3: frontend
cd frontend
npm run dev
```

Default local URLs:

- Frontend: `http://localhost:5173`
- API docs: `http://localhost:8080/doc.html`
- Default account: `admin / Admin123456`

Useful commands:

```bash
# backend compile/test
cd backend && mvn test
cd backend && mvn -DskipTests package

# frontend checks/build
cd frontend && npm run build

# OpenSpec
openspec list --json
openspec validate --strict
```

## Backend Guidelines

- Keep Java 8 compatibility.
- Follow existing package boundaries. Add new controllers, services, DTOs, entities, mappers, and VOs in the established packages.
- Prefer MyBatis-Plus wrappers and existing mapper patterns. Remember MySQL 5.7 does not support CTEs or window functions.
- Keep judgment logic deterministic. AI features may explain or advise, but must not silently replace rule-based judgment.
- Use `ApiResult` and existing exception patterns for API responses.
- Use `@AuditLog` or explicit audit service behavior for sensitive operations such as standard maintenance, rejudgment approval, concession approval, conflict裁决, confidence configuration, and AI recommendation adoption.
- Do not store secrets in source. Use environment placeholders in `application-dev.yml` or `application.yml`.
- For new configuration, prefer namespaced properties under `app.*` or a focused namespace matching the component.
- For generated files/uploads, respect configured upload locations and avoid committing runtime artifacts.

## Frontend Guidelines

- Use Vue 3 Composition API and TypeScript, consistent with existing pages.
- Use Element Plus components and `@element-plus/icons-vue` where possible.
- Keep API wrappers in `frontend/src/api/`.
- Keep shared types in `frontend/src/types/` when the type is reused across views.
- Routes are dynamically driven by menu data. When adding pages, update backend menu/RBAC seed data and ensure component paths match `frontend/src/views/...`.
- Use Pinia stores already present under `frontend/src/store/` for auth, menus, and dictionaries.
- Keep operational UI dense and scan-friendly. This is a quality management system, not a marketing site.

## Business Rules To Preserve

Quality judgment is rule-first:

- Standard priority: customer agreement > enterprise standard > national standard.
- Existing judgment result concepts include `QUALIFIED`, `UNQUALIFIED`, `NEED_REINSPECTION`, and `CAN_CONCESSION`.
- Reinspection and rejudgment are human-controlled workflows. AI can suggest and prefill reasons, but must not auto-create or auto-approve workflow records.
- Concession acceptance is high-risk. Low-confidence AI concession assessment must force manual review.
- Quality certificate generation must not be allowed for unresolved standard conflicts. A preview may be generated only if clearly marked as non-final.

For the planned AI enhancement work, preserve these red lines:

- Structured standard data is the single source of truth for judgment.
- RAG source text is for citations, explanation, and review; it must not override structured limits.
- AI output must be traceable, degradable, and auditable.
- If source evidence is missing, the system must say so rather than inventing standards, limits, clauses, or cases.

## AI Enhancement Direction

The current explored direction is documented in:

- `/Users/weixuelei/workspace/aiAgent/ai-project/doc/req.md`
- `/Users/weixuelei/workspace/aiAgent/ai-project/doc/clarify.md`
- `/Users/weixuelei/workspace/aiAgent/ai-project/doc/clarify2.md`

Expected future change name:

```text
ai-quality-enhancement
```

Likely capabilities:

- Standard RAG search.
- AI judgment explanation with citations.
- Standard conflict detection and裁决.
- Concession risk assessment.
- Reinspection/rejudgment suggestions.
- Quality certificate PDF export.
- AI confidence weight configuration.
- Repeatable demo/evaluation dataset.

Architectural constraints for AI work:

- Use a `ModelGateway`-style abstraction for DeepSeek or any future model provider.
- Use a `VectorStoreGateway`-style abstraction for Elasticsearch 8.15.0 or any future vector store.
- Configure model/vector credentials through environment placeholders; do not hardcode them.
- AI fallback should degrade in layers: pre-generated cache, rule template explanation, raw ES retrieved clauses, then clear unavailable state.
- Persist AI assessment outputs and input snapshots when they affect business review.

## Database And Seed Data

- Schema baseline: `backend/scripts/init-schema.sql`.
- Dictionaries: `backend/scripts/init-dict-data.sql`.
- Menu/RBAC seed: `backend/scripts/init-menu-rbac.sql`.
- Existing test/demo data: `backend/scripts/init-test-data.sql`.
- Add new migrations under `backend/scripts/migrations/` for existing databases.

When adding a table:

- Add entity, mapper, DTO/VO, service, and controller as needed.
- Add indexes for fields used by list pages, business lookup, and joins.
- Add seed data only when required for demo, tests, dictionaries, or menus.
- Avoid changing existing data semantics without a migration/backfill script.

## OpenSpec Workflow

Use OpenSpec for non-trivial feature changes:

```bash
openspec list --json
openspec validate --strict
```

Before implementing a large change:

- Create or update a change under `openspec/changes/<change-id>/`.
- Capture `proposal.md`, `design.md`, `tasks.md`, and spec deltas where appropriate.
- Keep implementation aligned with the accepted proposal.

Explore mode is for investigation only. Do not implement while using an explore-only instruction set.

## Testing And Verification

Minimum checks after code changes:

- Backend-only change: run `cd backend && mvn test` when feasible.
- Frontend-only change: run `cd frontend && npm run build` when feasible.
- Cross-stack feature: run both backend tests and frontend build where feasible.
- SQL changes: inspect scripts for MySQL 5.7 compatibility.
- Menu/page changes: verify backend menu seed and frontend component path align.

If a check cannot be run because MySQL, Redis, ES, network, or credentials are unavailable, state that clearly in the final response.

## Git And File Hygiene

- Do not revert user changes unless explicitly asked.
- Keep edits scoped to the requested change.
- Do not commit generated build outputs, logs, uploads, `target/`, or `node_modules/`.
- Prefer ASCII for new source/config files unless existing files or user-facing Chinese content require otherwise.
- Do not add secrets, API keys, passwords, or real customer data.
