package dev.jry.velora.display;

import dev.jry.velora.config.DisplayPattern;

import java.util.OptionalInt;

/** Resolves configured item-name mappings by item identity, independent of inventory slot. */
public final class ItemNameMapping {
	private ItemNameMapping() {
	}

	public static OptionalInt mappedNumber(String itemId, String customName, String item1Id,
			String item2Id, DisplayPattern pattern) {
		if (itemId == null || customName == null || pattern == null) {
			return OptionalInt.empty();
		}
		String name = customName.trim();
		if (!name.matches("[1-9]")) {
			return OptionalInt.empty();
		}
		int namePosition = Integer.parseInt(name) - 1;
		if (itemId.equals(item1Id)) {
			return OptionalInt.of(pattern.lowerNumber(namePosition));
		}
		if (itemId.equals(item2Id)) {
			return OptionalInt.of(pattern.higherNumber(namePosition));
		}
		return OptionalInt.empty();
	}


	/**
	 * Resolves a displayed inventory number back to its server-side number using the
	 * selected item identity's pattern. When a pattern repeats a displayed number, the
	 * first matching server number is used (e.g. Pattern A's higher-item "6" maps to 2).
	 */
	public static OptionalInt serverNumberForDisplayedName(String itemId, String displayedName,
			String item1Id, String item2Id, DisplayPattern pattern) {
		if (itemId == null || displayedName == null || pattern == null) {
			return OptionalInt.empty();
		}
		String value = displayedName.trim();
		if (!value.matches("[1-9]")) {
			return OptionalInt.empty();
		}
		int displayedNumber = Integer.parseInt(value);
		for (int serverNumber = 1; serverNumber <= DisplayPattern.SLOT_COUNT; serverNumber++) {
			int mapped = itemId.equals(item1Id) ? pattern.lowerNumber(serverNumber - 1)
					: itemId.equals(item2Id) ? pattern.higherNumber(serverNumber - 1) : -1;
			if (mapped == displayedNumber) {
				return OptionalInt.of(serverNumber);
			}
		}
		return OptionalInt.empty();
	}

	/** Resolves a numbered item's destination, keeping unconfigured items in their real position. */
	public static int destination(String itemId, String customName, int sourcePosition, String item1Id,
			String item2Id, DisplayPattern pattern) {
		OptionalInt mappedNumber = mappedNumber(itemId, customName, item1Id, item2Id, pattern);
		return mappedNumber.isPresent() ? mappedNumber.getAsInt() - 1 : sourcePosition;
	}

	/**
	 * Returns the target slot based on the item's identity and server-side number, not its current slot.
	 * For server number n mapped to displayed number d, the visual destination is n-1 wherever
	 * the matching item currently resides in the nine-slot container.
	 */
	public static OptionalInt dropperDestination(String itemId, String customName, int currentSlot,
			String item1Id, String item2Id, DisplayPattern pattern) {
		OptionalInt serverNumber = serverNumber(customName);
		OptionalInt displayedNumber = mappedNumber(itemId, customName, item1Id, item2Id, pattern);
		if (serverNumber.isEmpty() || displayedNumber.isEmpty()
				|| displayedNumber.getAsInt() - 1 != currentSlot) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(serverNumber.getAsInt() - 1);
	}

	private static OptionalInt serverNumber(String customName) {
		if (customName == null) {
			return OptionalInt.empty();
		}
		String name = customName.trim();
		if (!name.matches("[1-9]")) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(Integer.parseInt(name));
	}
}
