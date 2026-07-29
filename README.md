# SlumberSignal

Client-side Fabric mod for **Minecraft 26.1.2**. It tells you the exact moment the Overworld night becomes dark enough to use a bed, with a deliberately over-animated notification.

`you can sleep now.`

## Features

| # | Behaviour |
|---|-----------|
| 1 | Detects the moment sleeping becomes possible, using the same rule the server uses (`BED_RULE` environment attribute -> `BedRule.canSleep`), so thunderstorms count too |
| 2 | Only fires while the player is in the **Overworld** |
| 3 | Shows `you can sleep now.` |
| 4 | Fires **once per night** — never spams |
| 5 | Re-arms on dimension change, world change, join and disconnect |
| 6 | Heavily animated HUD: breathing vignette, two drifting star layers, three shockwave rings, a pulsing bloom, an elastic sliding plate with scrolling parallax art and shine sweep, four popping corner ornaments, a bobbing shining wordmark, a breathing tilting moon, a hopping squash-and-stretch bed, looping rising Zzz, ten orbiting sparkle sprites, per-letter typed + waving + colour-shifting text, and a countdown bar |
| 7 | 17 bundled images (icon, cover, banner, plate, wordmark, moon, bed, Zzz, sparkle sheet, glow, ring, shine, stars, vignette, splash strip) |
| 8 | **No config file is ever created** — all state is in memory |

Client only: `"environment": "client"`, no server entrypoint, no packets, no mixins.

## Build

```bash
./gradlew build
# -> build/libs/slumbersignal-<version>.jar
```

Requires **JDK 25**.

## Version Scheme

`MAJOR.MINOR.PATCH+mc<minecraft_version>` — the version lives only in `gradle.properties` (`mod_version`); `fabric.mod.json` picks it up through `${version}`.

| Component | When to increment | Notes |
|-----------|-------------------|-------|
| **MAJOR** | Breaking change — old saves or configs no longer work | Reset MINOR and PATCH to 0 |
| **MINOR** | New feature added, everything old still works | Reset PATCH to 0 |
| **PATCH** | Bug-fix-only, no new features, nothing broken | |
| **+mc\*** | Minecraft version this build targets | No effect on version comparison |

Only one position is incremented per release, matching the **biggest change** in that release. If a release contains both bug fixes and new features, only **MINOR** is incremented — a new feature always outranks a bug fix.

## Layout

```
src/main/java/com/slumbersignal/
  SlumberSignal.java        mod id, logger, message
  SlumberSignalClient.java  entrypoint: events + HUD registration
  SleepWatcher.java         night detection + once-per-night state
  hud/SsTextures.java       every bundled image
  hud/Gfx.java              easing / colour / blit helpers
  hud/SlumberToast.java     the whole animation timeline
```

## Technical Details

| | |
|---|---|
| **Mod Loader** | Fabric |
| **Minecraft** | 26.1.2 |
| **Dependencies** | Fabric API, Fabric Loader (>= 0.19.3) |
| **License** | MIT |
| **Environment** | Client only |

> **Source code only.** There is no issue tracker in this repository. To report a bug, request a feature, or send feedback, email **mattrixthai9911@zohomail.com**.

---

*This mod was made with the assistance of AI tools, in Thailand.*
