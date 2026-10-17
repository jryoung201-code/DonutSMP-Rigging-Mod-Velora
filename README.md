# Velora

A lightweight client-side Minecraft utility focused on customization and convenience.

## Requirements

- Minecraft Java Edition 1.21.11
- Fabric Loader 0.18.1 or newer
- Fabric API
- Java 21 to build

## Build

Run `gradle build --no-daemon` from the project root. The mod JAR is written to `build/libs/`.

## Settings

Press **Right Shift** in game to open Velora Settings. The starter source includes a saved pattern selector and a local visual preview for Normal and A patterns. The preview is a visual test UI and does not modify server inventory data.

Configuration is stored in `.minecraft/config/velora.json`.

## Automatic releases

Push a tag matching `v*.*.*` (for example `v1.1.1`) to trigger a GitHub Release. The workflow builds the source and attaches only the compiled mod JAR, excluding the sources JAR. The uploaded release artifact should be the versioned mod JAR, not the source archive.
