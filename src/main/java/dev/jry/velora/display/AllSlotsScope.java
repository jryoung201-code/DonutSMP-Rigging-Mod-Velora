package dev.jry.velora.display;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.TranslatableTextContent;

import java.util.OptionalInt;

/**
 * Applies the display pattern to inventory slots, repeating every nine handler slots.
 *
 * <p>When a vanilla dropper is open, its own nine container slots are deliberately
 * left with vanilla display names. Player inventory/hotbar slots remain eligible for
 * the normal local display pattern. Slot contents and slot order are never changed.
 */
public final class AllSlotsScope implements DisplayScope {
    @Override
    public OptionalInt positionOf(HandledScreen<?> screen, Slot slot) {
        if (slot.id < 0) {
            return OptionalInt.empty();
        }

        boolean dropperScreen = screen.getScreenHandler() instanceof Generic3x3ContainerScreenHandler
                && screen.getTitle().getContent() instanceof TranslatableTextContent title
                && "container.dropper".equals(title.getKey());

        // A dropper's container slots are 0-8. Leave those completely vanilla.
        if (dropperScreen && slot.id < 9) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(slot.id % 9);
    }
}
