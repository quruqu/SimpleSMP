package me.ujun.simplesmp.listener;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import me.ujun.simplesmp.config.ConfigHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class DragonEggProtectListener implements Listener {

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        if (item.getItemStack().getType() == Material.DRAGON_EGG) {
            item.setInvulnerable(true);
            item.setGlowing(true);
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (event.getEntity().getItemStack().getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onItemRemove(EntityRemoveFromWorldEvent event) {
        if (!(event.getEntity() instanceof Item item)) return;

        boolean containsDragonEgg = false;
        Material type = item.getItemStack().getType();

        if (type == Material.DRAGON_EGG) containsDragonEgg = true;

        if (Tag.SHULKER_BOXES.isTagged(type)) {
            if (item.getItemStack().getItemMeta() instanceof BlockStateMeta meta) {
                if (meta.getBlockState() instanceof ShulkerBox box) {
                    for (ItemStack inside : box.getInventory().getContents()) {
                        if (inside != null && inside.getType() == Material.DRAGON_EGG) {
                            containsDragonEgg = true;
                        }
                    }
                }
            }
        }

        if (containsDragonEgg) {
            World endWorld = Bukkit.getWorld("world_the_end");
            if (endWorld == null) return;


            Location endCenter = new Location(endWorld, 0, endWorld.getHighestBlockYAt(0, 0) + 1, 0);

            Block block = endCenter.getBlock();
            block.setType(Material.DRAGON_EGG);
            Bukkit.broadcast(Component.text("드래곤 알이 엔드 중심으로 이동하였습니다").color(NamedTextColor.DARK_PURPLE));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        if (p.getInventory().contains(Material.DRAGON_EGG)) {
            for (ItemStack stack : p.getInventory().getContents()) {
                if (stack != null && stack.getType() == Material.DRAGON_EGG) {

                    p.getWorld().dropItem(p.getLocation(), stack.clone());
                    p.getInventory().removeItem(stack);
                }
            }
        }
    }
}

