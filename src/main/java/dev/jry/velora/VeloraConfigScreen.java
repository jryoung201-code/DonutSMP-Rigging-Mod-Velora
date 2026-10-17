package dev.jry.velora;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class VeloraConfigScreen extends Screen {
    private static final int SLOT_SIZE = 22;
    private static final int SLOT_GAP = 4;

    public VeloraConfigScreen() {
        super(Text.literal("Velora Settings"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 + 24;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Pattern: " + VeloraConfig.get().pattern),
                button -> {
                    VeloraConfig.get().cyclePattern();
                    button.setMessage(Text.literal("Pattern: " + VeloraConfig.get().pattern));
                }
        ).dimensions(centerX - 100, y, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Inventory name patterns: " +
                        (VeloraConfig.get().inventoryNamePatternEnabled ? "ON" : "OFF")),
                button -> {
                    VeloraConfig.get().inventoryNamePatternEnabled =
                            !VeloraConfig.get().inventoryNamePatternEnabled;
                    VeloraConfig.save();
                    button.setMessage(Text.literal("Inventory name patterns: " +
                            (VeloraConfig.get().inventoryNamePatternEnabled ? "ON" : "OFF")));
                }
        ).dimensions(centerX - 100, y + 26, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Local pattern preview: " +
                        (VeloraConfig.get().localPatternPreviewEnabled ? "ON" : "OFF")),
                button -> {
                    VeloraConfig.get().localPatternPreviewEnabled =
                            !VeloraConfig.get().localPatternPreviewEnabled;
                    VeloraConfig.save();
                    button.setMessage(Text.literal("Local pattern preview: " +
                            (VeloraConfig.get().localPatternPreviewEnabled ? "ON" : "OFF")));
                }
        ).dimensions(centerX - 100, y + 52, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"), button -> close()
        ).dimensions(centerX - 100, y + 80, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Local pattern preview (visual test only)"),
                width / 2, 36, 0xFFBBAAFF);

        if (VeloraConfig.get().localPatternPreviewEnabled) {
            drawPreviewRow(context, "Lower", getLowerPattern(), height / 2 - 52);
            drawPreviewRow(context, "Higher", getHigherPattern(), height / 2 - 20);
        } else {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Preview is off"), width / 2, height / 2 - 25, 0xFFAAAAAA);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private String getLowerPattern() {
        return "A".equals(VeloraConfig.get().pattern) ? "123412345" : "123456789";
    }

    private String getHigherPattern() {
        return "A".equals(VeloraConfig.get().pattern) ? "567856789" : "123456789";
    }

    private void drawPreviewRow(DrawContext context, String label, String digits, int y) {
        int totalWidth = 9 * SLOT_SIZE + 8 * SLOT_GAP;
        int startX = (width - totalWidth) / 2;
        context.drawTextWithShadow(textRenderer, label, startX - textRenderer.getWidth(label) - 8,
                y + 7, 0xFFFFFFFF);
        for (int i = 0; i < 9; i++) {
            int x = startX + i * (SLOT_SIZE + SLOT_GAP);
            context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF292634);
            context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF17151E);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(String.valueOf(digits.charAt(i))),
                    x + SLOT_SIZE / 2, y + 7, 0xFFD9C8FF);
        }
    }

    @Override
    public void close() {
        VeloraConfig.save();
        if (client != null) client.setScreen(null);
    }
}
