package dev.jry.velora.gui;

import dev.jry.velora.config.VeloraConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Editable Lower/Higher received-name -> displayed-name rules. */
public class NameMappingScreen extends Screen {
	private static final int VISIBLE_ROWS = 5;
	private static final int ROW_HEIGHT = 29;
	private static final int TOP = 78;

	private final Screen parent;
	private final boolean higherGroup;
	private final int page;

	public NameMappingScreen(Screen parent) {
		this(parent, true, 0);
	}

	private NameMappingScreen(Screen parent, boolean higherGroup, int page) {
		super(Text.literal("Item Name Mappings"));
		this.parent = parent;
		this.higherGroup = higherGroup;
		this.page = Math.max(0, page);
	}

	@Override
	protected void init() {
		VeloraConfig config = VeloraConfig.get();
		ensureLists(config);
		List<VeloraConfig.ItemNameRule> rules = activeRules(config);
		int width = Math.max(1, Math.min(480, this.width - 20));
		int left = (this.width - width) / 2;
		int toggleWidth = 54;
		int removeWidth = 24;
		int fieldWidth = Math.max(48, (width - toggleWidth - removeWidth - 18) / 2);

		addDrawableChild(ButtonWidget.builder(Text.literal(higherGroup ? "Group: HIGHER (click to switch)" : "Group: LOWER (click to switch)"), b ->
				this.client.setScreen(new NameMappingScreen(parent, !higherGroup, 0)))
				.dimensions(left, 38, width, 20).build());

		int pageCount = Math.max(1, (rules.size() + VISIBLE_ROWS - 1) / VISIBLE_ROWS);
		int currentPage = Math.min(page, pageCount - 1);
		int start = currentPage * VISIBLE_ROWS;
		int end = Math.min(rules.size(), start + VISIBLE_ROWS);

		for (int index = start; index < end; index++) {
			VeloraConfig.ItemNameRule rule = rules.get(index);
			int rowY = TOP + (index - start) * ROW_HEIGHT;
			TextFieldWidget received = new TextFieldWidget(this.textRenderer, left, rowY, fieldWidth, 20,
					Text.literal("Received name"));
			received.setMaxLength(128);
			received.setPlaceholder(Text.literal("Received name"));
			received.setText(rule.receivedName == null ? "" : rule.receivedName);
			received.setChangedListener(value -> {
				rule.receivedName = value;
				VeloraConfig.save();
			});
			addDrawableChild(received);

			int outputX = left + fieldWidth + 6;
			TextFieldWidget displayed = new TextFieldWidget(this.textRenderer, outputX, rowY, fieldWidth, 20,
					Text.literal("Display as"));
			displayed.setMaxLength(128);
			displayed.setPlaceholder(Text.literal("Display as"));
			displayed.setText(rule.displayedName == null ? "" : rule.displayedName);
			displayed.setChangedListener(value -> {
				rule.displayedName = value;
				VeloraConfig.save();
			});
			addDrawableChild(displayed);

			int toggleX = outputX + fieldWidth + 6;
			addDrawableChild(ButtonWidget.builder(Text.literal(rule.enabled ? "ON" : "OFF"), b -> {
				rule.enabled = !rule.enabled;
				VeloraConfig.save();
				this.client.setScreen(new NameMappingScreen(parent, higherGroup, currentPage));
			}).dimensions(toggleX, rowY, toggleWidth, 20).build());

			int removeX = toggleX + toggleWidth + 6;
			addDrawableChild(ButtonWidget.builder(Text.literal("×"), b -> {
				rules.remove(rule);
				VeloraConfig.save();
				this.client.setScreen(new NameMappingScreen(parent, higherGroup, currentPage));
			}).dimensions(removeX, rowY, removeWidth, 20).build());
		}

		int bottomY = Math.min(this.height - 54, TOP + VISIBLE_ROWS * ROW_HEIGHT + 1);
		addDrawableChild(ButtonWidget.builder(Text.literal("+ Add mapping"), b -> {
			rules.add(new VeloraConfig.ItemNameRule("", "", true));
			VeloraConfig.save();
			int newPage = Math.max(0, (rules.size() - 1) / VISIBLE_ROWS);
			this.client.setScreen(new NameMappingScreen(parent, higherGroup, newPage));
		}).dimensions(left, bottomY, width, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.literal("‹"), b ->
				this.client.setScreen(new NameMappingScreen(parent, higherGroup, Math.max(0, currentPage - 1))))
				.dimensions(left, bottomY + 24, 24, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Page " + (currentPage + 1) + "/" + pageCount), b -> {})
				.dimensions(left + 28, bottomY + 24, Math.max(60, width - 56), 20).build());

		addDrawableChild(ButtonWidget.builder(Text.literal("›"), b ->
				this.client.setScreen(new NameMappingScreen(parent, higherGroup, Math.min(currentPage + 1, pageCount - 1))))
				.dimensions(left + width - 24, bottomY + 24, 24, 20).build());

		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
				.dimensions(this.width / 2 - 100, this.height - 28, 200, 20).build());
	}

	private List<VeloraConfig.ItemNameRule> activeRules(VeloraConfig config) {
		return higherGroup ? config.higherNameMappings : config.lowerNameMappings;
	}

	private static void ensureLists(VeloraConfig config) {
		if (config.lowerNameMappings == null) {
			config.lowerNameMappings = new ArrayList<>();
		}
		if (config.higherNameMappings == null) {
			config.higherNameMappings = new ArrayList<>();
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Type the received name and its replacement. Toggle each rule ON/OFF."),
				this.width / 2, 25, 0xFFAAAAAA);
		context.drawTextWithShadow(this.textRenderer, Text.literal("Received name"), 10, TOP - 12, 0xFFAAAAAA);
		context.drawTextWithShadow(this.textRenderer, Text.literal("Display as"), this.width / 2 - 28, TOP - 12, 0xFFAAAAAA);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Matching uses names only. Keep received names unique across groups."),
				this.width / 2, this.height - 43, 0xFFAAAAAA);
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}
}
