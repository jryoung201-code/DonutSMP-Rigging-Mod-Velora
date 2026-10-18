package dev.jry.velora.gui;

import dev.jry.velora.config.DisplayMode;
import dev.jry.velora.config.DisplayPattern;
import dev.jry.velora.config.VeloraConfig;
import dev.jry.velora.display.MapArt;
import dev.jry.velora.display.VeloraRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Velora's settings screen. Every change is saved immediately, so there is no JSON editing
 * and nothing is lost if the game closes.
 */
public class VeloraConfigScreen extends Screen {
	private static final int BUTTON_WIDTH = 220;
	private static final int BUTTON_HEIGHT = 20;
	private static final int ROW_STEP = 24;
	private static final int FIRST_ROW_Y = 34;
	private static final int CELL = 14;

	private final Screen parent;

	private ButtonWidget modeButton;
	private ButtonWidget nameMappingsButton;
	private ButtonWidget patternButton;
	private ButtonWidget overrideButton;
	private ButtonWidget winItemButton;
	private ButtonWidget loseItemButton;
	private ButtonWidget visualItemSwapButton;

	public VeloraConfigScreen(Screen parent) {
		super(Text.literal("Velora Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		VeloraConfig config = VeloraConfig.get();
		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int y = FIRST_ROW_Y;

		modeButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
			config.mode = config.mode.next();
			VeloraConfig.save();
			refresh();
		}).dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
		y += ROW_STEP;

		nameMappingsButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button ->
				this.client.setScreen(new NameMappingScreen(this)))
				.dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
		y += ROW_STEP;

		patternButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
			config.pattern = config.pattern.next();
			VeloraConfig.save();
			refresh();
		}).dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
		y += ROW_STEP;

		overrideButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
			config.map9LocalOverride = !config.map9LocalOverride;
			VeloraConfig.save();
			refresh();
		}).dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
		y += ROW_STEP;
		y += 12;

		winItemButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button ->
				this.client.setScreen(new ItemPickerScreen(
						this,
						Text.literal("Select WIN Item"),
						config.winItemId(),
						config.loseItemId(),
						config::setWinItem)))
				.dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
		y += ROW_STEP;

		loseItemButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button ->
				this.client.setScreen(new ItemPickerScreen(
						this,
						Text.literal("Select LOSE Item"),
						config.loseItemId(),
						config.winItemId(),
						config::setLoseItem)))
				.dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
		y += ROW_STEP;

		visualItemSwapButton = addDrawableChild(ButtonWidget.builder(Text.literal("Visual Item Swap..."), button ->
				this.client.setScreen(new VisualItemSwapScreen(this)))
				.dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close())
				.dimensions(x, this.height - 28, BUTTON_WIDTH, BUTTON_HEIGHT).build());

		refresh();
	}

	/** Updates button labels and which buttons apply to the current mode. */
	private void refresh() {
		VeloraConfig config = VeloraConfig.get();
		boolean itemMode = config.mode == DisplayMode.ITEM_NAMES;

		modeButton.setMessage(Text.literal("Display Mode: " + config.mode.label()));
		nameMappingsButton.setMessage(Text.literal("Item Name Mappings..."));
		patternButton.setMessage(Text.literal("Display Pattern: " + config.pattern.label()));
		overrideButton.setMessage(Text.literal("Map 9 Local Override: " + (config.map9LocalOverride ? "ON" : "OFF")));
		winItemButton.setMessage(Text.literal("WIN Item: " + itemName(config.winItemId())));
		loseItemButton.setMessage(Text.literal("LOSE Item: " + itemName(config.loseItemId())));
		visualItemSwapButton.setMessage(Text.literal("Visual Item Swap..."));

		// Item pickers only matter in Item Names mode, the override only in Maps mode.
		nameMappingsButton.active = itemMode;
		patternButton.active = !itemMode;
		overrideButton.active = !itemMode;
	}

	private static String itemName(Identifier id) {
		return id == null ? "?" : Registries.ITEM.get(id).getName().getString();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("WIN / LOSE ITEMS (P toggle: " + (VeloraConfig.get().winLoseEnabled ? "ON" : "OFF") + ")"),
				this.width / 2, FIRST_ROW_Y + 4 * ROW_STEP + 2, 0xFFFFFF);
		if (this.height >= 360) {
			drawActiveMapping(context);
		}
	}

	/** Shows what the dispenser's nine slots will look like with the current settings. */
	private void drawActiveMapping(DrawContext context) {
		VeloraConfig config = VeloraConfig.get();
		int centerX = this.width / 2;
		int y = FIRST_ROW_Y + 7 * ROW_STEP + 4;
		int left = centerX - BUTTON_WIDTH / 2;
		int cellsStart = left + 70;

		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Active mapping (real slot → shown as)"), centerX, y, 0xFFAAAAAA);
		y += 14;

		drawRow(context, "Real slot", left, cellsStart, y, 0xFF888888, DisplayPattern.NORMAL::number);
		y += 12;

		if (config.mode == DisplayMode.ITEM_NAMES) {
			context.drawTextWithShadow(this.textRenderer,
					Text.literal("Lower rules: " + config.lowerNameMappings.size()
							+ "   Higher rules: " + config.higherNameMappings.size()),
					left, y, 0xFFFFFF55);
		} else {
			drawRow(context, "Map art", left, cellsStart, y, 0xFFFFFF55, config.pattern::number);
			y += 13;
			for (int i = 0; i < DisplayPattern.SLOT_COUNT; i++) {
				Identifier art = MapArt.texture(config.pattern.number(i), config.map9LocalOverride);
				VeloraRenderer.drawArt(context, art, cellsStart + i * CELL + 1, y, 12);
			}
		}
	}

	private void drawRow(DrawContext context, String name, int left, int cellsStart, int y, int color,
						 java.util.function.IntUnaryOperator numberForSlot) {
		context.drawTextWithShadow(this.textRenderer, Text.literal(name), left, y, color);
		for (int i = 0; i < DisplayPattern.SLOT_COUNT; i++) {
			context.drawCenteredTextWithShadow(this.textRenderer,
					Text.literal(String.valueOf(numberForSlot.applyAsInt(i))),
					cellsStart + i * CELL + CELL / 2, y, color);
		}
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}
}
