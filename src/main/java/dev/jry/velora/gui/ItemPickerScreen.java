package dev.jry.velora.gui;

import dev.jry.velora.config.VeloraConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Searchable grid of every item in the game. The item that is already used for the other
 * slot role is disabled, so Item 1 and Item 2 can never be the same.
 */
public class ItemPickerScreen extends Screen {
	private static final int COLUMNS = 9;
	private static final int CELL = 22;
	private static final int GRID_TOP = 56;

	private final Screen parent;
	private final Identifier current;
	private final Identifier disallowed;
	private final Consumer<Identifier> onPick;
	private final List<Item> allItems = new ArrayList<>();

	private record Cell(Item item, ItemStack stack, Identifier id, ButtonWidget button) {
	}

	private final List<Cell> cells = new ArrayList<>();
	private List<Item> filtered = List.of();
	private TextFieldWidget search;
	private ButtonWidget previousButton;
	private ButtonWidget nextButton;
	private String query = "";
	private int page;
	private int rows;

	public ItemPickerScreen(Screen parent, Text title, Identifier current, Identifier disallowed, Consumer<Identifier> onPick) {
		super(title);
		this.parent = parent;
		this.current = current;
		this.disallowed = disallowed;
		this.onPick = onPick;

		Registries.ITEM.stream()
				.filter(item -> item != Items.AIR)
				.sorted(Comparator.comparing(item -> Registries.ITEM.getId(item).toString()))
				.forEach(allItems::add);
	}

	@Override
	protected void init() {
		cells.clear(); // the old buttons were dropped with the previous layout
		rows = Math.max(2, Math.min(6, (this.height - GRID_TOP - 70) / CELL));
		int gridWidth = COLUMNS * CELL;
		int left = this.width / 2 - gridWidth / 2;
		int pagerY = GRID_TOP + rows * CELL + 4;

		search = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 28, 200, 18, Text.literal("Search"));
		search.setPlaceholder(Text.literal("Search items..."));
		search.setText(query);
		search.setChangedListener(text -> {
			query = text;
			page = 0;
			applyFilter();
		});
		addDrawableChild(search);
		setInitialFocus(search);

		previousButton = addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> {
			page--;
			rebuildGrid();
		}).dimensions(left, pagerY, 20, 20).build());
		nextButton = addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> {
			page++;
			rebuildGrid();
		}).dimensions(left + gridWidth - 20, pagerY, 20, 20).build());

		addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> close())
				.dimensions(this.width / 2 - 100, this.height - 28, 200, 20).build());

		applyFilter();
	}

	private void applyFilter() {
		String needle = query.toLowerCase(Locale.ROOT).trim();
		filtered = allItems.stream()
				.filter(item -> needle.isEmpty()
						|| Registries.ITEM.getId(item).toString().contains(needle)
						|| item.getName().getString().toLowerCase(Locale.ROOT).contains(needle))
				.toList();
		rebuildGrid();
	}

	private void rebuildGrid() {
		for (Cell cell : cells) {
			remove(cell.button());
		}
		cells.clear();

		int pageSize = COLUMNS * rows;
		int pageCount = Math.max(1, (filtered.size() + pageSize - 1) / pageSize);
		page = Math.max(0, Math.min(page, pageCount - 1));

		int left = this.width / 2 - (COLUMNS * CELL) / 2;
		int start = page * pageSize;
		int end = Math.min(filtered.size(), start + pageSize);
		for (int i = start; i < end; i++) {
			Item item = filtered.get(i);
			Identifier id = Registries.ITEM.getId(item);
			int slot = i - start;
			int x = left + (slot % COLUMNS) * CELL;
			int y = GRID_TOP + (slot / COLUMNS) * CELL;

			ButtonWidget button = ButtonWidget.builder(Text.empty(), b -> pick(id))
					.dimensions(x, y, CELL - 2, CELL - 2).build();
			button.active = !id.equals(disallowed);
			addDrawableChild(button);
			cells.add(new Cell(item, new ItemStack(item), id, button));
		}

		previousButton.active = page > 0;
		nextButton.active = page < pageCount - 1;
	}

	private void pick(Identifier id) {
		if (id.equals(disallowed)) {
			return;
		}
		onPick.accept(id);
		VeloraConfig.save();
		close();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);

		for (Cell cell : cells) {
			ButtonWidget button = cell.button();
			context.drawItem(cell.stack(), button.getX() + 1, button.getY() + 1);
			if (cell.id().equals(current)) {
				context.drawStrokedRectangle(button.getX() - 1, button.getY() - 1, button.getWidth() + 2, button.getHeight() + 2, 0xFFFFFF55);
			}
		}

		int pageSize = COLUMNS * rows;
		int pageCount = Math.max(1, (filtered.size() + pageSize - 1) / pageSize);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Page " + (page + 1) + "/" + pageCount + "  (" + filtered.size() + " items)"),
				this.width / 2, GRID_TOP + rows * CELL + 10, 0xFFAAAAAA);

		for (Cell cell : cells) {
			if (cell.button().isMouseOver(mouseX, mouseY)) {
				List<Text> tooltip = new ArrayList<>();
				tooltip.add(cell.item().getName());
				tooltip.add(Text.literal(cell.id().toString()).formatted(Formatting.GRAY));
				if (cell.id().equals(disallowed)) {
					tooltip.add(Text.literal("Already used by the other item").formatted(Formatting.RED));
				}
				context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
				break;
			}
		}
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}
}
