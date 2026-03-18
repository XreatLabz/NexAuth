# Premium Auto-Login Message Fix Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Ensure premium auto-login reliably sends the existing success message to the player, including on proxy flows where immediate server forwarding can race delayed chat delivery.

**Architecture:** Keep the existing premium/session message keys and overall auth flow, but move the premium auto-login chat message send out of the delayed task so it is delivered immediately before any authenticated-event side effects can forward the player. Leave the premium title behavior delayed if needed for client readiness, since the reported bug concerns missing chat feedback rather than title rendering.

**Tech Stack:** Java 21, Gradle, JUnit 5, Adventure audience messaging.

---

### Task 1: Verify the current auth-flow implementation

**TDD scenario:** Modifying tested code — run existing tests first

**Files:**
- Inspect: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/listener/AuthenticListeners.java`
- Inspect: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/config/MessageKeys.java`

**Step 1: Confirm the premium auto-login path**

Verify that `onPostLogin(...)` currently delays the premium message and then fires the authenticated event.

**Step 2: Confirm intended message key**

Verify that `info-premium-logged-in` already exists and matches the desired player-facing text.

### Task 2: Implement the minimal premium message timing fix

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/listener/AuthenticListeners.java`

**Step 1: Send the premium chat message immediately**

Refactor the `user.autoLoginEnabled()` branch so it:
- obtains the audience immediately,
- sends `info-premium-logged-in` immediately,
- keeps only the premium title in the delayed task if title display still needs delay.

**Step 2: Preserve existing behavior otherwise**

Do not change message keys, title text, authentication reason, or session auto-login behavior unless strictly needed by verification.

### Task 3: Add a focused regression test

**TDD scenario:** New feature — full TDD cycle

**Files:**
- Create or modify a focused test under `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/listener/`

**Step 1: Write a failing test for immediate premium message send**

Add a test that exercises the premium auto-login branch and proves `info-premium-logged-in` is sent before delayed tasks/event-driven forwarding could race it.

**Step 2: Run the focused test to confirm failure**

Run the smallest command that executes the new test.

**Step 3: Implement the minimal production change**

Apply only the timing fix needed to make the test pass.

**Step 4: Re-run the focused test**

Expect the new regression test to pass.

### Task 4: Verify the fix end-to-end at repo level

**TDD scenario:** Modifying tested code — run relevant suites

**Files:**
- Verify only

**Step 1: Run focused plugin tests**

Run the listener-focused test command plus any directly affected tests.

**Step 2: Run the full project test suite**

Run:
```bash
./gradlew test
```
Expected: exit code 0.

**Step 3: Request review and summarize**

Open a review for the changed files and report the exact verification output before any commit/push step.
