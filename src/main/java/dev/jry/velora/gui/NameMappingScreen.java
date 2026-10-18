package dev.jry.velora.gui;

import dev.jry.velora.config.DisplayPattern;
import dev.jry.velora.config.VeloraConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/** Edits numeric custom-name mappings independently of Minecraft item type. */
public class NameMappingScreen extends Screen {
	private static final int BUTTON_WIDTH = 220;
	private static final int ROW_HEIGHT = 22;

	private final Screen parent;
	private final ButtonWidget[] mappingButtons = new ButtonWidget[DisplayPattern.SLOT_COUNT];

	public NameMappingScreen(Screen parent) {
		super(Text.literal("Item Name Mappings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ensureMappings();
		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int firstY = 47;
		for (int i = 0; i < mappingButtons.length; i++) {
			final int mappingIndex = i;
			mappingButtons[i] = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
				VeloraConfig config = VeloraConfig.get();
				int current = config.itemNameMappings[mappingIndex];
				config.itemNameMappings[mappingIndex] = current >= DisplayPattern.SLOT_COUNT ? 1 : current + 1;
				VeloraConfig.save();
				refreshLabels();
			}).dimensions(x, firstY + i * ROW_HEIGHT, BUTTON_WIDTH, 20).build());
		}

		addDrawableChild(ButtonWidget.builder(Text.literal("Reset to Normal"), button -> {
			VeloraConfig config = VeloraConfig.get();
			for (int i = 0; i < config.itemNameMappings.length; i++) {
				config.itemNameMappings[i] = i + 1;
			}
			VeloraConfig.save();
			refreshLabels();
		}).dimensions(this.width / 2 - 102, this.height - 28, 100, 20).build());

		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close())
				.dimensions(this.width / 2 + 2, this.height - 28, 100, 20).build());

		refreshLabels();
	}

	private void ensureMappings() {
		VeloraConfig config = VeloraConfig.get();
		if (config.itemNameMappings == null || config.itemNameMappings.length != DisplayPattern.SLOT_COUNT) {
			config.itemNameMappings = new int[DisplayPattern.SLOT_COUNT];
			for (int i = 0; i < config.itemNameMappings.length; i++) {
				config.itemNameMappings[i] = config.pattern.higherNumber(i);
			}
			VeloraConfig.save();
		}
	}

	private void refreshLabels() {
		ensureMappings();
		int[] mappings = VeloraConfig.get().itemNameMappings;
		for (int i = 0; i < mappingButtons.length; i++) {
			mappingButtons[i].setMessage(Text.literal("Item name " + (i + 1) + "  →  display as " + mappings[i]));
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 13, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Click a row to cycle its display number (1–9)."),
				this.width / 2, 30, 0xFFAAAAAA);
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}
}
