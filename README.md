# The Abyss: The Other Side — NeoForge 26.3

Port of the Minecraft mod *The Abyss: The Other Side* (TATOS 1.0.5_beta, originally Forge 1.20.1 / MCreator)
to **NeoForge 26.3.0.101** (requires 26.3.0.80 or newer) for **Minecraft 26.3** (Java 25). Mod id: `theabyss`.

## Requirements

- Any JDK 17+ to start Gradle (e.g. a JDK 21). Gradle itself downloads **Java 25** for compiling and running
  (foojay toolchain resolver), so no manual JDK 25 install is needed.
- Internet access on the first build (Minecraft, NeoForge, GeckoLib, JEI).

No MCreator installation is needed.

## Build and run

**Windows, quickest way:** double-click `start.bat`. It finds a JDK by itself and offers a menu: build, start client,
start server, automated test, test server, compile check.

```bash
./gradlew build            # mod jar -> build/libs/theabyss-<version>.jar
./gradlew runClient        # Minecraft client with the mod (+ JEI), game dir: run/
./gradlew runServer        # dedicated server (accept run/eula.txt once), game dir: run/
./gradlew runData          # data generators (none required at the moment)
```
On Windows use `gradlew.bat` (or `.\gradlew` in PowerShell). If your default `java` is older than 17, set `JAVA_HOME` to a
JDK 17+ first.

### Development test harness (not shipped)

```bash
./gradlew runDevTestClient   # scripted run: creates a test world, visits all dimensions, lines up all mobs,
                             # opens the workstation GUIs, runs /abysstest, saves screenshots to run-devtest/screenshots/, quits
./gradlew runDevTestServer   # dedicated server with the /abysstest command; type "abysstest all" in the console
python -I tools/compile_check.py         # fast javac-only check of src/main/java (needs ./gradlew writeCompileClasspath once)
```
`/abysstest all` logs `[ABYSS-TEST] ... RESULT <phase> PASS|FAIL` lines for registries, dimensions, blocks, data and entities.

## Project layout

| Path | Content |
|---|---|
| `src/main/java/net/yezon/theabyss/` | The mod (package and class names kept from 1.20.1) |
| `src/main/resources/` | Assets and data (26.3 formats) |
| `src/main/templates/META-INF/neoforge.mods.toml` | Mod metadata |
| `src/devtest/` | Dev-only test mod `theabyss_devtest` |
| `legacy/forge-1.20.1/` | Reconstructed original (Forge 1.20.1), compiling, for reference |
| `legacy/tools/` | Decompile/reconstruction pipeline |
| `tools/` | Compile check, server smoke test, region inspector, window screenshot/input helpers |
| `docs/` | Reconstruction report, porting guide, per-area notes, handover |

See `docs/HANDOVER.md` for the current state, changes and open points.
