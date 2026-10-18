package dev.jry.velora.config;

/**
 * The local slot -> displayed-number mappings.
 *
 * <p>Index = the real numeric item name minus one, value = the number Velora shows.
 * This is display data only; it never changes the actual item name or stack.
 *
 * <pre>
 *   Normal    : 1 2 3 4 5 6 7 8 9
 *   Pattern A lower : 1 2 3 4 1 2 3 4 5
 *   Pattern A higher: 5 6 7 8 5 6 7 8 9
 * </pre>
 */
public enum DisplayPattern {
	NORMAL("Normal",
			new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9},
			new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}),
	PATTERN_A("Pattern A",
			new int[]{1, 2, 3, 4, 1, 2, 3, 4, 5},
			new int[]{5, 6, 7, 8, 5, 6, 7, 8, 9});

	public static final int SLOT_COUNT = 9;

	private final String label;
	private final int[] lowerNumbers;
	private final int[] higherNumbers;

	DisplayPattern(String label, int[] lowerNumbers, int[] higherNumbers) {
		this.label = label;
		this.lowerNumbers = lowerNumbers;
		this.higherNumbers = higherNumbers;
	}

	public String label() {
		return label;
	}

	/** The mapped number for a real item name from 1 through 9. */
	public int number(int itemName) {
		return lowerNumber(itemName);
	}

	public int lowerNumber(int slot) {
		return lowerNumbers[slot];
	}

	public int higherNumber(int slot) {
		return higherNumbers[slot];
	}

	public DisplayPattern next() {
		return this == NORMAL ? PATTERN_A : NORMAL;
	}
}
