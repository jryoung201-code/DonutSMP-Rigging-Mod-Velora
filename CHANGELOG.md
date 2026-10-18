# Changelog

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
