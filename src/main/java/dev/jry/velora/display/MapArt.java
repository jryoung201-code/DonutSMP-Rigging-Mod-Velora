package dev.jry.velora.display;

import net.minecraft.util.Identifier;

/**
 * Locates the local map artwork bundled in the mod jar.
 *
 * <p>Files live at {@code assets/velora/maps/map_N.png}. To replace Map 9's local image
 * edit {@code assets/velora/maps/map_9_override.png} (or ship the same path in a
 * resource pack). No code change is needed.
 */
public final class MapArt {
	private static final String NAMESPACE = "velora";

	private MapArt() {
	}

	/** Texture for map art number 1-9. Map 9 optionally uses the local override image. */
	public static Identifier texture(int number, boolean map9LocalOverride) {
		if (number == 9 && map9LocalOverride) {
			return Identifier.of(NAMESPACE, "maps/map_9_override.png");
		}
		return Identifier.of(NAMESPACE, "maps/map_" + number + ".png");
	}
}
