package dev.jry.velora.display;

import dev.jry.velora.config.VeloraConfig;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ItemNameMappingTest {
	@Test
	public void mapsDifferentReceivedNamesToTypedReplacements() {
		List<VeloraConfig.ItemNameRule> lower = List.of(new VeloraConfig.ItemNameRule("lower-in", "lower-out", true));
		List<VeloraConfig.ItemNameRule> higher = List.of(new VeloraConfig.ItemNameRule("2", "6", true));
		assertEquals("lower-out", ItemNameMapping.resolve("lower-in", lower, higher).orElseThrow().displayedName());
		assertEquals("6", ItemNameMapping.resolve("2", lower, higher).orElseThrow().displayedName());
		assertEquals(6, ItemNameMapping.resolve("2", lower, higher).orElseThrow().displayedNumber().orElseThrow());
	}

	@Test
	public void arbitraryReplacementTextHasNoNumericSlotTarget() {
		Optional<ItemNameMapping.MappingResult> result = ItemNameMapping.resolve(
				"server-name", List.of(new VeloraConfig.ItemNameRule("server-name", "local-name", true)), List.of());
		assertEquals("local-name", result.orElseThrow().displayedName());
		assertFalse(result.orElseThrow().displayedNumber().isPresent());
	}

	@Test
	public void disabledRulesAreIgnored() {
		assertFalse(ItemNameMapping.resolve("2",
				List.of(new VeloraConfig.ItemNameRule("2", "6", false)), List.of()).isPresent());
	}

	@Test
	public void lowerRulesTakePriorityForDuplicateReceivedNames() {
		assertEquals("3", ItemNameMapping.resolve("2",
				List.of(new VeloraConfig.ItemNameRule("2", "3", true)),
				List.of(new VeloraConfig.ItemNameRule("2", "6", true))).orElseThrow().displayedName());
	}
}
