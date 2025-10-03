package me.ujun.simplesmp.listener;

import me.ujun.simplesmp.config.ConfigHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class MaceHitListener implements Listener {


    @EventHandler(ignoreCancelled = true)
    public void onMaceUse(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;

        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.MACE) {
            if (p.hasCooldown(Material.MACE)) {
                e.setCancelled(true);
                return;
            }

            int damage = (int) e.getDamage();
//            p.sendMessage(String.valueOf(damage));
            p.setCooldown(Material.MACE, ConfigHandler.maceCooldownPerDamage * damage);
        }
    }
}
