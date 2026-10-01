# AGENTS.md

Instructions for AI coding agents working in this repository. `CLAUDE.md` imports this file, so keep all agent guidance here.

## Project

`nojumpdelay` is a client-side [Fabric](https://fabricmc.net/) mod for Minecraft: Java Edition that removes the local player's jump cooldown (the `LivingEntity.noJumpDelay` timer).

The archived predecessor [urntt/nojumpdelay-legacy](https://github.com/urntt/nojumpdelay-legacy) may be consulted for verified 26.x API usage. Do not copy its build configuration, which predates the current Fabric template, or its package layout.

## Project decisions

These decisions are settled. Do not deviate from them without the user's explicit approval.

### Minecraft and toolchain

- Development started on Minecraft 26.3 with Java 25. Once the project is scaffolded, `gradle.properties` is the single source of truth for the Minecraft, Fabric Loader, Loom, and Fabric API versions; do not restate them elsewhere.
- Follow the latest official Fabric template ([FabricMC/fabric-example-mod](https://github.com/FabricMC/fabric-example-mod), also available from the [template generator](https://fabricmc.net/develop/template/)): the `net.fabricmc.fabric-loom` Gradle plugin, Mojang's official names with no `mappings` dependency, and `implementation` (not `modImplementation`) for dependencies. Do not use Yarn.
- Target only the latest stable (release) Minecraft version. Updates, fixes, and new features are always developed against it. Snapshots, pre-releases, and release candidates are not supported targets.
- Do not maintain older Minecraft versions and do not set up multi-version builds (no per-version branches, no Stonecutter or other preprocessors). When a new stable version is released, port the mod to it and drop the previous one.

### Identity

| Item | Value |
| --- | --- |
| Mod ID | `nojumpdelay` |
| Display name | `nojumpdelay` |
| Maven group | `com.urntt` |
| Base package | `com.urntt.nojumpdelay` |
| Version format | `<SemVer>+<Minecraft version>`, for example `1.0.0+26.3` |
| License | MIT |

The mod version itself follows [Semantic Versioning](https://semver.org/); the `+<Minecraft version>` suffix is build metadata naming the Minecraft version the build targets. Version numbers start at `1.0.0`, independent of the predecessor's releases.

### Distribution

- Releases are published only as GitHub Releases. Do not publish to Modrinth, CurseForge, or any other mod platform, and do not add publishing tooling for them.
- `README.md` must clearly warn that this is a movement modification: it may conflict with server anti-cheat systems, and using it in multiplayer may get the player set back, kicked, or banned.

### Scope and behavior

- Client-only: `fabric.mod.json` declares `"environment": "client"`. There is no server-side component and no networking.
- The mod only removes the jump cooldown. Other movement features are out of scope; in particular, air jump is planned as a separate mod and must not be added here.
- Only the local player (`LocalPlayer`) is affected. The jump behavior of every other entity, including other players and mobs simulated on the client, must stay vanilla.
- The feature is enabled by default.
- A configurable key binding toggles the feature. It is unbound by default. Each toggle shows the new state on the action bar and saves it to the configuration file, so it persists across game restarts.

### Localization

- All user-facing text, including key binding names, the key binding category, action bar messages, and the configuration screen, uses translation keys. Never hard-code display strings.
- Provide translations for `en_us` and `zh_cn`, and keep both complete whenever a translation key is added or changed.

### Dependencies

- Required: Fabric Loader and Fabric API.
- Optional: Mod Menu, declared under `suggests` in `fabric.mod.json`. The mod must load and work normally without it, so Mod Menu classes may only be referenced from the Mod Menu entrypoint.
- Configuration is hand-written without a config library: a JSON file in the Fabric config directory, serialized with Gson (bundled with Minecraft). Any configuration screen uses vanilla widgets.
- Do not add other dependencies without the user's explicit approval.

### Implementation

- Language: Java only.
- Mixins: prefer the MixinExtras injectors bundled with Fabric Loader (for example `@ModifyExpressionValue` and `@WrapOperation`) over `@Redirect` and `@Overwrite`, to stay compatible with other mods and keep porting work small.

### CI, releases, and changelog

- GitHub Actions builds the project on every push and pull request.
- Release builds are attached to GitHub Releases.
- Maintain `CHANGELOG.md` following [Keep a Changelog](https://keepachangelog.com/). Record every user-visible change under `Unreleased` in the same change that introduces it.

## Engineering principles

- Fix root causes, not symptoms. Diagnose the underlying cause before implementing a permanent fix. If an immediate mitigation is necessary, treat it as temporary and follow through with a root-cause fix.
- Prefer configuration-driven design for values that are expected to vary by environment, deployment, or product requirements. Avoid unexplained or duplicated magic values, but do not introduce configuration where a well-named constant is the clearer source of truth.
- Preserve a single source of truth and clear ownership for data, state, configuration, business logic, and authoritative documentation. Avoid duplicating canonical information across multiple locations.
- Do not maintain parallel legacy and replacement implementations without an explicit migration and removal plan.

## Documentation

- Keep documentation aligned with the code. When a code change affects documented behavior, APIs, architecture, configuration, workflows, or usage, update the relevant documentation in the same change.
- Keep each document's responsibility clear. For example, use `README.md` for project overview and usage, and `VISION.md` for product direction, architectural principles, or long-term decisions.
- Always specify a language identifier for fenced code blocks in Markdown.

## Language

- Communicate with the user in Chinese, including explanations, progress updates, and user-facing planning.
- Use English for development artifacts, including source code, comments, docstrings, documentation, READMEs, Git branch names, commit messages, and other deliverables intended to live in the repository.

## Git

- Do not change or override the Git author or committer identity. When an identity must be configured for commits created during the task, use:
  - Name: `urntt`
  - Email: `urntts@gmail.com`
- Do all actions on the user's behalf. Do not rewrite existing commit authorship unless explicitly requested. Do not add `Co-Authored-By` trailers or session links to commit messages or pull request descriptions.
- Use category-based branch prefixes that reflect the purpose of the change, such as `feat/`, `fix/`, `refactor/`, `docs/`, `test/`, or `chore/`.
- Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification for commit messages.
