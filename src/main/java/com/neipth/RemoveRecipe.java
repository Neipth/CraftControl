package com.neipth;

import com.neipth.commands.ItemsManagerCommand;
import com.neipth.events.CraftingEvents;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveRecipe implements ModInitializer {
	public static final String MOD_ID = "remove-recipe";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ItemManager ITEM_MANAGER = new ItemManager();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			ItemsManagerCommand.register(dispatcher);
		});
		CraftingEvents.register();
		LOGGER.info("Craftable Items Mod initialized!");
	}
}