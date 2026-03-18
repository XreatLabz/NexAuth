# First Production Release 1.0.0 Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Prepare NexAuth for its first production release by updating the project version to 1.0.0 and aligning release-facing metadata and tests.

**Architecture:** Keep the root Gradle version as the single source of truth, then update release-facing documentation and test fixtures that intentionally assert the current version string. Verification should prove both version parsing behavior and release metadata consistency.

**Tech Stack:** Gradle, Java 21, JUnit 5, Markdown release docs.

---

### Task 1: Update canonical project version

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `build.gradle`

**Step 1: Update the root project version**

Change:
```groovy
version = '0.0.1-beta3'
```
To:
```groovy
version = '1.0.0'
```

**Step 2: Confirm this is the canonical source**

Check that subprojects still inherit `rootProject.version` and resource expansion still uses `version`.

**Step 3: Commit (optional, only if requested)**

```bash
git add build.gradle
git commit -m "chore(release): prepare v1.0.0"
```

### Task 2: Update release-facing documents

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `CHANGELOG.md`
- Modify: `CHANGELOGS.md`

**Step 1: Add or normalize the 1.0.0 release entry**

Update `CHANGELOG.md` so it presents the first production release as `1.0.0` with release-ready notes.

**Step 2: Align auxiliary release notes**

Update `CHANGELOGS.md` so the headline and compare links point to `1.0.0`.

**Step 3: Ensure no release text still labels the current release as beta/pre-production**

Search for stale `0.0.1-beta3` or `pre-production` references in release-facing docs and remove or replace them where they describe the current release.

### Task 3: Update tests that intentionally assert the release version

**TDD scenario:** Modifying tested code — run existing tests first

**Files:**
- Modify: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/StatusRendererTest.java`
- Modify: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/StatusSnapshotProviderTest.java`
- Modify: `Plugin/src/test/java/xyz/xreatlabs/nexauth/common/doctor/DoctorJsonExporterTest.java`
- Modify: `API/src/test/java/xyz/xreatlabs/nexauth/api/util/SemanticVersionTest.java`

**Step 1: Run focused tests before editing**

Run:
```bash
./gradlew :Plugin:test --tests '*StatusRendererTest' --tests '*StatusSnapshotProviderTest' --tests '*DoctorJsonExporterTest' :API:test --tests '*SemanticVersionTest'
```
Expected: existing tests pass before the fixture update.

**Step 2: Update hard-coded version expectations**

Replace old beta fixtures with `1.0.0`, and make the semantic-version test assert that a stable release parses with `dev == false`.

**Step 3: Re-run the focused tests**

Run the same command and expect all targeted tests to pass.

### Task 4: Verify release readiness

**TDD scenario:** Trivial change — use judgment

**Files:**
- Verify only

**Step 1: Search for stale current-release version strings**

Run:
```bash
grep -R "0.0.1-beta3\|pre-production" -n build.gradle CHANGELOG.md CHANGELOGS.md Plugin/src/test API/src/test
```
Expected: no stale references remain for the current release metadata.

**Step 2: Run the project verification command**

Run:
```bash
./gradlew test
```
Expected: exit code 0.

**Step 3: Summarize next manual release actions**

Report that the repo is locally prepared on `master` for `v1.0.0`, and ask whether to commit/tag/push after verification.
