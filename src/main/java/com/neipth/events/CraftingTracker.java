package com.neipth.events;

import com.neipth.RemoveRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class CraftingTracker {
    private static ItemStack lastCraftedStack = ItemStack.EMPTY;

    public static void checkCraftedItem(ServerPlayerEntity player, ScreenHandler handler) {
        if (handler instanceof CraftingScreenHandler) {
            // El slot 0 suele ser el resultado en la mesa de crafteo
            Slot resultSlot = handler.slots.get(0);
            ItemStack result = resultSlot.getStack();

            if (!result.isEmpty()) {
                Identifier id = Registries.ITEM.getId(result.getItem());
                if (!RemoveRecipe.ITEM_MANAGER.isItemEnabled(id)) {
                    player.sendMessage(Text.literal("¡No puedes craftear este ítem!").formatted(Formatting.RED), false);
                    resultSlot.setStack(ItemStack.EMPTY); // Borra el resultado
                    player.closeHandledScreen(); // Cierra la mesa de crafteo
                }
            }
        }
    }
}
