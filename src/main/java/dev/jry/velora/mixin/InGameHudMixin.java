package dev.jry.velora.mixin;

import dev.jry.velora.display.DisplayResolver;
import dev.jry.velora.display.SlotDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Redirect(
			method = "renderHeldItemTooltip",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;")
	)
	private Text velora$useDisplayName(ItemStack stack) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			int position = client.player.getInventory().getSelectedSlot();
			Optional<SlotDisplay> display = DisplayResolver.resolve(stack, position);
			if (display.isPresent()) {
				return Text.literal(display.get().label());
			}
		}
		return stack.getName();
	}
}