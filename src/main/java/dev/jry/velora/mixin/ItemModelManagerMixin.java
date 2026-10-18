package dev.jry.velora.mixin;

import dev.jry.velora.display.DisplayResolver;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ItemModelManager.class)
public abstract class ItemModelManagerMixin {
	@WrapOperation(
			method = "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/world/World;Lnet/minecraft/util/HeldItemContext;I)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
	)
	private Object velora$useLoseItemModel(ItemStack stack, ComponentType<?> type, Operation<Object> original) {
		Object originalModelId = original.call(stack, type);
		if (type == DataComponentTypes.ITEM_MODEL) {
			Optional<Identifier> modelId = DisplayResolver.winLoseModelId(stack);
			if (modelId.isPresent()) {
				return modelId.get();
			}
		}
		return originalModelId;
	}
}