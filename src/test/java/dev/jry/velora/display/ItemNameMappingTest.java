package dev.jry.velora.display;

import dev.jry.velora.config.DisplayPattern;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

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
	public void mapsPaperNumbersFiveThroughNineToDisplaySlotsZeroThroughFour() {
		for (int serverNumber = 5; serverNumber <= 9; serverNumber++) {
			int expectedPosition = serverNumber - 5;
			assertEquals(expectedPosition, ItemNameMapping.destination(ITEM_1,
					Integer.toString(serverNumber), 8, ITEM_1, ITEM_2, DisplayPattern.PATTERN_A));
		}
	}


	@Test
	public void reversesDisplayedInventoryNumberToServerNumberByItemIdentity() {
		assertEquals(1, ItemNameMapping.serverNumberForDisplayedName(ITEM_2, "5",
				ITEM_1, ITEM_2, DisplayPattern.PATTERN_A).orElseThrow());
		assertEquals(2, ItemNameMapping.serverNumberForDisplayedName(ITEM_2, "6",
				ITEM_1, ITEM_2, DisplayPattern.PATTERN_A).orElseThrow());
		assertEquals(1, ItemNameMapping.serverNumberForDisplayedName(ITEM_1, "1",
				ITEM_1, ITEM_2, DisplayPattern.PATTERN_A).orElseThrow());
		assertFalse(ItemNameMapping.serverNumberForDisplayedName("minecraft:diamond", "6",
				ITEM_1, ITEM_2, DisplayPattern.PATTERN_A).isPresent());
	}


	@Test
	public void vacancyUsesDisplayedNumberRatherThanServerNumber() {
		// Server number 2 maps to displayed number 6 for Item 2 in Pattern A.
		int serverNumber = 2;
		int displayedNumber = ItemNameMapping.mappedNumber(ITEM_2, Integer.toString(serverNumber),
				ITEM_1, ITEM_2, DisplayPattern.PATTERN_A).orElseThrow();
		assertEquals(6, displayedNumber);
		assertEquals(5, displayedNumber - 1); // sixth visual slot, zero-based
	}

	@Test
	public void reversesHigherPatternForDropperOneToFiveAndTwoToSix() {
		assertEquals(0, ItemNameMapping.dropperDestination(ITEM_2, "1", 4,
				ITEM_1, ITEM_2, DisplayPattern.PATTERN_A).orElseThrow());
		assertEquals(1, ItemNameMapping.dropperDestination(ITEM_2, "2", 5,
				ITEM_1, ITEM_2, DisplayPattern.PATTERN_A).orElseThrow());
		assertFalse(ItemNameMapping.dropperDestination(ITEM_2, "1", 0,
				ITEM_1, ITEM_2, DisplayPattern.PATTERN_A).isPresent());
	}

	@Test
	public void followsTheConfiguredHigherPatternForAllBookNumbers() {
		for (int serverNumber = 1; serverNumber <= 9; serverNumber++) {
			int displayedNumber = DisplayPattern.PATTERN_A.higherNumber(serverNumber - 1);
			int expectedDestination = serverNumber - 1;
			int sourceSlot = displayedNumber - 1;
			assertEquals(expectedDestination, ItemNameMapping.dropperDestination(ITEM_2,
					Integer.toString(serverNumber), sourceSlot, ITEM_1, ITEM_2,
					DisplayPattern.PATTERN_A).orElseThrow());
		}
	}

	@Test
	public void reversesLowerPatternForPaperIdentity() {
		assertEquals(4, ItemNameMapping.dropperDestination(ITEM_1, "5", 0,
				ITEM_1, ITEM_2, DisplayPattern.PATTERN_A).orElseThrow());
	}

	@Test
	public void placesTheVacancyAtTheMappedItemsSourceSlot() {
		boolean[] occupied = new boolean[9];
		occupied[4] = true;
		int[] requestedDestinations = new int[9];
		java.util.Arrays.fill(requestedDestinations, -1);
		requestedDestinations[4] = 0;

		int[] destinations = VisualSlotMapping.destinations(occupied, requestedDestinations).orElseThrow();
		assertEquals(0, destinations[4]);
		assertEquals(4, destinations[0]);
		assertArrayEquals(new int[]{4, 1, 2, 3, 0, 5, 6, 7, 8}, destinations);
	}

	@Test
	public void movesDisplacedFullContainerItemIntoVacatedSourceSlot() {
		boolean[] occupied = new boolean[9];
		java.util.Arrays.fill(occupied, true);
		int[] requestedDestinations = new int[9];
		java.util.Arrays.fill(requestedDestinations, -1);
		requestedDestinations[3] = 0;

		int[] destinations = VisualSlotMapping.destinations(occupied, requestedDestinations).orElseThrow();
		assertArrayEquals(new int[]{3, 1, 2, 0, 4, 5, 6, 7, 8}, destinations);
		assertEquals(9, java.util.Arrays.stream(destinations).distinct().count());
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