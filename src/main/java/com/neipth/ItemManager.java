package com.neipth;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ItemManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path configPath = Paths.get("config/items_control.json");
    private final Set<String> disabledItemIds = new HashSet<>();

    public ItemManager() {
        loadConfig();
    }

    public void disableItem(Identifier itemId) {
        disabledItemIds.add(itemId.toString());
        saveConfig();
    }

    public void enableItem(Identifier itemId) {
        disabledItemIds.remove(itemId.toString());
        saveConfig();
    }

    public boolean isItemEnabled(Identifier itemId) {
        return !disabledItemIds.contains(itemId.toString());
    }

    public Set<Identifier> getDisabledItems() {
        return disabledItemIds.stream()
                .map(this::createIdentifier)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public List<Item> getAllCraftableItems() {
        return Registries.ITEM.stream().collect(Collectors.toList());
    }

    private Identifier createIdentifier(String idStr) {
        try {
            return Identifier.of(idStr);
        } catch (Exception e) {
            RemoveRecipe.LOGGER.warn("ID inválido: " + idStr, e);
            return null;
        }
    }

    private void loadConfig() {
        try {
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                JsonArray array = JsonParser.parseString(json).getAsJsonArray();

                disabledItemIds.clear();

                for (JsonElement element: array) {
                    String idStr = element.getAsString();

                    try {
                        // Validamos creando el ID
                        Identifier.of(idStr);
                        disabledItemIds.add(idStr);
                    } catch (Exception e) {
                        RemoveRecipe.LOGGER.warn("ID inválido en configuración: " + idStr);
                    }
                }
            }
        } catch (Exception e) {
            RemoveRecipe.LOGGER.error("Error al guardar configuración ", e);
        }
    }

    private void saveConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            JsonArray array = new JsonArray();
            disabledItemIds.forEach(array::add);
            Files.writeString(configPath, GSON.toJson(array));
        } catch (Exception e) {
            RemoveRecipe.LOGGER.error("Error al guardar configuración ", e);
        }
    }
}
