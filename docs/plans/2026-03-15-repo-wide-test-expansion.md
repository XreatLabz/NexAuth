# Repo-wide test expansion Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Expand NexAuth test coverage as far as practical across API and Plugin modules, prioritizing high-value pure-unit coverage for auth core, utility/config logic, database helpers, and selected limbo/runtime components while keeping the current master build green.

**Architecture:** Work in phases from lowest-cost/highest-value logic to more coupled components. Favor pure-unit tests around deterministic classes, records, enums, parsers, renderers, guards, and policy objects; only introduce broader integration-style tests where the code cannot be exercised credibly in isolation.

**Tech Stack:** Java 21, Gradle, JUnit 5, existing NexAuth API/Plugin modules.

---

### Task 1: Expand API module coverage for pure value/utility types

**TDD scenario:** Modifying/adding tested code — full TDD for uncovered logic.

**Files:**
- Existing tested file: `API/src/test/java/xyz/xreatlabs/nexauth/api/util/SemanticVersionTest.java`
- Create tests for small deterministic API types, for example:
  - `API/src/test/java/xyz/xreatlabs/nexauth/api/util/ReleaseTest.java`
  - `API/src/test/java/xyz/xreatlabs/nexauth/api/premium/PremiumExceptionTest.java`
  - `API/src/test/java/xyz/xreatlabs/nexauth/api/premium/PremiumUserTest.java`
  - `API/src/test/java/xyz/xreatlabs/nexauth/api/server/ServerPingTest.java`
  - `API/src/test/java/xyz/xreatlabs/nexauth/api/totp/TOTPDataTest.java`

**Step 1: Write failing tests**

Cover equality/record semantics, exposed flags/accessors, and any nontrivial enum/exception behavior.

**Step 2: Run focused API tests to verify red**

Run: `./gradlew :API:test --tests "*ReleaseTest" --tests "*PremiumExceptionTest" --tests "*PremiumUserTest" --tests "*ServerPingTest" --tests "*TOTPDataTest"`
Expected: FAIL until the tests and any tiny required fixes are correct.

**Step 3: Add minimal code fixes only if tests reveal real defects**

Prefer test-only additions unless uncovered API bugs require tiny source changes.

**Step 4: Re-run focused API tests to verify green**

Run the same command and confirm PASS.

**Step 5: Commit**

```bash
git add API/src/test/java API/src/main/java
git commit -m "test: expand API value type coverage"
```

### Task 2: Cover common policy/utility logic in Plugin

**TDD scenario:** Modifying tested code — add focused unit tests first.

**Files:**
- Existing tests to extend around utility/policy logic:
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/reliability/FailurePolicyModeTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/util/AuthRuntimeGuardsTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/observability/AuthMetricsTest.java`
- Create tests for additional pure logic, for example:
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/listener/PreLoginResultTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/listener/PreLoginStateTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/authorization/ProfileConflictResolutionStrategyTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/config/NewUUIDCreatorTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/util/RateLimiterTest.java`

**Step 1: Write failing tests**

Focus on enum parsing, state containers, rate-limiter semantics, and other deterministic behavior.

**Step 2: Run focused Plugin tests to verify red**

Run: `./gradlew :Plugin:test --tests "*PreLoginResultTest" --tests "*PreLoginStateTest" --tests "*ProfileConflictResolutionStrategyTest" --tests "*NewUUIDCreatorTest" --tests "*RateLimiterTest"`
Expected: FAIL until tests compile and assertions are correct.

**Step 3: Write minimal implementation only if tests expose real defects**

No speculative refactors.

**Step 4: Re-run focused tests to verify green**

Run the same command and confirm PASS.

**Step 5: Commit**

```bash
git add Plugin/src/test/java Plugin/src/main/java
git commit -m "test: cover common policy and utility logic"
```

### Task 3: Add auth-core regression tests around listeners/commands/guards

**TDD scenario:** Modifying tested code — full red/green for uncovered high-value logic.

**Files:**
- Extend/add tests around auth flow helpers and guards, for example:
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/listener/LoginTryListenerTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/authorization/AuthenticAuthorizationProviderTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/command/InvalidCommandArgumentTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/util/AuthRuntimeGuardsTest.java` (extend)

**Step 1: Write failing tests**

Target concrete auth invariants such as lockout/backoff counters, TOTP-attempt limits, and null-safe user lookup behavior.

**Step 2: Run focused tests to verify red**

Run: `./gradlew :Plugin:test --tests "*LoginTryListenerTest" --tests "*AuthenticAuthorizationProviderTest" --tests "*InvalidCommandArgumentTest"`
Expected: FAIL until tests are in place and assertions are correct.

**Step 3: Write minimal test harnesses and source fixes**

Use simple fakes/stubs rather than heavyweight platform bootstraps where possible.

**Step 4: Re-run focused tests to verify green**

Run the same command and confirm PASS.

**Step 5: Commit**

```bash
git add Plugin/src/test/java Plugin/src/main/java
git commit -m "test: add auth core regression coverage"
```

### Task 4: Add configuration/database pure-unit coverage

**TDD scenario:** Modifying tested code — add tests first.

**Files:**
- Create tests for deterministic configuration/database helpers, for example:
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/database/AuthenticUserTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/database/connector/DatabaseConnectorRegistrationTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/config/key/ConfigurationKeyTest.java`
  - `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/config/migrate/...` (selected migrators if practical)

**Step 1: Write failing tests**

Prioritize objects with pure getters/setters/defaulting logic and migration helpers that can be exercised without production-only runtime dependencies.

**Step 2: Run focused tests to verify red**

Run: `./gradlew :Plugin:test --tests "*AuthenticUserTest" --tests "*DatabaseConnectorRegistrationTest" --tests "*ConfigurationKeyTest"`
Expected: FAIL initially.

**Step 3: Add minimal implementation fixes if necessary**

Only where tests reveal actual defects.

**Step 4: Re-run focused tests to verify green**

Run the same command and confirm PASS.

**Step 5: Commit**

```bash
git add Plugin/src/test/java Plugin/src/main/java
git commit -m "test: expand configuration and database helper coverage"
```

### Task 5: Add selected limbo/runtime unit tests where isolation is practical

**TDD scenario:** New feature/new file — full TDD.

**Files:**
- Create narrowly scoped tests for isolated limbo/runtime classes, for example:
  - `Plugin/src/test/java/ua/nanit/limbo/util/ColorsTest.java`
  - `Plugin/src/test/java/ua/nanit/limbo/server/data/PingDataTest.java`
  - `Plugin/src/test/java/ua/nanit/limbo/server/data/TitleTest.java`
  - `Plugin/src/test/java/ua/nanit/limbo/server/data/BossBarTest.java`
  - `Plugin/src/test/java/ua/nanit/limbo/protocol/registry/VersionTest.java`

**Step 1: Write failing tests**

Choose only classes with deterministic behavior and no heavy Netty/proxy bootstrap requirements.

**Step 2: Run focused tests to verify red**

Run: `./gradlew :Plugin:test --tests "*ColorsTest" --tests "*PingDataTest" --tests "*TitleTest" --tests "*BossBarTest" --tests "*VersionTest"`
Expected: FAIL until tests exist.

**Step 3: Write minimal implementation fixes if tests expose real bugs**

Avoid broad limbo-runtime refactors.

**Step 4: Re-run focused tests to verify green**

Run the same command and confirm PASS.

**Step 5: Commit**

```bash
git add Plugin/src/test/java Plugin/src/main/java
git commit -m "test: add isolated limbo runtime coverage"
```

### Task 6: Full verification and gap report

**TDD scenario:** Trivial change — use judgment.

**Files:**
- No new source changes unless verification exposes another concrete bug.

**Step 1: Run all API tests**

Run: `./gradlew :API:test`
Expected: PASS.

**Step 2: Run all Plugin tests**

Run: `./gradlew :Plugin:test`
Expected: PASS.

**Step 3: Run full plugin build**

Run: `./gradlew :Plugin:build`
Expected: BUILD SUCCESSFUL.

**Step 4: Review remaining untested areas**

Summarize which packages are now covered and which areas still need heavier harnesses (for example full platform bootstraps, protocol/netty flows, or external-service integrations).

**Step 5: Request review**

Request code review on the cumulative testing expansion before commit/push/merge decisions.
