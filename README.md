# SupaNightvision

A lightweight night-vision toggle plugin for Paper and Purpur.

## Features

- `/nightvision` and `/nv` toggle night vision.
- Hides potion particles and the effect icon.
- Reapplies night vision after respawning and drinking milk.
- Saves each player's toggle state in `plugins/SupaNightvision/players.yml`, including across restarts.
- Configurable chat/action-bar messages with MiniMessage formatting, hex colors, and gradients.
- No database or external plugin dependencies.

## Requirements

- Minecraft 1.21.11
- Java 21
- Paper or Purpur

## Commands

| Command | Description |
| --- | --- |
| `/nightvision` | Toggle night vision |
| `/nv` | Alias for `/nightvision` |

Permission: `supanightvision.use` (enabled for all players by default).

## Configuration

Edit `plugins/SupaNightvision/config.yml` to customize the messages and whether they appear in chat and/or the action bar. Message strings use MiniMessage syntax, for example `<#A8A8A8>ᴛᴇxᴛ`.

## Build

Run `mvn clean package` with Java 21. The release JAR is created at `target/SupaNightvision-1.0.0.jar`.

GitHub Actions builds the JAR on pushes to `main` and on manual workflow runs. Download it from the workflow run's **Artifacts** section.
