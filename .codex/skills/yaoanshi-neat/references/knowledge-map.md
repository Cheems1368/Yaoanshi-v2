# Yaoanshi Knowledge Map

## Authoritative Files

- Progress memory: `progress.txt`
- Product scope, user stories, success criteria, non-goals: `Standard document/PRD.md`
- Pages, routes, flows, triggers, success/error paths: `Standard document/APP_FLOW.md`
- Dependencies, versions, build tools, runtime requirements: `Standard document/TECH_STACK.md`
- UI tokens, components, layout, responsive rules: `Standard document/FRONTEND_GUIDELINES.md`
- Database, API, DTO, auth, storage, migrations: `Standard document/BACKEND_STRUCTURE.md`
- Build order, phase sequence, validation gates: `Standard document/IMPLEMENTATION_PLAN.md`

Do not use `docs/` for project knowledge, references, or plan output.

## progress.txt Policy

`progress.txt` is the first file a new agent reads to continue from the previous session.

On closeout:

- Update top-level current state so the next session can resume without scanning history.
- Append a dated note using the existing template: Date, Workstream, Changed, Validated, Open risks, Next.
- Keep historical notes intact unless correcting a factual error.
- Never paste secrets, tokens, private keys, `.env` values, or credentials.
- Mention validation commands by name and result, not long logs.

## Impact Matrix

- API, DTO, auth, storage, migrations, database schema -> `Standard document/BACKEND_STRUCTURE.md`
- User flow, page route, success/error path -> `Standard document/APP_FLOW.md`
- Dependency, runtime, build command, environment requirement -> `Standard document/TECH_STACK.md`
- UI token, component behavior, responsive rule -> `Standard document/FRONTEND_GUIDELINES.md`
- Product scope, non-goal, success criterion -> `Standard document/PRD.md`
- Durable phase order or validation gate -> `Standard document/IMPLEMENTATION_PLAN.md`
- Short-lived implementation status, one-time validation, active blocker -> `progress.txt`

If a fact fits only the current task or next handoff, keep it in `progress.txt`.
If a fact changes how future engineers must build the product, update the matching Standard document.

## Closeout Summary Shape

Return:

- Summary of changed knowledge files.
- Validation or checker result.
- Open risks or stale/conflicting facts not changed.
- Next action for the next session.
