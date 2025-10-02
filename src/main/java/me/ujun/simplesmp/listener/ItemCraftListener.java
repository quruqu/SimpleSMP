package me.ujun.simplesmp.listener;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class ItemCraftListener implements Listener {
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack result = event.getCurrentItem();
        result.setData(DataComponentTypes.MAX_STACK_SIZE, 32);
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        result.setData(DataComponentTypes.MAX_STACK_SIZE, 32);
    }
}
