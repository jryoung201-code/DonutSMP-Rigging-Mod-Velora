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

	/** Resolves a numbered item's destination, keeping unconfigured items in their real position. */
	public static int destination(String itemId, String customName, int sourcePosition, String item1Id,
			String item2Id, DisplayPattern pattern) {
		OptionalInt mappedNumber = mappedNumber(itemId, customName, item1Id, item2Id, pattern);
		return mappedNumber.isPresent() ? mappedNumber.getAsInt() - 1 : sourcePosition;
	}

	/**
	 * Returns the source-number slot for an item currently at its configured displayed-number slot.
	 * For server number n mapped to displayed number d, the container move is d-1 -> n-1.
	 */
	public static OptionalInt dropperDestination(String itemId, String customName, int displayedSlot,
			String item1Id, String item2Id, DisplayPattern pattern) {
		OptionalInt serverNumber = serverNumber(customName);
		OptionalInt displayedNumber = mappedNumber(itemId, customName, item1Id, item2Id, pattern);
		if (serverNumber.isEmpty() || displayedNumber.isEmpty() || displayedNumber.getAsInt() - 1 != displayedSlot) {
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