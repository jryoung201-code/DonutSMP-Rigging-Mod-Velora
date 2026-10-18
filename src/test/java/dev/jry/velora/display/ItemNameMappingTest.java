package dev.jry.velora.display;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ItemNameMappingTest {
	private static final int[] NAME_MAPPINGS = {5, 6, 7, 8, 5, 6, 7, 8, 9};

	@Test
	public void mapsNameTwoToSixWithoutAnItemType() {
		assertEquals(6, ItemNameMapping.mappedNumber("2", NAME_MAPPINGS).orElseThrow());
	}

	@Test
	public void mapsEachNumericCustomNameByItsConfiguredValue() {
		for (int name = 1; name <= 9; name++) {
			assertEquals(NAME_MAPPINGS[name - 1],
					ItemNameMapping.mappedNumber(Integer.toString(name), NAME_MAPPINGS).orElseThrow());
		}
	}

	@Test
	public void mapsNameToItsConfiguredDisplaySlot() {
		assertEquals(5, ItemNameMapping.destination("2", 0, NAME_MAPPINGS));
	}

	@Test
	public void leavesUnrecognizedNamesUnmapped() {
		assertFalse(ItemNameMapping.mappedNumber(null, NAME_MAPPINGS).isPresent());
		assertFalse(ItemNameMapping.mappedNumber("0", NAME_MAPPINGS).isPresent());
		assertFalse(ItemNameMapping.mappedNumber("two", NAME_MAPPINGS).isPresent());
		assertFalse(ItemNameMapping.mappedNumber("2", new int[]{1, 2}).isPresent());
	}
}
