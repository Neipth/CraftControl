package com.neipth.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.neipth.ItemManager;
import com.neipth.RemoveRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.*;

public class ItemsManagerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("itemcontrol")
                .requires(source -> source.hasPermissionLevel(2)) // Solo OPs
                // List
                .then(literal("list")
                        .executes(ctx -> listItems(ctx.getSource())))

                // Disable
                .then(literal("disable")
                        .then(argument("item", StringArgumentType.greedyString())
                                .suggests(ItemsManagerCommand::suggestItems)
                                .executes(ctx -> disableItem(ctx.getSource(), StringArgumentType.getString(ctx, "item")))))

                // Enable
                .then(literal("enable")
                        .then(argument("item", StringArgumentType.greedyString())
                                .suggests(ItemsManagerCommand::suggestDisabledItems)
                                .executes(ctx -> enableItem(ctx.getSource(), StringArgumentType.getString(ctx, "item")))))

                // Disable
                .then(literal("disabled")
                        .executes(ctx -> listDisabledItems(ctx.getSource())))
        );
    }

    private static CompletableFuture<Suggestions> suggestItems(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        Registries.ITEM.getIds().forEach(id -> {
            String idStr = id.toString();
            if (idStr.contains(builder.getRemaining().toLowerCase())) {
                builder.suggest(idStr);
            }
        });
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestDisabledItems(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        RemoveRecipe.ITEM_MANAGER.getDisabledItems().forEach(id -> {
            if (id.toString().contains(builder.getRemaining().toLowerCase())) {
                builder.suggest(id.toString());
            }
        });
        return builder.buildFuture();
    }

    private static int disableItem(ServerCommandSource source, String itemId) {
        // Limpiar espacios en blanco
        itemId = itemId.trim();

        // Añadir namespace "minecraft:" si no tiene uno
        if (!itemId.contains(":")) {
            itemId = "minecraft:" + itemId;
        }

        // Validar el ID
        Identifier id;
        try {
            id = Identifier.of(itemId);
        } catch (Exception e) {
            source.sendError(Text.literal("ID de ítem inválido: " + itemId + " - Debe ser en formato 'namespace:item'"));
            return 0;
        }

        // Verificar que el ítem existe
        if (!Registries.ITEM.containsId(id)) {
            source.sendError(Text.literal("Ítem no encontrado: " + id));
            return 0;
        }

        // Desactivar el ítem
        RemoveRecipe.ITEM_MANAGER.disableItem(id);
        source.sendFeedback(() -> Text.literal("Desactivado: " + id).formatted(Formatting.RED), false);
        return 1;
    }

    private static int enableItem(ServerCommandSource source, String itemId) {
        // Limpiar espacios en blanco
        itemId = itemId.trim();

        // Si no tiene namespace, añadir minecraft:
        if (!itemId.contains(":")) {
            itemId = "minecraft:" + itemId;
        }

        // Validar el ID
        Identifier id;
        try {
            id = Identifier.of(itemId);
        } catch (Exception e) {
            source.sendError(Text.literal("ID de ítem inválido: " + itemId + " - Debe ser en formato 'namespace:item'"));
            return 0;
        }

        // Verificar que el ítem existe
        if (!Registries.ITEM.containsId(id)) {
            source.sendError(Text.literal("Ítem no encontrado: " + id));
            return 0;
        }

        // Activar el ítem
        RemoveRecipe.ITEM_MANAGER.enableItem(id);
        source.sendFeedback(() -> Text.literal("Activado: " + id).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int listItems(ServerCommandSource source) {
        ItemManager manager = RemoveRecipe.ITEM_MANAGER;
        source.sendFeedback(() -> Text.literal("Items crafteables: ").formatted(Formatting.GOLD), false);

        manager.getAllCraftableItems().forEach(item -> {
            Identifier id = Registries.ITEM.getId(item);
            source.sendFeedback(() -> Text.literal("- "+id), false);
        });
        return 1;
    }

    private static int listDisabledItems(ServerCommandSource source) {
        ItemManager manager = RemoveRecipe.ITEM_MANAGER;
        Set<Identifier> disabled = manager.getDisabledItems();

        if (disabled.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No Hay Items desactivados").formatted(Formatting.YELLOW), false);
        } else {
            source.sendFeedback(() -> Text.literal("Items desactivados:").formatted(Formatting.GOLD), false);
            disabled.forEach(id -> {
                source.sendFeedback(() -> Text.literal("- " + id), false);
            });
        }

        return disabled.size();
    }
}
