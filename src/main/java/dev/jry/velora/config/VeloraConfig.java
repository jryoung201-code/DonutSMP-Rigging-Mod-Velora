package dev.jry.velora.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persistent Velora settings, stored in {@code config/velora.json}.
 *
 * <p>Players never need to edit the file: everything is exposed in the config screen.
 */
public final class VeloraConfig {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("velora.json");

	private static final String DEFAULT_ITEM_1 = "minecraft:paper";
	private static final String DEFAULT_ITEM_2 = "minecraft:book";
	private static final String DEFAULT_WIN_ITEM = "minecraft:paper";
	private static final String DEFAULT_LOSE_ITEM = "minecraft:diamond";

	private static VeloraConfig instance = new VeloraConfig();

	public DisplayMode mode = DisplayMode.ITEM_NAMES;
	/** Item shown with the "lower" numbers (registry id). */
	public String item1 = DEFAULT_ITEM_1;
	/** Item shown with the "higher" numbers (registry id). Always different from item1. */
	public String item2 = DEFAULT_ITEM_2;
	public DisplayPattern pattern = DisplayPattern.NORMAL;
	public boolean map9LocalOverride = false;
	public String winItem = DEFAULT_WIN_ITEM;
	public String loseItem = DEFAULT_LOSE_ITEM;
	public boolean winLoseEnabled = false;

	public static VeloraConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				VeloraConfig loaded = GSON.fromJson(reader, VeloraConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (IOException | JsonParseException e) {
				LOGGER.warn("[Velora] Could not read {}, using defaults", PATH, e);
			}
		}
		instance.sanitize();
		save();
	}

	public static void save() {
		try (Writer writer = Files.newBufferedWriter(PATH)) {
			GSON.toJson(instance, writer);
		} catch (IOException e) {
			LOGGER.warn("[Velora] Could not save {}", PATH, e);
		}
	}

	public Identifier item1Id() {
		return Identifier.tryParse(item1);
	}

	public Identifier item2Id() {
		return Identifier.tryParse(item2);
	}

	public Identifier winItemId() {
		return Identifier.tryParse(winItem);
	}

	public Identifier loseItemId() {
		return Identifier.tryParse(loseItem);
	}

	/** @return false (and changes nothing) if the item is already used as Item 2. */
	public boolean setItem1(Identifier id) {
		if (id.toString().equals(item2)) {
			return false;
		}
		item1 = id.toString();
		return true;
	}

	/** @return false (and changes nothing) if the item is already used as Item 1. */
	public boolean setItem2(Identifier id) {
		if (id.toString().equals(item1)) {
			return false;
		}
		item2 = id.toString();
		return true;
	}

	public boolean setWinItem(Identifier id) {
		if (id.toString().equals(loseItem)) {
			return false;
		}
		winItem = id.toString();
		return true;
	}

	public boolean setLoseItem(Identifier id) {
		if (id.toString().equals(winItem)) {
			return false;
		}
		loseItem = id.toString();
		return true;
	}

	/** Repairs missing, unknown or duplicate values (e.g. a hand-edited or old file). */
	private void sanitize() {
		if (mode == null) {
			mode = DisplayMode.ITEM_NAMES;
		}
		if (pattern == null) {
			pattern = DisplayPattern.NORMAL;
		}
		if (!isValidItem(item1)) {
			item1 = DEFAULT_ITEM_1;
		}
		if (!isValidItem(item2)) {
			item2 = DEFAULT_ITEM_2;
		}
		if (item1.equals(item2)) {
			item2 = item1.equals(DEFAULT_ITEM_2) ? DEFAULT_ITEM_1 : DEFAULT_ITEM_2;
		}
		if (!isValidItem(winItem)) {
			winItem = DEFAULT_WIN_ITEM;
		}
		if (!isValidItem(loseItem)) {
			loseItem = DEFAULT_LOSE_ITEM;
		}
		if (winItem.equals(loseItem)) {
			loseItem = winItem.equals(DEFAULT_LOSE_ITEM) ? DEFAULT_WIN_ITEM : DEFAULT_LOSE_ITEM;
		}
	}

	private static boolean isValidItem(String id) {
		Identifier parsed = id == null ? null : Identifier.tryParse(id);
		return parsed != null && Registries.ITEM.containsId(parsed) && Registries.ITEM.get(parsed) != Items.AIR;
	}
}
