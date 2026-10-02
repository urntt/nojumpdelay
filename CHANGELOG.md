# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html). Each version carries the targeted Minecraft version as build metadata, for example `1.0.0+26.3`.

## [Unreleased]

### Added

- Add a configuration screen built from vanilla widgets, with separate defaults for singleplayer worlds and allowed servers, and options to reset to the default on world exit or game exit.
- Add multiplayer modes (disabled, whitelist, blacklist) and a server list screen for editing the addresses they use.
- Add an "Open nojumpdelay Settings" key binding, unbound by default, so the configuration screen is available without Mod Menu.

### Changed

- The mod is now disabled on multiplayer servers by default. To use it on a server, choose the whitelist or blacklist mode on the configuration screen. The toggle key no longer turns the mod on where the multiplayer mode rules it out.

## [1.0.0+26.3] - 2026-10-01

### Added

- Remove the local player's jump cooldown, so holding the jump key jumps again as soon as the player lands.
- Add a key binding, unbound by default, that toggles the feature and shows the new state on the action bar.
- Save whether the feature is enabled to `config/nojumpdelay.json`. The feature is enabled by default.
- Add a configuration screen for Mod Menu, which is optional.
- Add English and Simplified Chinese translations.
