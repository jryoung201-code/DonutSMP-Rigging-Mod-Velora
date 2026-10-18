package dev.jry.velora.gui;

import dev.jry.velora.config.VeloraConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class VisualItemSwapScreen extends Screen {
	private static final int ROW_HEIGHT = 44;
	private final Screen parent;
	private int page;
	private int pageSize = 1;

	public VisualItemSwapScreen(Screen parent) {
		this(parent, 0);
	}

	private VisualItemSwapScreen(Screen parent, int page) {
		super(Text.literal("Visual Item Swap"));
		this.parent = parent;
		this.page = page;
	}

	@Override
	protected void init() {
		VeloraConfig config = VeloraConfig.get();
		int buttonWidth = Math.min(220, this.width - 24);
		int x = (this.width - buttonWidth) / 2;
		int top = 42;
		int controlsY = this.height - 48;
		pageSize = Math.max(1, (controlsY - (top + 52)) / ROW_HEIGHT);
		page = Math.max(0, Math.min(page, pageCount(config.visualItemMappings.size()) - 1));

		addDrawableChild(ButtonWidget.builder(Text.literal("Visual Item Swap: " + (config.visualItemSwapEnabled ? "ON" : "OFF")), button -> {
			config.visualItemSwapEnabled = !config.visualItemSwapEnabled;
			VeloraConfig.save();
			clearAndInit();
		}).dimensions(x, top, buttonWidth, 20).build());

		int first = page * pageSize;
		int last = Math.min(config.visualItemMappings.size(), first + pageSize);
		for (int i = first; i < last; i++) {
			addMappingRow(config, config.visualItemMappings.get(i), top + 34 + (i - first) * ROW_HEIGHT);
		}

		int gap = 4;
		int smallWidth = Math.max(48, (this.width - 24 - gap * 3) / 4);
		int controlsWidth = smallWidth * 4 + gap * 3;
		int controlsX = (this.width - controlsWidth) / 2;
		addDrawableChild(ButtonWidget.builder(Text.literal("Add"), button -> addMapping())
				.dimensions(controlsX, controlsY, smallWidth, 20).build());
		ButtonWidget previous = addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> {
			page--;
			clearAndInit();
		}).dimensions(controlsX + smallWidth + gap, controlsY, smallWidth, 20).build());
		ButtonWidget next = addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> {
			page++;
			clearAndInit();
		}).dimensions(controlsX + (smallWidth + gap) * 2, controlsY, smallWidth, 20).build());
		ButtonWidget clear = addDrawableChild(ButtonWidget.builder(Text.literal("Clear All"), button -> {
			config.visualItemMappings.clear();
			page = 0;
			VeloraConfig.save();
			clearAndInit();
		}).dimensions(controlsX + (smallWidth + gap) * 3, controlsY, smallWidth, 20).build());
		previous.active = page > 0;
		next.active = page < pageCount(config.visualItemMappings.size()) - 1;
		clear.active = !config.visualItemMappings.isEmpty();

		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close())
				.dimensions(x, this.height - 24, buttonWidth, 20).build());
	}

	private void addMappingRow(VeloraConfig config, VeloraConfig.VisualItemMapping mapping, int y) {
		int itemWidth = Math.max(48, (this.width - 20 - 56 - 24 - 12) / 2);
		int rowWidth = itemWidth * 2 + 56 + 24 + 12;
		int left = (this.width - rowWidth) / 2;
		int sourceX = left;
		int displayX = sourceX + itemWidth + 4;
		int enabledX = displayX + itemWidth + 4;
		int removeX = enabledX + 60;
		Identifier sourceId = parse(mapping.sourceItem);
		Identifier displayId = parse(mapping.displayItem);

		addDrawableChild(ButtonWidget.builder(Text.literal(itemLabel("Source", sourceId)), button ->
				this.client.setScreen(new ItemPickerScreen(this, Text.literal("Select Source Item"), sourceId,
					displayId, id -> {
						config.setVisualSource(mapping, id);
						VeloraConfig.save();
						clearAndInit();
					})))
				.dimensions(sourceX, y, itemWidth, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal(itemLabel("Display", displayId)), button ->
				this.client.setScreen(new ItemPickerScreen(this, Text.literal("Select Display Item"), displayId,
					sourceId, id -> {
						config.setVisualDisplay(mapping, id);
						VeloraConfig.save();
						clearAndInit();
					})))
				.dimensions(displayX, y, itemWidth, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal(mapping.enabled ? "On" : "Off"), button -> {
			mapping.enabled = !mapping.enabled;
			VeloraConfig.save();
			clearAndInit();
		}).dimensions(enabledX, y, 56, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> {
			config.visualItemMappings.remove(mapping);
			page = Math.min(page, pageCount(config.visualItemMappings.size()) - 1);
			VeloraConfig.save();
			clearAndInit();
		}).dimensions(removeX, y, 24, 20).build());
	}

	private void addMapping() {
		VeloraConfig config = VeloraConfig.get();
		List<Identifier> items = Registries.ITEM.stream()
				.filter(item -> !item.equals(Items.AIR))
				.map(Registries.ITEM::getId)
				.toList();
		for (Identifier source : items) {
			for (Identifier display : items) {
				if (config.addVisualItemMapping(source, display)) {
					page = pageCount(config.visualItemMappings.size()) - 1;
					VeloraConfig.save();
					clearAndInit();
					return;
				}
			}
		}
	}

	private int pageCount(int mappings) {
		return Math.max(1, (mappings + pageSize - 1) / pageSize);
	}

	private static Identifier parse(String id) {
		return id == null ? null : Identifier.tryParse(id);
	}

	private static String itemLabel(String role, Identifier id) {
		return role + ": " + (id == null || !Registries.ITEM.containsId(id)
				? "?" : Registries.ITEM.get(id).getName().getString());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Feature: " + (VeloraConfig.get().visualItemSwapEnabled ? "ON" : "OFF")),
				this.width / 2, 30, 0xFFAAAAAA);
		int count = VeloraConfig.get().visualItemMappings.size();
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Mappings " + count + "  |  Page " + (page + 1) + "/" + pageCount(count)),
				this.width / 2, this.height - 58, 0xFFAAAAAA);
		for (int i = 0; i < pageSize; i++) {
			int index = page * pageSize + i;
			if (index >= count) {
				break;
			}
			int y = 76 + i * ROW_HEIGHT;
			context.drawTextWithShadow(this.textRenderer, Text.literal("Source item"), 12, y - 10, 0xFFFFFF55);
			context.drawTextWithShadow(this.textRenderer, Text.literal("Display item"), this.width / 2 - 52, y - 10, 0xFF55FFFF);
		}
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}
}