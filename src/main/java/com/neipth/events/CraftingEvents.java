package com.neipth.events;

import com.neipth.RemoveRecipe;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class CraftingEvents {
    private static final int CHECK_INTERVAL = 100; // Check every 100 ticks (5 seconds)
    private static int tickCounter = 0;

    public static void register() {
        // Evento para bloquear el uso directo de items
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient()) {
                ItemStack stack = player.getStackInHand(hand);
                Identifier id = Registries.ITEM.getId(stack.getItem());

                if (!RemoveRecipe.ITEM_MANAGER.isItemEnabled(id)) {
                    player.sendMessage(Text.literal("Este item está desactivado").formatted(Formatting.RED), true);
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });

        // Verificar crafteos cada tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                CraftingTracker.checkCraftedItem(player, player.currentScreenHandler);

                // Check for illegal items periodically
                tickCounter++;
                if (tickCounter >= CHECK_INTERVAL) {
                    checkPlayerInventory(player);
                    tickCounter = 0;
                }
            });
        });
    }

    private static void checkPlayerInventory(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                Identifier id = Registries.ITEM.getId(stack.getItem());
                if (!RemoveRecipe.ITEM_MANAGER.isItemEnabled(id)) {
                    player.sendMessage(Text.literal("Tenías un ítem desactivado ilegalmente: " + id).formatted(Formatting.RED), false);
                    inventory.setStack(i, ItemStack.EMPTY); // Destruye el ítem
                }
            }
        }
    }
}
