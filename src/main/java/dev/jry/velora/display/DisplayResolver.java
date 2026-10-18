package dev.jry.velora.display;

import dev.jry.velora.config.DisplayPattern;
import dev.jry.velora.config.DisplayMode;
import dev.jry.velora.config.VeloraConfig;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * The heart of Velora's client-side rendering system.
 *
 * <p>ACTUAL DATA (the real ItemStack, item id, map id, slot, server state) is only ever
 * READ here. DISPLAY DATA ({@link SlotDisplay}) is what comes out. The renderer draws that
 * at its local destination. Nothing is written back: the handler retains its original slots,
 * and visual hit tests are translated back to the real source slot before vanilla processes
 * clicks and drags.
 *
 * <p>The stack is re-read on every call. Nothing is cached, so server inventory updates are
 * picked up on the next frame.
 *
 * <p>Items are recognized by their real identity (item registry id, or the map id component).
 * Item Names mode reads the numeric custom name; map mode still uses its screen position.
 */
public final class DisplayResolver {
	private static final List<DisplayScope> SCOPES = List.of(new AllSlotsScope());

	private DisplayResolver() {
	}

	/** @return the local display for this slot, or empty if the slot should look vanilla. */
	public static Optional<SlotDisplay> resolve(HandledScreen<?> screen, Slot slot) {
		if (slot == null || !slot.hasStack()) {
			return Optional.empty();
		}

		OptionalInt position = OptionalInt.empty();
		for (DisplayScope scope : SCOPES) {
			position = scope.positionOf(screen, slot);
			if (position.isPresent()) {
				break;
			}
		}
		if (position.isEmpty()) {
			return Optional.empty();
		}

		return resolve(slot.getStack(), position.getAsInt());
	}

	/** Resolves a stack using a zero-based repeating display position. */
	public static Optional<SlotDisplay> resolve(ItemStack stack, int position) {
		if (stack == null || stack.isEmpty()) {
			return Optional.empty();
		}

		VeloraConfig config = VeloraConfig.get();
		return switch (config.mode) {
			case ITEM_NAMES -> resolveItemName(config, stack, position);
			case MAPS -> resolveMap(config, stack, position);
		};
	}

	/** Returns this container slot's visual destination when its item-number layout is valid. */
	public static OptionalInt visualContainerPosition(HandledScreen<?> screen, Slot requestedSlot) {
		if (requestedSlot == null || requestedSlot.id < 0 || requestedSlot.id >= DisplayPattern.SLOT_COUNT) {
			return OptionalInt.empty();
		}
		int[] destinations = visualContainerDestinations(screen);
		return destinations == null ? OptionalInt.empty() : OptionalInt.of(destinations[requestedSlot.id]);
	}

	/** Resolves a slot found at a displayed position back to the source slot it represents. */
	public static Optional<Slot> visualContainerSourceSlot(HandledScreen<?> screen, Slot displayedSlot) {
		if (displayedSlot == null || displayedSlot.id < 0 || displayedSlot.id >= DisplayPattern.SLOT_COUNT) {
			return Optional.empty();
		}
		int[] destinations = visualContainerDestinations(screen);
		if (destinations == null) {
			return Optional.empty();
		}
		for (int source = 0; source < destinations.length; source++) {
			if (destinations[source] == displayedSlot.id) {
				return Optional.of(screen.getScreenHandler().slots.get(source));
			}
		}
		return Optional.empty();
	}

	private static int[] visualContainerDestinations(HandledScreen<?> screen) {
		if (!(screen.getScreenHandler() instanceof net.minecraft.screen.Generic3x3ContainerScreenHandler)
				|| VeloraConfig.get().mode != DisplayMode.ITEM_NAMES) {
			return null;
		}
		Slot[] containerSlots = new Slot[DisplayPattern.SLOT_COUNT];
		for (Slot slot : screen.getScreenHandler().slots) {
			if (slot.id >= 0 && slot.id < containerSlots.length) {
				containerSlots[slot.id] = slot;
			}
		}
		if (Arrays.stream(containerSlots).anyMatch(slot -> slot == null)) {
			return null;
		}

		boolean[] destinationsUsed = new boolean[DisplayPattern.SLOT_COUNT];
		int[] destinations = new int[DisplayPattern.SLOT_COUNT];
		Arrays.fill(destinations, -1);
		for (int source = 0; source < containerSlots.length; source++) {
			Slot slot = containerSlots[source];
			if (!slot.hasStack()) {
				continue;
			}

			OptionalInt mappedPosition = resolve(slot.getStack(), source)
					.map(display -> display.number() - 1)
					.filter(number -> number >= 0 && number < DisplayPattern.SLOT_COUNT)
					.map(OptionalInt::of)
					.orElseGet(OptionalInt::empty);
			if (mappedPosition.isPresent() && !destinationsUsed[mappedPosition.getAsInt()]) {
				destinations[source] = mappedPosition.getAsInt();
				destinationsUsed[mappedPosition.getAsInt()] = true;
			}
		}

		for (int source = 0; source < containerSlots.length; source++) {
			if (containerSlots[source].hasStack() && destinations[source] < 0) {
				destinations[source] = firstAvailableDestination(destinationsUsed, source);
			}
		}
		for (int source = 0; source < containerSlots.length; source++) {
			if (!containerSlots[source].hasStack()) {
				destinations[source] = firstAvailableDestination(destinationsUsed, source);
			}
		}

		return destinations;
	}

	private static int firstAvailableDestination(boolean[] destinationsUsed, int preferred) {
		if (!destinationsUsed[preferred]) {
			destinationsUsed[preferred] = true;
			return preferred;
		}
		for (int destination = 0; destination < destinationsUsed.length; destination++) {
			if (!destinationsUsed[destination]) {
				destinationsUsed[destination] = true;
				return destination;
			}
		}
		return preferred;
	}

	/** Returns the selected LOSE item's model id for WIN/LOSE stacks when the visual toggle is on. */
	public static Optional<Identifier> winLoseModelId(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return Optional.empty();
		}

		VeloraConfig config = VeloraConfig.get();
		if (!config.winLoseEnabled) {
			return Optional.empty();
		}

		String itemId = Registries.ITEM.getId(stack.getItem()).toString();
		if (!itemId.equals(config.winItem) && !itemId.equals(config.loseItem)) {
			return Optional.empty();
		}

		Identifier loseId = config.loseItemId();
		if (loseId == null || !Registries.ITEM.containsId(loseId)) {
			return Optional.empty();
		}
		ItemStack loseStack = new ItemStack(Registries.ITEM.get(loseId));
		return Optional.ofNullable(loseStack.get(DataComponentTypes.ITEM_MODEL));
	}

	/** Returns a new render-only stack for a configured substitution, never the original stack. */
	public static Optional<ItemStack> visualSwapStack(ItemStack originalStack) {
		if (originalStack == null || originalStack.isEmpty()) {
			return Optional.empty();
		}

		String sourceId = Registries.ITEM.getId(originalStack.getItem()).toString();
		return VeloraConfig.get().visualDisplayItemId(sourceId)
				.map(displayId -> new ItemStack(Registries.ITEM.get(displayId)));
	}

	private static Optional<SlotDisplay> resolveItemName(VeloraConfig config, ItemStack stack, int position) {
		String itemId = Registries.ITEM.getId(stack.getItem()).toString();
		DisplayPattern pattern = config.pattern;
		String realName = stack.get(DataComponentTypes.CUSTOM_NAME) == null
				? null
				: stack.get(DataComponentTypes.CUSTOM_NAME).getString().trim();
		if (realName == null || !realName.matches("[1-9]")) {
			return Optional.empty();
		}
		int namePosition = Integer.parseInt(realName) - 1;

		int number;
		if (itemId.equals(config.item1)) {
			number = pattern.lowerNumber(namePosition);
		} else if (itemId.equals(config.item2)) {
			number = pattern.higherNumber(namePosition);
		} else {
			return Optional.empty();
		}
		return Optional.of(new SlotDisplay(String.valueOf(number), number, null));
	}

	private static Optional<SlotDisplay> resolveMap(VeloraConfig config, ItemStack stack, int position) {
		if (!stack.isOf(Items.FILLED_MAP)) {
			return Optional.empty();
		}
		MapIdComponent mapId = stack.get(DataComponentTypes.MAP_ID);
		// Only maps whose real server id is one of the nine known ids are restyled.
		if (mapId == null || MapIds.knownMapNumber(mapId.id()) < 0) {
			return Optional.empty();
		}

		int number = config.pattern.number(position);
		return Optional.of(new SlotDisplay(
				"Map " + number,
				number,
				MapArt.texture(number, config.map9LocalOverride)));
	}
}
