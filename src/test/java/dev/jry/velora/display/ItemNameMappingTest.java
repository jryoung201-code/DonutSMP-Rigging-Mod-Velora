package dev.jry.velora.display;

import dev.jry.velora.config.DisplayPattern;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ItemNameMappingTest {
	private static final String ITEM_1 = "minecraft:paper";
	private static final String ITEM_2 = "minecraft:book";

	@Test
	public void mapsConfiguredPaperAndBookIdentities() {
		assertEquals(1, ItemNameMapping.mappedNumber(ITEM_1, "5", ITEM_1, ITEM_2,
				DisplayPattern.PATTERN_A).getAsInt());
		assertEquals(5, ItemNameMapping.mappedNumber(ITEM_2, "5", ITEM_1, ITEM_2,
				DisplayPattern.PATTERN_A).getAsInt());
	}

	@Test
	public void mapsSameItemIdentityAtDifferentSourceSlots() {
		for (int sourceSlot : new int[]{0, 4, 8}) {
			assertEquals(0, ItemNameMapping.destination(ITEM_1, "5", sourceSlot,
					ITEM_1, ITEM_2, DisplayPattern.PATTERN_A));
			assertEquals(4, ItemNameMapping.destination(ITEM_2, "5", sourceSlot,
					ITEM_1, ITEM_2, DisplayPattern.PATTERN_A));
		}
	}

	@Test
	public void preservesUnconfiguredItemsAndMapsAcrossRows() {
		assertEquals(2, ItemNameMapping.destination("minecraft:diamond", "5", 2,
				ITEM_1, ITEM_2, DisplayPattern.PATTERN_A));
		assertEquals(8, ItemNameMapping.destination(ITEM_2, "9", 0,
				ITEM_1, ITEM_2, DisplayPattern.PATTERN_A));
		assertFalse(ItemNameMapping.mappedNumber(ITEM_1, "not a number", ITEM_1, ITEM_2,
				DisplayPattern.PATTERN_A).isPresent());
	}
}