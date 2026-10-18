package dev.jry.velora.display;

import dev.jry.velora.config.VeloraConfig;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/** Resolves typed custom-name mappings without depending on the Minecraft item type. */
public final class ItemNameMapping {
	private ItemNameMapping() {
	}

	/** A matched display label, plus a numeric slot target when the replacement is 1-9. */
	public record MappingResult(String displayedName, OptionalInt displayedNumber) {
	}

	/**
	 * Maps a received name to its configured replacement. Lower rules are checked first;
	 * keep received names unique across both groups when different outputs are desired.
	 */
	public static Optional<MappingResult> resolve(String receivedName,
			List<VeloraConfig.ItemNameRule> lowerRules,
			List<VeloraConfig.ItemNameRule> higherRules) {
		if (receivedName == null || receivedName.trim().isEmpty()) {
			return Optional.empty();
		}
		String normalized = receivedName.trim();
		Optional<MappingResult> lower = find(normalized, lowerRules);
		return lower.isPresent() ? lower : find(normalized, higherRules);
	}

	private static Optional<MappingResult> find(String receivedName, List<VeloraConfig.ItemNameRule> rules) {
		if (rules == null) {
			return Optional.empty();
		}
		for (VeloraConfig.ItemNameRule rule : rules) {
			if (rule == null || !rule.enabled || rule.receivedName == null || rule.displayedName == null
					|| rule.receivedName.trim().isEmpty() || rule.displayedName.trim().isEmpty()) {
				continue;
			}
			if (receivedName.equalsIgnoreCase(rule.receivedName.trim())) {
				String replacement = rule.displayedName.trim();
				OptionalInt number = replacement.matches("[1-9]")
						? OptionalInt.of(Integer.parseInt(replacement)) : OptionalInt.empty();
				return Optional.of(new MappingResult(replacement, number));
			}
		}
		return Optional.empty();
	}
}
