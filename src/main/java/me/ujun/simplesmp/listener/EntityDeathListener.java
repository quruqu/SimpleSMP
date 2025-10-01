package me.ujun.simplesmp.listener;

import me.ujun.simplesmp.config.ConfigHandler;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EntityDeathListener implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntityType() == EntityType.EVOKER) {
            List<ItemStack> drops = e.getDrops();

            double chance = ConfigHandler.totemDropChance;

            if (e.getEntity().getKiller() != null) {
                ItemStack weapon = e.getEntity().getKiller().getInventory().getItemInMainHand();
                if (weapon.containsEnchantment(Enchantment.LOOTING)) {
                    int lootingLevel = weapon.getEnchantmentLevel(Enchantment.LOOTING);

                    chance *= (lootingLevel * 0.10) + 1;
                }
            }

            double finalChance = chance;
            drops.removeIf(item -> {
                if (item.getType() == Material.TOTEM_OF_UNDYING) {
                    return Math.random() > finalChance;
                }
                return false;
            });
        }
    }
}
