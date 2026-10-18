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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
	public boolean visualItemSwapEnabled = false;
	public List<VisualItemMapping> visualItemMappings = new ArrayList<>();

	public static final class VisualItemMapping {
		public String sourceItem;
		public String displayItem;
		public boolean enabled = true;

		public VisualItemMapping() {
		}

		public VisualItemMapping(String sourceItem, String displayItem) {
			this.sourceItem = sourceItem;
			this.displayItem = displayItem;
		}
	}

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

	public Optional<Identifier> visualDisplayItemId(String sourceItem) {
		if (!visualItemSwapEnabled || sourceItem == null || visualItemMappings == null) {
			return Optional.empty();
		}
		for (VisualItemMapping mapping : visualItemMappings) {
			if (mapping != null && mapping.enabled && sourceItem.equals(mapping.sourceItem)) {
				Identifier displayId = mapping.displayItem == null ? null : Identifier.tryParse(mapping.displayItem);
				if (displayId != null && isValidItem(displayId.toString())) {
					return Optional.of(displayId);
				}
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	public boolean addVisualItemMapping(Identifier sourceId, Identifier displayId) {
		if (!isValidItem(sourceId.toString()) || !isValidItem(displayId.toString())
				|| sourceId.equals(displayId) || hasVisualSource(sourceId.toString())) {
			return false;
		}
		visualItemMappings.add(new VisualItemMapping(sourceId.toString(), displayId.toString()));
		return true;
	}

	public boolean setVisualSource(VisualItemMapping mapping, Identifier sourceId) {
		if (!visualItemMappings.contains(mapping) || !isValidItem(sourceId.toString())
				|| sourceId.toString().equals(mapping.displayItem)
				|| visualItemMappings.stream().anyMatch(other -> other != mapping
					&& other.sourceItem.equals(sourceId.toString()))) {
			return false;
		}
		mapping.sourceItem = sourceId.toString();
		return true;
	}

	public boolean setVisualDisplay(VisualItemMapping mapping, Identifier displayId) {
		if (!visualItemMappings.contains(mapping) || !isValidItem(displayId.toString())
				|| displayId.toString().equals(mapping.sourceItem)) {
			return false;
		}
		mapping.displayItem = displayId.toString();
		return true;
	}

	private boolean hasVisualSource(String sourceId) {
		return visualItemMappings.stream().anyMatch(mapping -> mapping != null && sourceId.equals(mapping.sourceItem));
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
		if (visualItemMappings == null) {
			visualItemMappings = new ArrayList<>();
		}
		List<VisualItemMapping> validMappings = new ArrayList<>();
		Set<String> sources = new HashSet<>();
		for (VisualItemMapping mapping : visualItemMappings) {
			if (mapping == null || !isValidItem(mapping.sourceItem) || !isValidItem(mapping.displayItem)
					|| mapping.sourceItem.equals(mapping.displayItem) || !sources.add(mapping.sourceItem)) {
				continue;
			}
			validMappings.add(mapping);
		}
		visualItemMappings = validMappings;
	}

	private static boolean isValidItem(String id) {
		Identifier parsed = id == null ? null : Identifier.tryParse(id);
		return parsed != null && Registries.ITEM.containsId(parsed) && Registries.ITEM.get(parsed) != Items.AIR;
	}
}
