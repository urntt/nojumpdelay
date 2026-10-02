# nojumpdelay

A simple client-side Fabric mod for Minecraft: Java Edition that removes the player's jump cooldown.

In vanilla Minecraft, holding the jump key waits 10 ticks between jumps. This matters whenever a jump ends early, for example when you hit your head on a low ceiling. With this mod, you jump again as soon as you land. Only your own player is affected.

## Multiplayer warning

**This mod is disabled in multiplayer by default. To use it in multiplayer, change the settings on its configuration screen.**

This mod changes player movement. Servers that run anti-cheat systems may detect it, which can get your movement set back, get you kicked, or get you banned, and using it may break a server's rules. Check each server's rules before joining with this mod enabled. Use it in multiplayer at your own risk.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api).
2. Download the jar from this repository's [Releases](https://github.com/urntt/nojumpdelay/releases) page. Each release supports a single Minecraft version, shown after the `+` in its version number. For example, `1.0.0+26.3` is for Minecraft 26.3.
3. Put the jar into your `.minecraft/mods` folder.

[Mod Menu](https://modrinth.com/mod/modmenu) is optional. When installed, it opens the mod's configuration screen from its mod list.

## Usage

The mod is enabled by default in singleplayer and disabled on multiplayer servers.

The mod adds two key bindings in **Options → Controls → Key Binds**, both unbound by default:

- **Toggle No Jump Delay** turns the mod on or off and shows the new state on the action bar. On a server that the multiplayer settings rule out, it only shows that the mod is disabled there.
- **Open nojumpdelay Settings** opens the configuration screen. With Mod Menu installed, you can also open it from the mod list.

### Settings

All settings are saved to `config/nojumpdelay.json` as soon as you change them.

| Setting | Default | Meaning |
| --- | --- | --- |
| No Jump Delay | On | The current state, the same one the toggle key switches. |
| Singleplayer Default | On | The state a reset restores in singleplayer worlds, including worlds you open to LAN. |
| Server Default | On | The state a reset restores on servers that the multiplayer mode allows. |
| Reset on World Exit | Off | Every world starts from its default state instead of keeping the last state. |
| Reset on Game Exit | Off | After restarting the game, the first world where the mod is allowed starts from its default state. |
| Multiplayer mode | Disabled | **Disabled**: never active on servers. **Whitelist**: active only on servers in the server list. **Blacklist**: active on all servers except those in the server list. |
| Server List | Empty | The addresses the whitelist and blacklist modes use. |

The multiplayer mode is a hard limit: on a server it rules out, the mod stays off whatever the current state is. Joining another player's LAN world or a Realm counts as multiplayer.

Server list entries are compared with the address you connect to, ignoring upper and lower case. An entry without a port, such as `mc.example.com`, matches the server on any port, while an entry with a port, such as `mc.example.com:25566`, matches only that port. The server list screen marks invalid addresses in red and does not save until they are fixed or removed.

## Development

Building requires the JDK version set by `java_version` in `gradle.properties`.

Build the mod:

```bash
./gradlew build
```

The jar is written to `build/libs/`.

Run the client game tests, which start Minecraft and check the jump behavior in singleplayer and on a local dedicated server, the key bindings, the reset rules, the multiplayer modes, and the saved configuration:

```bash
./gradlew runClientGameTest
```

The game tests need a display. On a headless Linux machine, run them under Xvfb. Xvfb offers no sRGB-capable OpenGL visuals, so install Mesa's Vulkan driver (`mesa-vulkan-drivers` on Ubuntu) for the game to fall back to:

```bash
xvfb-run -a -s "-screen 0 1920x1080x24" ./gradlew runClientGameTest
```

Screenshots taken by the tests are saved to `build/run/clientGameTest/screenshots/`.

The multiplayer tests start a local dedicated server, so the test setup in `build.gradle` accepts the [Minecraft EULA](https://aka.ms/MinecraftEULA) for that test server.

## License

[MIT](LICENSE)
