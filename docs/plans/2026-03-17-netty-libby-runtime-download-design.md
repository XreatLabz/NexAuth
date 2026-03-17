# Netty Libby Runtime Download Design

**Problem:** `Plugin/build/libs/NexAuth.jar` is roughly 18 MB because Netty is currently shaded directly into the plugin artifact.

**Observed evidence:**
- `io/netty/**` entries in the shaded jar: 3563
- `META-INF/native/**` entries in the shaded jar: 16
- `Plugin/build.gradle.kts` currently declares `implementation("io.netty:netty-all:4.2.2.Final")`
- Existing `libby(...)` dependencies are already emitted to `Plugin/build/libby/libby.json` and are not being shaded into the final jar

## Architecture

NexAuth already has the right runtime-loading architecture for this change. Paper, Bungee, and Velocity bootstrap classes all call `libraryManager.configureFromJSON()` before constructing the main runtime plugin objects. Because Libby metadata is already generated during the build, Netty can follow the same path as the project’s other runtime-delivered libraries.

The design is therefore intentionally small: remove Netty from the shaded runtime classpath, keep it available to the compiler, and add it to Libby metadata generation. The build should switch from a shaded `implementation("io.netty:netty-all:4.2.2.Final")` declaration to `compileOnly("io.netty:netty-all:4.2.2.Final")` plus `libby("io.netty:netty-all:4.2.2.Final")`.

## Data Flow

1. Gradle compiles the plugin against `netty-all` through `compileOnly`.
2. The Libby Gradle plugin writes Netty coordinates into `Plugin/build/libby/libby.json`.
3. At runtime, each bootstrap invokes `configureFromJSON()` before creating the main plugin object.
4. Libby downloads Netty into the server runtime library area and makes it available before NexAuth initialization continues.
5. The shaded plugin jar no longer contains embedded Netty classes or native binaries.

## Error Handling

No bootstrap changes are planned unless verification proves a load-order issue. If Libby fails at runtime, existing bootstrap error handling remains the authority. Paper already logs and stops startup on library-load failure. Bungee and Velocity already rely on successful `configureFromJSON()` before creating their runtime plugin classes.

## Testing and Verification

Verification must prove both build-size reduction and startup safety:
- rebuild `:Plugin:shadowJar`
- compare final jar size
- confirm `io/netty/**` count is zero
- confirm `META-INF/native/**` count is zero
- confirm `Plugin/build/libby/libby.json` contains `netty-all`
- reconfirm bootstrap load order still places `configureFromJSON()` before runtime plugin construction
- run the project test suite to ensure the dependency change does not break compilation or tests
