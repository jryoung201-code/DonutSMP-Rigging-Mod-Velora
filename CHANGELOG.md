# Changelog

## 1.1.9-pre-2

- Continue the prerelease with the latest correction to use the displayed inventory number when calculating the Dropper's visual vacancy.
- Validate the item-identity mapping and visual slot permutation with the existing regression tests.
- Attach the compiled mod JAR to the GitHub prerelease when the `v1.1.9-pre-2` tag is pushed.

## 1.1.8f

- Apply the existing pattern by identity: for item number `n`, calculate displayed number `d` from the configured Item 1/Item 2 pattern array; in a Dropper, the item at slot `d - 1` is drawn at slot `n - 1`.
- Assign the visual vacancy to the mapped source position that was vacated, rather than to an arbitrary next-open position.
- Add regression coverage for the requested `1 → 5`, `2 → 6` examples and the remaining configured sequence.

## 1.1.8c

- Resolve Item Names mappings by the configured Item 1 and Item 2 registry identities for all inventory locations.
- Reuse the same identity-based mapping rule for container destinations, with unconfigured item types retaining their source position.
- Add unit coverage for Paper and Book mappings across different source slots.

## 1.1.8b

- Resolve one complete, deterministic permutation for all nine Dropper/Dispenser slots, including every empty slot.
- Map hit-tested visual slots back to their real source slots so pickup, shift-click, drag, release, and tooltip selection follow the displayed item without modifying inventory data.
- Keep duplicate destination handling stable: the earliest configured mapping wins, and remaining items/empty slots fill unclaimed destinations in slot order.

## 1.1.8

- Visually reposition numbered items in Dropper and Dispenser 3x3 layouts, allowing the vacant slot to appear at the corresponding mapped position.
- Offset only item icons and stack-count overlays; real slot contents, indices, hit targets, clicks, and server transfers remain unchanged.
- Resolve duplicate mapped destinations deterministically in slot order, then place remaining items and empty slots into unclaimed positions.

## 1.1.6

- Add configurable, local-only visual item swaps with source/display item selection, per-mapping enable controls, pagination, and reset support.
- Substitute only the stack passed to Minecraft's item model renderer; inventory contents and gameplay interactions continue using the original stack.
- Preserve the existing WIN/LOSE settings and configuration fields; visual mappings are additive and take precedence for matching source items.
- Resolve mappings once per render, with the first valid mapping for a source item taking precedence and no chained or recursive substitutions.

## 1.1.2

- Keep vanilla display names and original slot order for the nine contents of a vanilla dropper.
- Continue applying the configured local name pattern to eligible player inventory and hotbar slots.
- No item stacks, server inventory data, packets, or slot positions are modified.

## 1.1.0

- Imported source project and existing local visual customization features.
