# The Abyss: The Other Side — NeoForge and Fabric 26.3

Port of the Minecraft mod *The Abyss: The Other Side* (TATOS 1.0.5_beta, originally Forge 1.20.1 / MCreator)
to **Minecraft 26.3** (Java 25) for two loaders. Mod id: `theabyss`.

| Loader | Version | Jar |
|---|---|---|
| NeoForge | built against 26.3.0.101, requires 26.3.0.80 or newer | `neoforge/build/libs/theabyss-<version>.jar` |
| Fabric | Loader 0.19.5+, Fabric API 0.162.0+26.3 | `fabric/build/libs/theabyss-fabric-<version>.jar` |

Both need GeckoLib 5.5.7+ for 26.3. JEI is optional. The Fabric jar bundles Forge Config API Port for the config file.

## Requirements

- Any JDK 21+ to start Gradle. Gradle itself runs on and downloads **Java 25** (foojay toolchain resolver,
  `gradle/gradle-daemon-jvm.properties`), so no manual JDK 25 install is needed.
- Internet access on the first build (Minecraft, NeoForge, Fabric, GeckoLib, JEI).

## Build and run

**Windows, quickest way:** double-click `start.bat`. Menu: start the client with NeoForge or Fabric, build the NeoForge, the
Fabric or both jars (copied to `dist/`), automated NeoForge test, NeoForge test server, compile check of all modules.

```bash
./gradlew :neoforge:build :fabric:build   # both jars (both loaders must always build)
./gradlew :neoforge:runClient             # NeoForge client, game dir: run/
./gradlew :fabric:runClient               # Fabric client, game dir: run-fabric/
./gradlew :neoforge:runServer             # NeoForge dedicated server, game dir: run/
./gradlew :fabric:runServer               # Fabric dedicated server, game dir: run-fabric-server/
```
On Windows use `gradlew.bat` (or `.\gradlew` in PowerShell).

### Development checks (not shipped)

```bash
./gradlew :neoforge:runDevTestClient -PdevtestScenario=all   # scripted NeoForge client run, screenshots in run-devtest/screenshots/
./gradlew :neoforge:runDevTestServer                         # NeoForge server with /abysstest
./gradlew :neoforge:runCheckServer / :fabric:runCheckServer  # start checks in run-check/<loader>-server
./gradlew :fabric:runCheckClient                             # Fabric client joins run-check/fabric-client/saves/check, quits after 45 s
```
The Fabric check runs load a small dev-only mod (`fabric/src/check`) that runs the Mixin audit (every mixin target is loaded,
failures are logged as `[ABYSS-CHECK] ... FAILED`). The NeoForge devtest mod does the same.

## Project layout

| Path | Content |
|---|---|
| `common/src/main/java/net/yezon/theabyss/` | The whole mod, compiled against plain vanilla 26.3 (package and class names kept from 1.20.1) |
| `common/.../theabyss/platform/` | Loader-neutral API used by the mod: event bus and event classes, registries, network, attachments, `Platform` and `services/*Hooks` interfaces implemented by each loader |
| `common/src/main/resources/` | Assets and data shared by both loaders |
| `neoforge/` | NeoForge entry point (`TheabyssNeoForge`), NeoForge implementations of the platform API, generated NeoForge event bridge, NeoForge mixins, `neoforge.mods.toml` template, devtest mod |
| `fabric/` | Fabric entry points (`TheabyssFabric`, `TheabyssFabricClient`), Fabric implementations of the platform API, the mixins that fire every event at the spot where NeoForge fires it, Fabric-only resources (dimension types without NeoForge attributes, recipes with Fabric ingredients, bucket models), `fabric.mod.json` |

Rules for changes: the common code must compile against vanilla. A new `@EventBusSubscriber` class has to be added to
`TheabyssSubscribers` (both sides) or `client/TheabyssClientSubscribers` (client only). A NeoForge event that is used for
the first time needs a class in `platform/`, a NeoForge bridge and a Fabric hook.
