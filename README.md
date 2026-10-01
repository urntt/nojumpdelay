# nojumpdelay

A simple client-side Fabric mod for Minecraft: Java Edition that removes the player's jump cooldown.

In vanilla Minecraft, holding the jump key waits 10 ticks between jumps. This matters whenever a jump ends early, for example when you hit your head on a low ceiling. With this mod, you jump again as soon as you land. Only your own player is affected.

## Multiplayer warning

This mod changes player movement. Servers that run anti-cheat systems may detect it, which can get your movement set back, get you kicked, or get you banned, and using it may break a server's rules. Check each server's rules before joining with this mod enabled. Use it in multiplayer at your own risk.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api).
2. Download the jar from this repository's [Releases](https://github.com/urntt/nojumpdelay/releases) page. Each release supports a single Minecraft version, shown after the `+` in its version number. For example, `1.0.0+26.3` is for Minecraft 26.3.
3. Put the jar into your `.minecraft/mods` folder.

[Mod Menu](https://modrinth.com/mod/modmenu) is optional. When installed, it adds a configuration screen for the mod.

## Usage

The mod is enabled by default.

To turn it on or off in game, bind the **Toggle No Jump Delay** key in **Options → Controls → Key Binds**. It is unbound by default. Each press shows the new state on the action bar.

The setting is saved to `config/nojumpdelay.json`, so it persists across game restarts. You can also change it from the Mod Menu configuration screen.

## Development

Building requires the JDK version set by `java_version` in `gradle.properties`.

Build the mod:

```bash
./gradlew build
```

The jar is written to `build/libs/`.

Run the client game tests, which start Minecraft, create a test world, and check the jump behavior, the toggle key, and the saved configuration:

```bash
./gradlew runClientGameTest
```

The game tests need a display. On a headless Linux machine, run them under Xvfb. Xvfb offers no sRGB-capable OpenGL visuals, so install Mesa's Vulkan driver (`mesa-vulkan-drivers` on Ubuntu) for the game to fall back to:

```bash
xvfb-run -a -s "-screen 0 1920x1080x24" ./gradlew runClientGameTest
```

Screenshots taken by the tests are saved to `build/run/clientGameTest/screenshots/`.

## License

[MIT](LICENSE)
