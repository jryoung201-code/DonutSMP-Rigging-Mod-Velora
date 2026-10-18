package dev.jry.velora.mixin;

import dev.jry.velora.display.DisplayResolver;
import dev.jry.velora.display.SlotDisplay;
import dev.jry.velora.display.VeloraRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Render-only hooks into every container screen.
 *
 * <p>Both injections only change what is DRAWN. Neither touches slot contents, the screen
 * handler, click handling or packets, so clicking a slot still acts on the real server slot.
 * Which slots get restyled is decided by {@link DisplayResolver}.
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
	@Shadow
	@Final
	protected T handler;

	@Shadow
	protected Slot focusedSlot;

	protected HandledScreenMixin(Text title) {
		super(title);
	}

	/**
	 * Replaces only the FIRST tooltip line (the item name) with the local label. A copy of
	 * the tooltip text is edited; the real ItemStack and its name are left alone.
	 */
	@Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
	private void velora$drawTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
		if (this.focusedSlot == null || !this.handler.getCursorStack().isEmpty()) {
			return; // vanilla shows nothing here either
		}
		HandledScreen<?> self = (HandledScreen<?>) (Object) this;
		Optional<SlotDisplay> display = DisplayResolver.resolve(self, this.focusedSlot);
		if (display.isEmpty()) {
			return; // not ours: let vanilla draw its normal tooltip
		}

		List<Text> lines = new ArrayList<>(this.getTooltipFromItem(this.client, this.focusedSlot.getStack()));
		if (lines.isEmpty()) {
			lines.add(Text.empty());
		}
		lines.set(0, Text.literal(display.get().label()));

		context.drawTooltip(this.textRenderer, lines, x, y);
		VeloraRenderer.drawMapPreview(context, x, y, this.height, display.get());
		ci.cancel();
	}
}
