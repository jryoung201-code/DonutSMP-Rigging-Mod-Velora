package dev.jry.velora;

import dev.jry.velora.config.VeloraConfig;
import dev.jry.velora.config.DisplayPattern;
import dev.jry.velora.gui.VeloraConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Velora entrypoint (client only).
 *
 * <p>Velora is a purely visual mod. Nothing in here talks to the server, and nothing
	 * touches inventories, item stacks or packets. It only loads the local config and
	 * registers Velora's client-side keybindings.
 */
public class VeloraClient implements ClientModInitializer {
	public static final String MOD_ID = "velora";
	private static final KeyBinding.Category KEY_CATEGORY =
			KeyBinding.Category.create(Identifier.of("velora", "key.categories.velora"));

	private static KeyBinding openSettingsKey;
	private static KeyBinding togglePatternKey;

	@Override
	public void onInitializeClient() {
		VeloraConfig.load();

		// Default: V. Vanilla Minecraft does not bind it. It can be changed in
		// Options -> Controls under the "Velora" category.
		openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.velora.open_settings",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				KEY_CATEGORY
		));
		togglePatternKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.velora.toggle_pattern",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_P,
				KEY_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openSettingsKey.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new VeloraConfigScreen(null));
				}
			}
			while (togglePatternKey.wasPressed()) {
				if (client.currentScreen == null) {
					VeloraConfig config = VeloraConfig.get();
					config.pattern = config.pattern == DisplayPattern.PATTERN_A
							? DisplayPattern.NORMAL
							: DisplayPattern.PATTERN_A;
					config.winLoseEnabled = !config.winLoseEnabled;
					VeloraConfig.save();
				}
			}
		});
	}
}
