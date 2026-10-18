package dev.jry.velora.config;

/** Which kind of local visual replacement Velora applies. */
public enum DisplayMode {
	ITEM_NAMES("Item Names"),
	MAPS("Maps");

	private final String label;

	DisplayMode(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public DisplayMode next() {
		DisplayMode[] all = values();
		return all[(ordinal() + 1) % all.length];
	}
}
