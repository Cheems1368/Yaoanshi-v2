---
name: yaoanshi-neat
description: Project-specific closeout and context recovery workflow for D:\yaoanshi. Use when the user explicitly asks to tidy, close out, update progress, sync project context, review knowledge drift, run /neat, or restore context for a new session. Maintains progress.txt and the Standard document/*.md knowledge base; do not trigger for ordinary feature implementation unless the user asks for cleanup or context sync.
---

# Yaoanshi Neat

## Purpose

Keep the Yaoshian project handoff state accurate between sessions. Treat `progress.txt` as the project progress memory index and `Standard document/*.md` as the only formal project knowledge base.

Do not use this skill to implement product features. Use it after implementation or at session start to recover context.

## Modes

### Context recovery

Use when the user asks to restore context, read progress, starts a new session, opens a new terminal, or switches branch.

1. Read `progress.txt`.
2. Read `Standard document/IMPLEMENTATION_PLAN.md`.
3. Read only the task-relevant formal document from `Standard document/`.
4. Run the read-only checker if the user asks for a health check.
5. Do not modify files in this mode.

### Closeout tidy

Use when the user asks to tidy the project, update progress, run closeout review, run `/neat`, or sync project context after a task.

1. Run `scripts/check_knowledge_state.py` from the repository root.
2. Review the current diff and completed work before editing.
3. Update `progress.txt`:
   - Keep Branch / Context, Current State, In Progress, Next, and Last Updated current.
   - Append a dated note with changed, validated, open risks, and next action.
   - Do not store secrets, tokens, credentials, or `.env` values.
   - Do not rewrite historical entries unless correcting a clear factual error.
4. Update `Standard document/*.md` only for durable facts: API contracts, data model, user flows, tech stack, UI rules, or implementation phase order.
5. Do not modify `AGENTS.md`, `CODEX.md`, `CLAUDE.md`, or Memory unless the user explicitly asks.
6. Report changed files, validation, unresolved risks, and next action.

## Hard Rules

- The only formal project knowledge base is `Standard document/*.md`.
- The `docs/` tree has been removed. Do not create, read, or write `docs/` as a project knowledge location.
- Temporary plans stay in conversation. Durable build sequence changes belong in `Standard document/IMPLEMENTATION_PLAN.md`.
- Prefer merging and correcting existing entries over appending duplicate facts.
- Prefer exposing stale or conflicting information in the final summary over deleting it silently.
- Use absolute dates such as `2026-05-12`; avoid relative-time wording.

## Resources

- Read `references/knowledge-map.md` before deciding which knowledge file to update.
- Run the checker:

```powershell
python D:\yaoanshi\.codex\skills\yaoanshi-neat\scripts\check_knowledge_state.py D:\yaoanshi
```
