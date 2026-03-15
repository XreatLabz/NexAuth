# Doctor/Status System Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Add a phased operator-facing `/nexauth status` and `/nexauth doctor` system that starts with a minimal command shell, grows into MVP health checks, and finishes with support-heavy diagnostics and dump integration.

**Architecture:** Keep command wiring thin inside `NexAuthCommand`, and move status/doctor logic into a dedicated `xyz.xreatlabs.nexauth.common.doctor` package made of small immutable models, snapshot builders, health checks, and renderers. Build the feature in phases so each phase ships usable behavior: first the shell and renderer, then MVP checks, then richer support/report integration. Prefer pure unit-tested services and renderers over platform-heavy command or runtime integration tests.

**Tech Stack:** Gradle multi-module build, Java 21, JUnit 5, Adventure components/messages, ACF command framework, existing `AuthMetrics`, `FailurePolicyMode`, `PlatformHandle.ProxyData`, and dump command infrastructure.

---

## Phase Overview

### Phase 0 — Workspace + planning handoff
- Create a clean feature branch or worktree after the existing stash.
- Keep the existing stash (`pre-doctor-status-implementation`) intact until the feature branch is ready.
- Use subagents for focused execution by phase, not for final verification.

### Phase 1 — Minimal command shell
- Add the doctor/status package and base result/report/renderer types.
- Add `/nexauth status` and `/nexauth doctor` commands with simple summary output.
- Add message keys and command syntax/autocomplete entries.

### Phase 2 — MVP operator checks
- Add health snapshots and checks for configuration, integrations, and auth state.
- Expand status output with runtime/config summary and metrics.
- Expand doctor output with `OK/WARN/FAIL` results and a summary line.

### Phase 3 — Support-heavy diagnostics
- Add remediation hints and verbose detail rendering.
- Integrate doctor/status data into `/nexauth dump` JSON.
- Add redaction-safe diagnostic sections for support workflows.

### Phase 4 — Verification + polish
- Run focused tests during each phase.
- Run final module test verification.
- Optionally request review before merge.

---

## Recommended subagent execution model

Use one subagent batch per phase after planning:

1. **Phase 1 subagent batch**
   - Agent A: implement base models + renderers + tests
   - Agent B: add command wiring + message keys + tests
2. **Phase 2 subagent batch**
   - Agent A: implement status snapshot builder + tests
   - Agent B: implement MVP health checks + tests
3. **Phase 3 subagent batch**
   - Agent A: integrate dump JSON/report rendering
   - Agent B: add remediation hints/polish + docs updates if needed
4. **Lead agent responsibilities**
   - review diffs between phases
   - resolve merge conflicts
   - run authoritative verification commands yourself

Do **not** let subagents claim completion without fresh local verification in the lead session.

---

### Task 1: Establish doctor package foundation

**TDD scenario:** New feature — full TDD cycle

**Files:**
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/DoctorSeverity.java`
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/DoctorCheckResult.java`
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/DoctorReport.java`
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/DoctorCheck.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/DoctorCheckResultTest.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/DoctorReportTest.java`

**Step 1: Write the failing tests**

Add tests that define:
- severity ordering / semantics (`OK`, `WARN`, `FAIL`)
- `DoctorReport` summary counts
- overall report severity derivation
- deterministic check ordering if the report sorts checks

**Step 2: Run the focused tests to verify they fail**

Run:
```bash
./gradlew :Plugin:test --tests '*DoctorCheckResultTest' --tests '*DoctorReportTest'
```
Expected: failing tests because the doctor model classes do not exist yet.

**Step 3: Write minimal production code**

Implement the smallest immutable model types needed to satisfy the tests.

**Step 4: Run the focused tests to verify they pass**

Run:
```bash
./gradlew :Plugin:test --tests '*DoctorCheckResultTest' --tests '*DoctorReportTest'
```
Expected: all focused tests pass.

**Step 5: Commit**

```bash
git add Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor
git commit -m "feat: add doctor report foundation"
```

---

### Task 2: Add text renderers for status and doctor output

**TDD scenario:** New feature — full TDD cycle

**Files:**
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/StatusSnapshot.java`
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/StatusRenderer.java`
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/DoctorRenderer.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/StatusRendererTest.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/DoctorRendererTest.java`

**Step 1: Write the failing tests**

Define expected rendered lines for:
- a minimal status snapshot
- a doctor report with mixed `OK/WARN/FAIL` results
- summary line formatting
- optional detail/hint lines in verbose rendering hooks (the hooks may be no-op in this phase)

**Step 2: Run the focused tests to verify they fail**

Run:
```bash
./gradlew :Plugin:test --tests '*StatusRendererTest' --tests '*DoctorRendererTest'
```
Expected: failing tests because the renderers do not exist yet.

**Step 3: Write minimal production code**

Implement renderers that return ordered line lists or simple value objects for command emission. Keep them independent from `Audience` so they stay easy to test.

**Step 4: Run the focused tests to verify they pass**

Run:
```bash
./gradlew :Plugin:test --tests '*StatusRendererTest' --tests '*DoctorRendererTest'
```
Expected: all focused tests pass.

**Step 5: Commit**

```bash
git add Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor
git commit -m "feat: add doctor and status renderers"
```

---

### Task 3: Add message keys and minimal command shell

**TDD scenario:** Modifying tested code — run existing tests first, then add coverage for new behavior

**Files:**
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/staff/NexAuthCommand.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/config/MessageKeys.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/MessageKeySmokeTest.java`

**Step 1: Run the current doctor foundation tests as baseline**

Run:
```bash
./gradlew :Plugin:test --tests '*DoctorCheckResultTest' --tests '*DoctorReportTest' --tests '*StatusRendererTest' --tests '*DoctorRendererTest'
```
Expected: current focused tests are green before command wiring.

**Step 2: Add message/config keys**

Add defaults for:
- `info-status-header`
- `info-status-entry`
- `info-status-metrics`
- `info-doctor-header`
- `info-doctor-entry`
- `info-doctor-detail`
- `info-doctor-summary`
- `syntax.status`
- `syntax.doctor`
- `autocomplete.status`
- `autocomplete.doctor`

Use existing `MessageKeys` patterns so `messages.conf` generation picks them up automatically.

**Step 3: Add minimal `/nexauth status` and `/nexauth doctor` subcommands**

In `NexAuthCommand`:
- add `@Subcommand("status")`
- add `@Subcommand("doctor")`
- both should return `CompletionStage<Void>`
- both should use `runAsync(() -> { ... })`
- both should emit rendered lines from the new renderer classes

For this phase, the commands may use placeholder/minimal snapshots and reports rather than full health checks.

**Step 4: Add a light smoke test for new message keys**

Write a small test that exercises the new keys through direct access or constant presence patterns. Do not build a heavy command-manager integration harness yet.

**Step 5: Run the focused tests**

Run:
```bash
./gradlew :Plugin:test --tests '*DoctorCheckResultTest' --tests '*DoctorReportTest' --tests '*StatusRendererTest' --tests '*DoctorRendererTest' --tests '*MessageKeySmokeTest'
```
Expected: all focused tests pass.

**Step 6: Commit**

```bash
git add Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/staff/NexAuthCommand.java Plugin/src/main/java/xyz/xreatlabs/nexauth/common/config/MessageKeys.java Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor
git commit -m "feat: add doctor and status command shell"
```

---

### Task 4: Build status snapshot provider for MVP output

**TDD scenario:** New feature — full TDD cycle

**Files:**
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/StatusSnapshotProvider.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/StatusSnapshotProviderTest.java`

**Step 1: Write the failing tests**

Cover snapshot fields for:
- plugin version
- platform identifier
- failure policy mode
- multi-proxy enabled flag
- email enabled/disabled via handler presence
- TOTP enabled/disabled via provider presence
- limbo integration present/absent
- auth metrics JSON presence
- proxy data summary presence

Prefer small handwritten stubs/fakes instead of Mockito.

**Step 2: Run the focused tests to verify they fail**

Run:
```bash
./gradlew :Plugin:test --tests '*StatusSnapshotProviderTest'
```
Expected: failing tests because the provider does not exist yet.

**Step 3: Write minimal production code**

Build a provider that converts `AuthenticNexAuth<?, ?>` state into a `StatusSnapshot` without doing any risky I/O.

**Step 4: Run the focused tests to verify they pass**

Run:
```bash
./gradlew :Plugin:test --tests '*StatusSnapshotProviderTest'
```
Expected: all focused tests pass.

**Step 5: Update `/nexauth status` to use the real provider**

Replace the placeholder snapshot in `NexAuthCommand` with the provider output.

**Step 6: Run the related focused tests**

Run:
```bash
./gradlew :Plugin:test --tests '*StatusSnapshotProviderTest' --tests '*StatusRendererTest'
```
Expected: both pass.

**Step 7: Commit**

```bash
git add Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/staff/NexAuthCommand.java Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor
git commit -m "feat: add status snapshot provider"
```

---

### Task 5: Add MVP doctor checks

**TDD scenario:** New feature — full TDD cycle

**Files:**
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/checks/ConfigurationDoctorCheck.java`
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/checks/DatabaseDoctorCheck.java`
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/checks/IntegrationDoctorCheck.java`
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/DoctorService.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/checks/ConfigurationDoctorCheckTest.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/checks/DatabaseDoctorCheckTest.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/checks/IntegrationDoctorCheckTest.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/DoctorServiceTest.java`

**Step 1: Write the failing tests for individual checks**

Define expected results for:
- invalid/unknown failure-policy strings defaulting to `HARD_FAIL` but surfacing as a warning
- database provider or connector missing when one should exist
- disconnected connector state
- email enabled/disabled state
- TOTP provider mismatch (feature expected vs unavailable)
- limbo integration and proxy data availability

Keep checks read-only and snapshot-driven where possible.

**Step 2: Run the focused tests to verify they fail**

Run:
```bash
./gradlew :Plugin:test --tests '*ConfigurationDoctorCheckTest' --tests '*DatabaseDoctorCheckTest' --tests '*IntegrationDoctorCheckTest' --tests '*DoctorServiceTest'
```
Expected: failing tests because the checks and service do not exist yet.

**Step 3: Write minimal production code**

Implement:
- check classes that return `DoctorCheckResult`
- a `DoctorService` that assembles and orders results into a `DoctorReport`
- snapshot extraction helpers only when needed to avoid excessive command logic

**Step 4: Run the focused tests to verify they pass**

Run:
```bash
./gradlew :Plugin:test --tests '*ConfigurationDoctorCheckTest' --tests '*DatabaseDoctorCheckTest' --tests '*IntegrationDoctorCheckTest' --tests '*DoctorServiceTest'
```
Expected: all focused tests pass.

**Step 5: Wire `/nexauth doctor` to the real service**

Update `NexAuthCommand` so `/nexauth doctor` renders the report from `DoctorService`.

**Step 6: Re-run related tests**

Run:
```bash
./gradlew :Plugin:test --tests '*DoctorServiceTest' --tests '*DoctorRendererTest' --tests '*StatusSnapshotProviderTest'
```
Expected: all pass.

**Step 7: Commit**

```bash
git add Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/staff/NexAuthCommand.java Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor
git commit -m "feat: add doctor mvp health checks"
```

---

### Task 6: Add support-heavy details and remediation hints

**TDD scenario:** New feature — full TDD cycle

**Files:**
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/DoctorCheckResult.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/DoctorRenderer.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/checks/ConfigurationDoctorCheck.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/checks/DatabaseDoctorCheck.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/checks/IntegrationDoctorCheck.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/DoctorRemediationTest.java`

**Step 1: Write the failing tests**

Define expectations for:
- optional remediation hint text on warning/fail results
- optional detail lines in verbose rendering
- summary counts remaining correct when details are present

**Step 2: Run the focused tests to verify they fail**

Run:
```bash
./gradlew :Plugin:test --tests '*DoctorRemediationTest' --tests '*DoctorRendererTest' --tests '*DoctorServiceTest'
```
Expected: failing tests because remediation/detail support is not implemented yet.

**Step 3: Write minimal production code**

Add optional fields like:
- detail
- remediation hint
- possibly category/check id

Render them only when present.

**Step 4: Run the focused tests to verify they pass**

Run:
```bash
./gradlew :Plugin:test --tests '*DoctorRemediationTest' --tests '*DoctorRendererTest' --tests '*DoctorServiceTest'
```
Expected: all focused tests pass.

**Step 5: Commit**

```bash
git add Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor
git commit -m "feat: add remediation hints to doctor output"
```

---

### Task 7: Integrate status/doctor data into `/nexauth dump`

**TDD scenario:** Modifying tested code — run existing tests first, then add coverage for new behavior

**Files:**
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/staff/NexAuthCommand.java`
- Create: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/DoctorJsonExporter.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/DoctorJsonExporterTest.java`

**Step 1: Run related tests as baseline**

Run:
```bash
./gradlew :Plugin:test --tests '*DoctorServiceTest' --tests '*DoctorRendererTest' --tests '*StatusSnapshotProviderTest'
```
Expected: current focused tests are green before dump integration.

**Step 2: Write the failing exporter test**

Define JSON expectations for:
- top-level doctor/status sections
- report severity and counts
- check entries with severity, message, detail, and remediation when present
- status fields and auth metrics inclusion

**Step 3: Run the focused exporter test to verify it fails**

Run:
```bash
./gradlew :Plugin:test --tests '*DoctorJsonExporterTest'
```
Expected: failing test because the exporter does not exist yet.

**Step 4: Write minimal production code**

Implement JSON export helpers and update `/nexauth dump` to include:
- `status`
- `doctor`
- existing `authMetrics` retained for backward compatibility if needed

**Step 5: Run the focused dump/export tests**

Run:
```bash
./gradlew :Plugin:test --tests '*DoctorJsonExporterTest' --tests '*DoctorServiceTest' --tests '*StatusSnapshotProviderTest'
```
Expected: all focused tests pass.

**Step 6: Commit**

```bash
git add Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/staff/NexAuthCommand.java Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor
git commit -m "feat: include doctor and status in dump output"
```

---

### Task 8: Final verification sweep

**TDD scenario:** Modifying tested code — run existing tests before and after

**Files:**
- No new files; verification only

**Step 1: Run all doctor/status focused tests**

Run:
```bash
./gradlew :Plugin:test --tests '*Doctor*' --tests '*Status*' --tests '*AuthMetricsTest' --tests '*FailurePolicyModeTest'
```
Expected: all focused tests pass.

**Step 2: Run the full Plugin test suite**

Run:
```bash
./gradlew :Plugin:test
```
Expected: Plugin tests pass cleanly.

**Step 3: Run full module verification if Plugin stays green**

Run:
```bash
./gradlew :API:test :Plugin:test
```
Expected: both modules pass cleanly.

**Step 4: Review scope discipline**

Confirm changes are limited to:
- `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/**`
- `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/staff/NexAuthCommand.java`
- `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/config/MessageKeys.java`
- `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/**`
- no unrelated production edits beyond required wiring

**Step 5: Commit verification checkpoint**

```bash
git add Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/staff/NexAuthCommand.java Plugin/src/main/java/xyz/xreatlabs/nexauth/common/config/MessageKeys.java Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor
git commit -m "feat: add operator doctor and status system"
```

---

## Notes for execution

- Keep `/nexauth status` read-only and fast; it should never perform risky network or write operations.
- Keep `/nexauth doctor` resilient: one failing check should become a failing result, not crash the whole command.
- Prefer handwritten fakes/stubs in tests instead of Mockito unless test setup becomes clearly unreasonable.
- If command wiring becomes awkward to test, keep the command methods thinner and move more logic into `StatusSnapshotProvider`, `DoctorService`, renderers, and exporters.
- If you decide to move the package split toward `common.observability` or `common.reliability`, update this plan consistently before execution rather than mixing namespaces mid-implementation.
