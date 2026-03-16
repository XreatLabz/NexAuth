# License build fix Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Restore `:Plugin:build` on `master` by fixing the source-file license header violations currently blocking `:Plugin:checkLicenseMain`.

**Architecture:** Keep the change purely mechanical and scoped to license compliance. Use the existing Gradle licenser configuration and project `HEADER.txt` as the source of truth, update the flagged Java files to match project expectations, and verify by rerunning the exact failing build tasks.

**Tech Stack:** Gradle 8.14.2, Java 21, cadix licenser plugin.

---

### Task 1: Reproduce and scope the license failures

**TDD scenario:** Trivial change — use judgment.

**Files:**
- Read: `HEADER.txt`
- Read/Modify candidate set from failing task output:
  - `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/**/*.java`
  - `Plugin/src/main/java/ua/nanit/limbo/**/*.java`

**Step 1: Re-run the failing task**

Run: `./gradlew :Plugin:checkLicenseMain`
Expected: FAIL with the current list of violating files.

**Step 2: Inspect representative files**

Read representative doctor and limbo files plus `HEADER.txt` to confirm the mismatch type (missing header vs incompatible header).

**Step 3: Confirm project-supported remediation path**

Run: `./gradlew tasks --all | grep -i license`
Expected: identify whether the licenser plugin exposes a header-formatting/update task for Java sources.

**Step 4: Decide minimal remediation**

Prefer the existing project/plugin mechanism to apply canonical headers. Only do manual edits if the automated task cannot safely handle the flagged files.

**Step 5: Commit scope checkpoint**

Do not commit yet; move to implementation once the remediation path is confirmed.

### Task 2: Apply canonical headers to violating files

**TDD scenario:** Trivial change — use judgment.

**Files:**
- Modify: the exact files currently reported by `:Plugin:checkLicenseMain`, especially:
  - `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/*.java`
  - `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor/checks/*.java`
  - `Plugin/src/main/java/ua/nanit/limbo/**/*.java`

**Step 1: Apply headers using project tooling**

Run the project’s license-update task if available (for example `./gradlew updateLicenses`), or otherwise edit the flagged files to match `HEADER.txt` exactly where the project expects that canonical header.

**Step 2: Review the diff**

Run: `git diff -- Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor Plugin/src/main/java/ua/nanit/limbo`
Expected: only license-header changes unless the tool reformats whitespace.

**Step 3: Verify no unintended code changes**

Inspect a few representative files from each affected area to ensure only header content changed.

**Step 4: Run focused license verification**

Run: `./gradlew :Plugin:checkLicenseMain`
Expected: PASS.

**Step 5: Commit**

```bash
git add Plugin/src/main/java/xyz/xreatlabs/nexauth/common/doctor \
        Plugin/src/main/java/ua/nanit/limbo
git commit -m "fix: restore plugin build license compliance"
```

### Task 3: Full verification of the restored build

**TDD scenario:** Trivial change — use judgment.

**Files:**
- No further source changes unless verification exposes another real bug.

**Step 1: Re-run plugin tests**

Run: `./gradlew :Plugin:test`
Expected: PASS.

**Step 2: Re-run full plugin build**

Run: `./gradlew :Plugin:build`
Expected: BUILD SUCCESSFUL.

**Step 3: Inspect git status**

Run: `git status --short`
Expected: clean working tree except for intentionally retained local/untracked files like `.pi/` if still present.

**Step 4: Request review**

Request code review for the license/header-only diff before any merge or push.

**Step 5: Summarize remaining bug status**

Report whether any additional concrete failures remain after the build blocker is fixed, based on fresh verification output rather than speculation.
