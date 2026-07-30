# SlumberSignal

Client-side Forge mod for **Minecraft 26.1.2**. It tells you the exact moment the Overworld
night becomes dark enough to use a bed, with a deliberately over-animated notification.

`you can sleep now.`

## Features

| # | Behaviour |
|---|-----------|
| 1 | Detects the moment sleeping becomes possible, using the same rule the server uses (`BED_RULE` environment attribute -> `BedRule.canSleep`), so thunderstorms count too |
| 2 | Only fires while the player is in the **Overworld** |
| 3 | Shows `you can sleep now.` |
| 4 | Fires **once per night** - never spams |
| 5 | Re-arms on dimension change, world change, join and disconnect |
| 6 | Heavily animated HUD: flash, breathing vignette, two drifting star layers, three shockwave rings, a pulsing bloom, an elastic sliding plate with scrolling parallax art and a shine sweep, four popping corner ornaments, a bobbing shining wordmark, a breathing tilting moon, a hopping squash-and-stretch bed, looping rising Zzz, ten orbiting sparkle sprites cycling a 2x2 sheet, per-letter typed + waving + colour-shifting text, and a countdown bar |
| 7 | 17 bundled images (icon, cover, banner, plate, wordmark, moon, bed, Zzz, sparkle sheet, glow, ring, shine, stars, vignette, splash strip) |
| 8 | **No config file is ever created** - all state is in memory |

Client only: `clientSideOnly=true`, no server entrypoint, no packets, no mixins.

## Build

```bash
./gradlew build
# -> build/libs/slumbersignal-<version>.jar
```

Requires JDK 25.

## Versioning

`MAJOR.MINOR.PATCH+mc<minecraft_version>` - the version lives only in `gradle.properties`
(`mod_version`); `META-INF/mods.toml` picks it up through `${version}`.

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
