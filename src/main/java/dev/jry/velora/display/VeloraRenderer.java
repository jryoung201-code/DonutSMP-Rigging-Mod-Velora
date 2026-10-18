package dev.jry.velora.display;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;

/**
 * Draws Velora's local overlays. Every method only issues GUI draw calls; none of them
 * touches an ItemStack, a slot or the network.
 */
public final class VeloraRenderer {
	/** Source size of the bundled map art PNGs. */
	private static final int ART_SIZE = 128;

	private VeloraRenderer() {
	}

	/** Larger map preview shown next to the cursor while hovering a restyled map. */
	public static void drawMapPreview(DrawContext context, int cursorX, int cursorY, int screenHeight, SlotDisplay display) {
		if (display.art() == null) {
			return;
		}
		int size = 64;
		int x = cursorX + 12;
		int y = Math.min(cursorY + 12, screenHeight - size - 4);
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(0.0f, 0.0f);
		drawArt(context, display.art(), x, y, size);
		context.getMatrices().popMatrix();
	}

	/** Draws the whole 128x128 art texture scaled to {@code size} x {@code size}. */
	public static void drawArt(DrawContext context, Identifier texture, int x, int y, int size) {
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, size, size);
	}
}
