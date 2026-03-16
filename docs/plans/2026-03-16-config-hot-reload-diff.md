# Config Hot-Reload with Diff Display

**Date:** 2026-03-16
**Status:** Approved

## Summary

Enhance the existing reload mechanism with a unified `/nexauth reload` command that reloads both `config.conf` and `messages.conf`, then displays a summary of what changed in a click-to-expand diff format using hover text.

## Design

### Command

- **`/nexauth reload`** — unified reload (new)
  - Permission: `nexauth.reload`
  - Reloads config + messages in one shot
  - Shows summary with hover-expandable diff
- Existing `/nexauth reload configuration` and `/nexauth reload messages` stay for backward compat

### Flow

1. Snapshot all current config values (key → string repr) + message raw strings
2. Call `configuration.reload(plugin)` — re-reads config.conf
3. Call `messages.reload(plugin)` + `commandProvider.injectMessages()` — re-reads messages.conf
4. Diff old snapshot vs new values
5. Display summary: `✅ Reloaded! 3 config changes, 1 message change` (hover to see details)
6. On error: catch exception, report error, no diff (nothing changed)

### Components

1. **ConfigSnapshot** — captures Map<String, String> from ConfigurateHelper for all config keys + message keys
2. **ConfigDiff** — compares two snapshots, produces list of `Change(key, oldValue, newValue, type=ADDED|CHANGED|REMOVED)`
3. **DiffRenderer** — formats changes as Adventure TextComponent with hover events
4. **Unified reload subcommand** in NexAuthCommand

### Diff Display

Summary line with hover text showing per-key changes:
```
✅ Reloaded! 3 config changes, 1 message change [hover for details]
```

Hover text:
```
Config Changes:
  database.type: MySQL → PostgreSQL
  totp.enabled: false → true
  new-sessions.timeout: 604800 → 86400

Message Changes:
  kick-name-mismatch: <old> → <new>
```

If no changes: `✅ Reloaded! No changes detected.`
