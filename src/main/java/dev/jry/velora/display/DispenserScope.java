package dev.jry.velora.display;

import dev.jry.velora.config.DisplayPattern;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.TranslatableTextContent;

import java.util.OptionalInt;

/**
 * Covers the nine slots of a dispenser.
 *
 * <p>Vanilla uses the same 3x3 screen handler for droppers, so a screen titled with the
 * vanilla dropper name is excluded. Servers often rename dispensers, so the title is not
 * used to require "Dispenser".
 */
public final class DispenserScope implements DisplayScope {
	@Override
	public OptionalInt positionOf(HandledScreen<?> screen, Slot slot) {
		if (!(screen.getScreenHandler() instanceof Generic3x3ContainerScreenHandler)) {
			return OptionalInt.empty();
		}
		if (screen.getTitle().getContent() instanceof TranslatableTextContent title
				&& "container.dropper".equals(title.getKey())) {
			return OptionalInt.empty();
		}
		// In this handler the container's nine slots come first (ids 0-8); the
		// player's inventory follows and is never touched.
		if (slot.id >= 0 && slot.id < DisplayPattern.SLOT_COUNT) {
			return OptionalInt.of(slot.id);
		}
		return OptionalInt.empty();
	}
}
