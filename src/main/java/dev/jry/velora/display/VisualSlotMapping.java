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
	 * @return source-to-destination positions, or empty for invalid/duplicate requested destinations
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

		// First reserve every destination explicitly requested by an identity/name mapping.
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

		// Keep unmapped occupied items in their own slot where possible. If a mapped item
		// needs that destination, move the displaced item into the vacated source position.
		for (int source = 0; source < count; source++) {
			if (!occupied[source] || mappedSources[source]) {
				continue;
			}
			int destination = !destinationsUsed[source]
					? source
					: findVacatedMappedSource(destinations, mappedSources, destinationsUsed);
			if (destination < 0) {
				// A full container has no real vacancy to move into; avoid inventing one.
				if (allOccupied(occupied)) {
					return Optional.empty();
				}
				destination = firstAvailableDestination(destinationsUsed);
			}
			if (destination < 0) {
				return Optional.empty();
			}
			destinations[source] = destination;
			destinationsUsed[destination] = true;
		}

		// Empty source slots fill the remaining destinations. Prefer their own position,
		// then the first free destination, so the mapping stays a complete permutation.
		for (int source = 0; source < count; source++) {
			if (occupied[source]) {
				continue;
			}
			int vacatedMappedSource = findVacatedMappedSource(destinations, mappedSources, destinationsUsed);
			int destination = vacatedMappedSource >= 0 ? vacatedMappedSource
					: (!destinationsUsed[source] ? source : firstAvailableDestination(destinationsUsed));
			if (destination < 0) {
				return Optional.empty();
			}
			destinations[source] = destination;
			destinationsUsed[destination] = true;
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

	private static boolean allOccupied(boolean[] occupied) {
		for (boolean value : occupied) {
			if (!value) {
				return false;
			}
		}
		return true;
	}

	private static int firstAvailableDestination(boolean[] destinationsUsed) {
		for (int destination = 0; destination < destinationsUsed.length; destination++) {
			if (!destinationsUsed[destination]) {
				return destination;
			}
		}
		return -1;
	}
}
