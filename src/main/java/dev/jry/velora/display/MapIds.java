package dev.jry.velora.display;

/**
 * Server-provided map IDs that Velora recognizes.
 *
 * <p>These IDs are used ONLY to decide "is this one of the maps I care about?".
 * They are never written back, never sent anywhere and never changed.
 */
public final class MapIds {
	/** Index 0 = map "1", index 8 = map "9". */
	private static final int[] IDS = {
			5821839, // 1
			5825443, // 2
			5828512, // 3
			5837266, // 4
			5840048, // 5
			5843929, // 6
			5847472, // 7
			5853260, // 8
			5857126  // 9
	};

	private MapIds() {
	}

	/** @return which of the nine known maps this ID is (1-9), or -1 if it is not one of them. */
	public static int knownMapNumber(int mapId) {
		for (int i = 0; i < IDS.length; i++) {
			if (IDS[i] == mapId) {
				return i + 1;
			}
		}
		return -1;
	}
}
