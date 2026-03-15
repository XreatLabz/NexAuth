# Velocity startup/auth reliability fix Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Prevent Velocity startup/login failures in NexAuth 0.0.1-beta2 from kicking players with `Couldn't load/register information` when the underlying trigger is the update check or Protocolize/TOTP startup state.

**Architecture:** Keep the fix narrow and local to the existing common/Velocity auth flow. Harden update-check parsing and comparison against invalid remote release data, gate TOTP cleanly when Protocolize is unavailable or incompatible, and add null-safe handling in shared auth/player lookup paths so startup/dependency issues degrade instead of cascading into login/register failures.

**Tech Stack:** Java 21, Gradle, Velocity, shared NexAuth common auth/database code, JUnit 5.

---

### Task 1: Harden update-check version parsing

**TDD scenario:** Modifying tested code — add focused tests first.

**Files:**
- Modify: `API/src/main/java/xyz/xreatlabs/nexauth/api/util/SemanticVersion.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/AuthenticNexAuth.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/UpdateCheckVersionParsingTest.java`

**Step 1: Write the failing test**

Add tests that prove invalid/partial version strings do not crash parsing/comparison and that invalid release entries are ignored by update-check helpers.

**Step 2: Run test to verify it fails**

Run: `./gradlew :Plugin:test --tests "*UpdateCheckVersionParsingTest"`
Expected: FAIL because the current code throws on malformed tags or still allows null/invalid release versions into comparison.

**Step 3: Write minimal implementation**

- Make semantic version parsing tolerant (return `null` or equivalent safe signal for invalid input instead of throwing unchecked parse/index exceptions).
- Filter out invalid/missing release versions before creating `Release` objects or before comparing them.
- Guard `checkForUpdates()` so `latest == null`, empty release lists, or invalid release versions never reach `SemanticVersion.compare()`.
- Keep current logging behavior, but downgrade invalid upstream data to debug/warn rather than exception-driven failures.

**Step 4: Run test to verify it passes**

Run: `./gradlew :Plugin:test --tests "*UpdateCheckVersionParsingTest"`
Expected: PASS.

**Step 5: Commit**

```bash
git add API/src/main/java/xyz/xreatlabs/nexauth/api/util/SemanticVersion.java \
        Plugin/src/main/java/xyz/xreatlabs/nexauth/common/AuthenticNexAuth.java \
        Plugin/src/test/java/xyz/xreatlabs/nexauth/common/UpdateCheckVersionParsingTest.java
git commit -m "fix: harden NexAuth update version parsing"
```

### Task 2: Gracefully disable TOTP when Protocolize is unavailable

**TDD scenario:** Modifying tested code — add focused tests first.

**Files:**
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/AuthenticNexAuth.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/velocity/VelocityNexAuth.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/authorization/LoginCommand.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/authorization/AuthenticAuthorizationProvider.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/TotpAvailabilityGuardTest.java`

**Step 1: Write the failing test**

Add tests covering:
- TOTP enabled in config but no image projector / Protocolize available → TOTP provider remains disabled without throwing.
- A registered user with a stored TOTP secret can still log in safely when the TOTP provider is unavailable (degrade instead of NPE/kick cascade).
- Two-factor setup/confirmation paths short-circuit safely when the provider is unavailable.

**Step 2: Run test to verify it fails**

Run: `./gradlew :Plugin:test --tests "*TotpAvailabilityGuardTest"`
Expected: FAIL because current code assumes `plugin.getTOTPProvider()` exists whenever a user has a secret or TOTP-related confirmation is attempted.

**Step 3: Write minimal implementation**

- Centralize a clear “TOTP unavailable” branch during startup when config enables TOTP but Protocolize is absent/incompatible.
- Log that TOTP is being disabled/degraded rather than leaving only a generic warning.
- Ensure login/confirmation paths never dereference a null TOTP provider.
- Preserve current behavior when Protocolize is present and compatible.

**Step 4: Run test to verify it passes**

Run: `./gradlew :Plugin:test --tests "*TotpAvailabilityGuardTest"`
Expected: PASS.

**Step 5: Commit**

```bash
git add Plugin/src/main/java/xyz/xreatlabs/nexauth/common/AuthenticNexAuth.java \
        Plugin/src/main/java/xyz/xreatlabs/nexauth/velocity/VelocityNexAuth.java \
        Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/authorization/LoginCommand.java \
        Plugin/src/main/java/xyz/xreatlabs/nexauth/common/authorization/AuthenticAuthorizationProvider.java \
        Plugin/src/test/java/xyz/xreatlabs/nexauth/common/TotpAvailabilityGuardTest.java
git commit -m "fix: degrade gracefully when TOTP support is unavailable"
```

### Task 3: Null-safe shared load/register flow on Velocity

**TDD scenario:** Modifying tested code — add focused tests first.

**Files:**
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/listener/AuthenticListeners.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/Command.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/authorization/RegisterCommand.java`
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/authorization/LoginCommand.java`
- Create: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/AuthUserLookupGuardTest.java`

**Step 1: Write the failing test**

Add tests that prove:
- Missing DB user records during shared load/choose-server/post-login logic do not throw NPE.
- Login/register commands fail with a controlled user-facing error when `getByUUID()` returns null.
- Velocity/shared startup dependency issues no longer cascade into `user.autoLoginEnabled()`, `user.getSecret()`, or `user.isRegistered()` NPEs.

**Step 2: Run test to verify it fails**

Run: `./gradlew :Plugin:test --tests "*AuthUserLookupGuardTest"`
Expected: FAIL because current shared auth/command code dereferences missing users.

**Step 3: Write minimal implementation**

- Add explicit null handling in `AuthenticListeners` shared auth flow (`onPostLogin`, `chooseServer`) and command `getUser()`/callers.
- Prefer controlled denial/message/logging over unexpected exceptions.
- Keep behavior unchanged for valid users and normal SQLite-backed flow.

**Step 4: Run test to verify it passes**

Run: `./gradlew :Plugin:test --tests "*AuthUserLookupGuardTest"`
Expected: PASS.

**Step 5: Commit**

```bash
git add Plugin/src/main/java/xyz/xreatlabs/nexauth/common/listener/AuthenticListeners.java \
        Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/Command.java \
        Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/authorization/RegisterCommand.java \
        Plugin/src/main/java/xyz/xreatlabs/nexauth/common/command/commands/authorization/LoginCommand.java \
        Plugin/src/test/java/xyz/xreatlabs/nexauth/common/AuthUserLookupGuardTest.java
git commit -m "fix: guard missing auth records during login flow"
```

### Task 4: Full verification for the targeted regression

**TDD scenario:** Trivial change — use judgment.

**Files:**
- No source changes required unless verification exposes a regression.

**Step 1: Run focused regression tests**

Run:
```bash
./gradlew :Plugin:test --tests "*UpdateCheckVersionParsingTest" --tests "*TotpAvailabilityGuardTest" --tests "*AuthUserLookupGuardTest"
```
Expected: PASS.

**Step 2: Run broader plugin tests**

Run: `./gradlew :Plugin:test`
Expected: PASS.

**Step 3: Run project build if test suite passes**

Run: `./gradlew :Plugin:build`
Expected: BUILD SUCCESSFUL.

**Step 4: Commit verification-only follow-up if needed**

Only if verification requires a tiny follow-up fix.

**Step 5: Request review**

Request code review covering the diff from the safety worktree setup commit to the fix branch HEAD.
