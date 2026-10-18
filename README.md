# Velora

A lightweight client-side Minecraft utility focused on customization and convenience.

**Author:** JRY  **Mod ID:** `velora`  **Loader:** Fabric  **Side:** client only

Velora is a **client-side visual customization and testing mod**. It changes how the
nine slots of a dispenser *look* on your own screen. It never changes what the server
sent you, and it never sends anything to the server.

---

## Supported versions

| Component    | Version                                  |
|--------------|------------------------------------------|
| Minecraft    | **1.21.11** (targeted for this build)          |
| Fabric Loader| 0.18.1 or newer                         |
| Fabric API   | 0.141.6+1.21.11 (required)                |
| Java         | 21                                       |
| Mod Menu     | optional (13.x)               |

Other 1.21.x versions: change the versions in `gradle.properties` (get matching values from
<https://fabricmc.net/develop/>) and the `minecraft` range in `fabric.mod.json`. Minecraft's
GUI rendering API shifts between 1.21.x releases, so a few calls in
`VeloraRenderer`, `HandledScreenMixin` and the screens may need small updates.

## Building

```
./gradlew build
```

The finished jar is written to `build/libs/` (use `velora-1.1.2.jar`, not the `-sources` jar).
The first build downloads Minecraft, mappings and dependencies, so it needs internet access.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11.
2. Put **Fabric API** and `velora-1.1.2.jar` in your `.minecraft/mods` folder.
3. Optional: add **Mod Menu** to configure Velora from the mod list.
4. Launch the game.

## Opening the settings

- **Mod Menu:** open *Mods*, select **Velora**, and press the config button.
- **Keybind:** press **V** (default) while in the world with no screen open. Rebind it under
  *Options -> Controls -> Key Binds*, category **Velora**, entry **Open Velora Settings**.

Settings save automatically to `config/velora.json` and persist between launches. You never
need to edit the file by hand. The screen shows the **active mapping** (real slot -> shown as)
for your current settings.

## Modes

### Item Names

Pick two **different** items:

- **Item 1** - lower numbers
- **Item 2** - higher numbers

When Velora finds one of these items in a dispenser, it shows a local number in the slot
corner and in the hover tooltip (it replaces only the first line, the name). The item itself
stays, for example, Paper. The item picker greys out the item already used by the other
role, so the two can never match.

Items are recognized by their real item id; the slot position only decides which number is shown.

### Maps

Velora recognizes the nine server maps by their real map ids:

| # | Map ID  | # | Map ID  | # | Map ID  |
|---|---------|---|---------|---|---------|
| 1 | 5821839 | 4 | 5837266 | 7 | 5847472 |
| 2 | 5825443 | 5 | 5840048 | 8 | 5853260 |
| 3 | 5828512 | 6 | 5843929 | 9 | 5857126 |

A recognized map in a dispenser slot is covered by local artwork (`assets/velora/maps/map_N.png`)
chosen by the slot and the Display Pattern, and hovering it shows a larger preview. The IDs are
only used to recognize the map; they are never changed or sent anywhere. Maps that are not one of
the nine are left alone.

## Display Patterns

The pattern maps the real slot (1-9) to the number shown:

| Pattern   | Slot 1-9 shown as    |
|-----------|----------------------|
| Normal    | 1 2 3 4 5 6 7 8 9    |
| Pattern A | 1 2 3 4 1 2 3 4 5    |
| Pattern B | 5 6 7 8 5 6 7 8 9    |

- **Maps mode:** the selected pattern is applied directly to all recognized maps.
- **Item Names mode:** Item 1 (lower) follows *Normal* and *Pattern A*; Item 2 (higher) follows
  *Normal* and *Pattern B*. Selecting the pattern that belongs to the other item falls back to
  Normal for that item.

## Map 9 Local Override

A toggle (used in Maps mode). **ON** draws Map 9's artwork from
`assets/velora/maps/map_9_override.png`; **OFF** uses the normal `map_9.png`.

To replace the image, either:

- put your own PNG at `assets/velora/maps/map_9_override.png` inside the jar (or in the project
  before building), or
- ship the same path in a resource pack (`assets/velora/maps/map_9_override.png`).

Use a square PNG, ideally 128x128. This is a client-side rendering feature only.

## Artwork note

The bundled icon and map artwork are generated placeholders styled as a purple star and numbers
1-9. To use your own art, replace the PNGs at the same names under
`src/main/resources/assets/velora/` (`icon.png` and `maps/map_1.png` ... `map_9.png`,
`map_9_override.png`). The icon has no text on it.

## Client-side only, and the server is never modified

- The mod is `environment: client`; it does nothing on a server.
- **Actual data** (the real ItemStack, item id, name, map id, map data, slot, server state) is
  only ever *read*. **Display data** (label, number, artwork) is created fresh each frame,
  used for drawing, and thrown away.
- Velora does **not**: modify ItemStacks, inventories, map ids, NBT/components, dispenser
  contents, game outcomes or random results; send any packet or fake item data; or change which
  slot is clicked.
- Clicks are untouched. If slot 5 *looks* like "1", clicking it still clicks real slot 5.
- Because nothing is cached, server inventory updates show up on the next frame.
- Only dispenser screens are affected (droppers and other containers look vanilla). New
  container types can be added by implementing `DisplayScope`.

## How it works (for developers)

```
HandledScreenMixin   render-only hooks: drawSlot (TAIL) and drawMouseoverTooltip (HEAD)
        |
DisplayResolver      reads the real stack, returns a SlotDisplay (DISPLAY DATA) or nothing
        |  uses
DisplayScope         which containers/slots are covered (DispenserScope)
DisplayPattern       slot -> number mappings
MapIds / MapArt      map id recognition / local artwork lookup
        |
VeloraRenderer       draws the number or artwork on top of the slot / next to the cursor
```

## License

MIT - see `LICENSE`.


## Dropper behavior in 1.1.2

The nine slots inside a vanilla dropper keep their vanilla names and their original order. The configured local display pattern can still apply to eligible player inventory/hotbar slots shown in the same screen. Velora never rearranges slots, edits item stacks, changes server inventory data, or sends packets for these display rules.
