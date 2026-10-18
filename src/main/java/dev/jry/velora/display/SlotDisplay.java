package dev.jry.velora.display;

import net.minecraft.util.Identifier;

/**
 * DISPLAY DATA for one slot: what Velora wants to draw, nothing else.
 *
 * <p>Created fresh every time a slot is rendered and never stored, so it cannot go
 * stale when the server updates the inventory. It holds no ItemStack, so it cannot
 * modify one.
 *
 * @param label  text shown as the local name ("1", "Map 5", ...)
 * @param number the visual number (1-9)
 * @param art    local map artwork texture, or null in Item Names mode
 */
public record SlotDisplay(String label, int number, Identifier art) {
}
