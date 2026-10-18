package dev.jry.velora.display;

import dev.jry.velora.config.DisplayPattern;

import java.util.Arrays;
import java.util.Optional;

/** Builds a one-to-one visual slot permutation without changing real slot contents. */
public final class VisualSlotMapping {
	private VisualSlotMapping() {
	}

	/**
	 * @param occupied whether each real slot contains an item
	 * @param requestedDestinations zero-based destination for each mapped item, or -1 when unmapped
	 * @return source-to-destination positions, or empty when configured and fixed slots conflict
	 */
	public static Optional<int[]> destinations(boolean[] occupied, int[] requestedDestinations) {
		int count = DisplayPattern.SLOT_COUNT;
		if (occupied == null || requestedDestinations == null || occupied.length != count
				|| requestedDestinations.length != count) {
			return Optional.empty();
		}

		boolean[] destinationsUsed = new boolean[count];
		boolean[] mappedSources = new boolean[count];
		int[] destinations = new int[count];
		Arrays.fill(destinations, -1);

		for (int source = 0; source < count; source++) {
			if (!occupied[source] || requestedDestinations[source] == -1) {
				continue;
			}
			int destination = requestedDestinations[source];
			if (destination < 0 || destination >= count || destinationsUsed[destination]) {
				return Optional.empty();
			}
			destinations[source] = destination;
			mappedSources[source] = true;
			destinationsUsed[destination] = true;
		}

		for (int source = 0; source < count; source++) {
			if (occupied[source] && !mappedSources[source]) {
				if (destinationsUsed[source]) {
					return Optional.empty();
				}
				destinations[source] = source;
				destinationsUsed[source] = true;
			}
		}

		for (int source = 0; source < count; source++) {
			if (occupied[source]) {
				continue;
			}
			int vacatedSource = findVacatedMappedSource(destinations, mappedSources, destinationsUsed);
			destinations[source] = vacatedSource >= 0
					? vacatedSource
					: firstAvailableDestination(destinationsUsed, source);
			destinationsUsed[destinations[source]] = true;
		}

		return Optional.of(destinations);
	}

	private static int findVacatedMappedSource(int[] destinations, boolean[] mappedSources,
			boolean[] destinationsUsed) {
		for (int source = 0; source < mappedSources.length; source++) {
			if (mappedSources[source] && destinations[source] != source && !destinationsUsed[source]) {
				return source;
			}
		}
		return -1;
	}

	private static int firstAvailableDestination(boolean[] destinationsUsed, int preferred) {
		if (!destinationsUsed[preferred]) {
			return preferred;
		}
		for (int destination = 0; destination < destinationsUsed.length; destination++) {
			if (!destinationsUsed[destination]) {
				return destination;
			}
		}
		throw new IllegalStateException("No visual slot remains for an empty source slot");
	}
}