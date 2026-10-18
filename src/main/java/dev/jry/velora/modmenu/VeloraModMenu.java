package dev.jry.velora.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.jry.velora.gui.VeloraConfigScreen;

/** Makes the "Configure" button in Mod Menu open Velora's settings screen. */
public class VeloraModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return VeloraConfigScreen::new;
	}
}
