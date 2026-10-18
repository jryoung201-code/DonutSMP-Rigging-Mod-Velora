package dev.jry.velora.mixin;

import dev.jry.velora.display.DisplayResolver;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.HeldItemContext;
import net.minecraft.world.World;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ItemModelManager.class)
public abstract class ItemModelManagerMixin {
	private static final ThreadLocal<Boolean> VELORA_SUPPRESS_LEGACY = ThreadLocal.withInitial(() -> false);

	@WrapMethod(method = "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/world/World;Lnet/minecraft/util/HeldItemContext;I)V")
	private void velora$applyVisualItemSwap(ItemRenderState state, ItemStack stack, ItemDisplayContext context,
			World world, HeldItemContext heldItemContext, int seed, Operation<Void> original) {
		Optional<ItemStack> replacement = DisplayResolver.visualSwapStack(stack);
		if (replacement.isEmpty()) {
			original.call(state, stack, context, world, heldItemContext, seed);
			return;
		}

		boolean previous = VELORA_SUPPRESS_LEGACY.get();
		VELORA_SUPPRESS_LEGACY.set(true);
		try {
			original.call(state, replacement.get(), context, world, heldItemContext, seed);
		} finally {
			VELORA_SUPPRESS_LEGACY.set(previous);
		}
	}

	@WrapOperation(
			method = "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/world/World;Lnet/minecraft/util/HeldItemContext;I)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
	)
	private Object velora$useLoseItemModel(ItemStack stack, ComponentType<?> type, Operation<Object> original) {
		Object originalModelId = original.call(stack, type);
		if (!VELORA_SUPPRESS_LEGACY.get() && type == DataComponentTypes.ITEM_MODEL) {
			Optional<Identifier> modelId = DisplayResolver.winLoseModelId(stack);
			if (modelId.isPresent()) {
				return modelId.get();
			}
		}
		return originalModelId;
	}
}