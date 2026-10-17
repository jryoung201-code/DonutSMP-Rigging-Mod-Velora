package dev.jry.velora;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VeloraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("velora.json");

    public String pattern = "Normal";
    public boolean inventoryNamePatternEnabled = false;
    public boolean localPatternPreviewEnabled = true;

    private static VeloraConfig INSTANCE = new VeloraConfig();

    private VeloraConfig() {}

    public static VeloraConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(FILE)) {
            try {
                VeloraConfig loaded = GSON.fromJson(Files.readString(FILE), VeloraConfig.class);
                if (loaded != null) INSTANCE = loaded;
            } catch (Exception ignored) {
                INSTANCE = new VeloraConfig();
            }
        }
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            System.err.println("[Velora] Could not save config: " + e.getMessage());
        }
    }

    public void cyclePattern() {
        pattern = "Normal".equals(pattern) ? "A" : "Normal";
        save();
    }
}
