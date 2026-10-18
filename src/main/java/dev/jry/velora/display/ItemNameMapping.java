package dev.jry.velora.display;

import dev.jry.velora.config.DisplayPattern;

import java.util.OptionalInt;

/** Resolves numeric custom names independently of Minecraft item identity and inventory slot. */
public final class ItemNameMapping {
	private ItemNameMapping() {
	}

	/**
	 * Maps an item's numeric custom name (1-9) to its configured display number.
	 * There is intentionally no item-id argument: any item with the same custom name
	 * receives the same mapping.
	 */
	public static OptionalInt mappedNumber(String customName, int[] nameMappings) {
		if (customName == null || nameMappings == null || nameMappings.length < DisplayPattern.SLOT_COUNT) {
			return OptionalInt.empty();
		}
		String name = customName.trim();
		if (!name.matches("[1-9]")) {
			return OptionalInt.empty();
		}
		int displayNumber = nameMappings[Integer.parseInt(name) - 1];
		if (displayNumber < 1 || displayNumber > DisplayPattern.SLOT_COUNT) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(displayNumber);
	}

	/** Returns the target display slot for a numeric name, otherwise its original source slot. */
	public static int destination(String customName, int sourcePosition, int[] nameMappings) {
		OptionalInt mapped = mappedNumber(customName, nameMappings);
		return mapped.isPresent() ? mapped.getAsInt() - 1 : sourcePosition;
	}
}
