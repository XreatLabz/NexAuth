# Netty Libby Runtime Download Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Remove Netty from the shaded plugin artifact and load it through Libby at runtime so `Plugin/build/libs/NexAuth.jar` becomes significantly smaller.

**Architecture:** Keep Netty available to the compiler, but stop placing it on the shaded runtime classpath. Declare Netty through the existing Libby metadata path so `libraryManager.configureFromJSON()` downloads it during bootstrap before the main plugin classes are constructed. Verify the resulting artifact no longer contains `io/netty` classes or Netty native binaries under `META-INF/native`.

**Tech Stack:** Gradle Kotlin DSL, ShadowJar, Libby, Java 21, Paper/Bungee/Velocity bootstrap loaders.

---

### Task 1: Move Netty off the shaded runtime classpath

**TDD scenario:** Trivial change — use judgment

**Files:**
- Modify: `Plugin/build.gradle.kts:211`
- Verify: `Plugin/build/libby/libby.json`

**Step 1: Change the Netty dependency declaration**

Replace the direct shaded dependency:

```kotlin
implementation("io.netty:netty-all:4.2.2.Final")
```

with compile-time + Libby declarations:

```kotlin
compileOnly("io.netty:netty-all:4.2.2.Final")
libby("io.netty:netty-all:4.2.2.Final")
```

This preserves compilation while removing Netty from the Shadow runtime classpath and adding it to runtime-download metadata.

**Step 2: Keep the existing bootstrap flow unchanged**

Do not edit the bootstrap loaders unless the build proves Netty still leaks into the shaded jar. `PaperBootstrap`, `BungeeCordBootstrap`, and `VelocityBootstrap` already call `configureFromJSON()` before constructing the main plugin runtime objects.

**Step 3: Rebuild Libby metadata and shaded jar**

Run:

```bash
./gradlew :Plugin:shadowJar
```

Expected:
- Build succeeds
- `Plugin/build/libby/libby.json` includes `io{}netty` / `netty-all`
- `Plugin/build/libs/NexAuth.jar` is recreated

**Step 4: Commit**

```bash
git add Plugin/build.gradle.kts docs/plans/2026-03-17-netty-libby-runtime-download.md
git commit -m "build: move netty to libby runtime download"
```

---

### Task 2: Verify the shaded artifact actually shrank

**TDD scenario:** Trivial change — use judgment

**Files:**
- Verify: `Plugin/build/libs/NexAuth.jar`

**Step 1: Inspect output size**

Run:

```bash
ls -lh Plugin/build/libs/NexAuth.jar
```

Expected:
- Jar size is materially smaller than the previous ~18 MB artifact

**Step 2: Confirm Netty classes are not embedded**

Run:

```bash
python3 - <<'PY'
import zipfile
path='Plugin/build/libs/NexAuth.jar'
with zipfile.ZipFile(path) as z:
    names=z.namelist()
    print('io/netty entries:', sum(1 for n in names if n.startswith('io/netty/')))
    print('META-INF/native entries:', sum(1 for n in names if n.startswith('META-INF/native/')))
PY
```

Expected:
- `io/netty entries: 0`
- `META-INF/native entries: 0`

**Step 3: Confirm Libby metadata includes Netty**

Run:

```bash
grep -n "netty-all" Plugin/build/libby/libby.json
```

Expected:
- One Netty entry appears in the generated Libby metadata

**Step 4: Commit**

```bash
git add Plugin/build.gradle.kts docs/plans/2026-03-17-netty-libby-runtime-download.md
git commit -m "test: verify netty removed from shaded jar"
```

---

### Task 3: Smoke-check build wiring for startup safety

**TDD scenario:** Modifying tested code — run existing tests first

**Files:**
- Verify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/paper/PaperBootstrap.java`
- Verify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/bungeecord/BungeeCordBootstrap.java`
- Verify: `Plugin/src/main/java/xyz/xreatlabs/nexauth/velocity/VelocityBootstrap.java`

**Step 1: Reconfirm library load order**

Run:

```bash
grep -n "configureFromJSON\|new PaperNexAuth\|new BungeeCordNexAuth\|new VelocityNexAuth" \
  Plugin/src/main/java/xyz/xreatlabs/nexauth/paper/PaperBootstrap.java \
  Plugin/src/main/java/xyz/xreatlabs/nexauth/bungeecord/BungeeCordBootstrap.java \
  Plugin/src/main/java/xyz/xreatlabs/nexauth/velocity/VelocityBootstrap.java
```

Expected:
- `configureFromJSON()` appears before construction of the main runtime plugin objects in each bootstrap

**Step 2: Record verification evidence for final summary**

Capture:
- final jar size
- absence of embedded Netty/native entries
- presence of Netty in `libby.json`
- bootstrap order evidence

**Step 3: Commit**

```bash
git add Plugin/build.gradle.kts docs/plans/2026-03-17-netty-libby-runtime-download.md
git commit -m "docs: record netty libby runtime-download plan"
```
