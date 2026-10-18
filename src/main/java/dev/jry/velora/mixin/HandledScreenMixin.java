package dev.jry.velora.mixin;

import dev.jry.velora.display.DisplayResolver;
import dev.jry.velora.display.SlotDisplay;
import dev.jry.velora.display.VeloraRenderer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import java.util.OptionalInt;

/**
 * Render-only hooks into every container screen.
 *
 * <p>Both injections only change what is DRAWN. Neither touches slot contents, the screen
 * handler, click handling or packets, so clicking a slot still acts on the real server slot.
 * Which slots get restyled is decided by {@link DisplayResolver}.
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
	private static final ThreadLocal<SlotOffset> VELORA_SLOT_OFFSET = new ThreadLocal<>();

	private record SlotOffset(int x, int y) {
	}

	@Shadow
	@Final
	protected T handler;

	@Shadow
	protected Slot focusedSlot;

	protected HandledScreenMixin(Text title) {
		super(title);
	}

	@WrapMethod(method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;II)V")
	private void velora$drawContainerItemAtMappedPosition(DrawContext context, Slot slot, int mouseX, int mouseY,
			Operation<Void> original) {
		OptionalInt destination = DisplayResolver.visualContainerPosition((HandledScreen<?>) (Object) this, slot);
		if (destination.isEmpty()) {
			original.call(context, slot, mouseX, mouseY);
			return;
		}

		Slot destinationSlot = this.handler.slots.get(destination.getAsInt());
		SlotOffset previous = VELORA_SLOT_OFFSET.get();
		VELORA_SLOT_OFFSET.set(new SlotOffset(destinationSlot.x - slot.x, destinationSlot.y - slot.y));
		try {
			original.call(context, slot, mouseX, mouseY);
		} finally {
			if (previous == null) {
				VELORA_SLOT_OFFSET.remove();
			} else {
				VELORA_SLOT_OFFSET.set(previous);
			}
		}
	}

	@WrapOperation(
			method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;II)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V")
	)
	private void velora$offsetSlotBackground(DrawContext context,
			com.mojang.blaze3d.pipeline.RenderPipeline renderPipeline, net.minecraft.util.Identifier texture,
			int x, int y, int width, int height, Operation<Void> original) {
		SlotOffset offset = VELORA_SLOT_OFFSET.get();
		original.call(context, renderPipeline, texture, x + (offset == null ? 0 : offset.x()),
				y + (offset == null ? 0 : offset.y()), width, height);
	}

	@WrapOperation(
			method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;II)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/item/ItemStack;III)V")
	)
	private void velora$offsetDrawItem(DrawContext context, net.minecraft.item.ItemStack stack, int x, int y, int seed,
			Operation<Void> original) {
		SlotOffset offset = VELORA_SLOT_OFFSET.get();
		original.call(context, stack, x + (offset == null ? 0 : offset.x()), y + (offset == null ? 0 : offset.y()), seed);
	}

	@WrapOperation(
			method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;II)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItemWithoutEntity(Lnet/minecraft/item/ItemStack;III)V")
	)
	private void velora$offsetDrawItemWithoutEntity(DrawContext context, net.minecraft.item.ItemStack stack,
			int x, int y, int seed, Operation<Void> original) {
		SlotOffset offset = VELORA_SLOT_OFFSET.get();
		original.call(context, stack, x + (offset == null ? 0 : offset.x()), y + (offset == null ? 0 : offset.y()), seed);
	}

	@WrapOperation(
			method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;II)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
	)
	private void velora$offsetStackOverlay(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer,
			net.minecraft.item.ItemStack stack, int x, int y, String text, Operation<Void> original) {
		SlotOffset offset = VELORA_SLOT_OFFSET.get();
		original.call(context, textRenderer, stack, x + (offset == null ? 0 : offset.x()),
				y + (offset == null ? 0 : offset.y()), text);
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
