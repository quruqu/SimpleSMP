package me.ujun.simplesmp.listener;

import me.ujun.simplesmp.config.ConfigHandler;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ConcurrentModificationException;
import java.util.List;

public class EntityDeathListener implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntityType() == EntityType.EVOKER) {

            List<ItemStack> drops = e.getDrops();

            drops.removeIf(item -> {
                if (item.getType() == Material.TOTEM_OF_UNDYING) {
                    double chance = ConfigHandler.totemDropChance;
                    return Math.random() > chance;
                }
                return false;
            });
        }
    }
}
