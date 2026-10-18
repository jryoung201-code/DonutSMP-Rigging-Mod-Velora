package dev.jry.velora.display;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;

import java.util.OptionalInt;

/**
 * Decides which containers Velora may restyle.
 *
 * <p>Each scope maps a handled-screen slot to one of the repeating display positions.
 * Scopes are registered by {@link DisplayResolver}.
 */
public interface DisplayScope {
	/**
	 * @return the logical position (0-based) of {@code slot} inside the supported
	 *         container, or empty if this scope does not cover the screen or slot.
	 */
	OptionalInt positionOf(HandledScreen<?> screen, Slot slot);
}
