package me.ujun.simplesmp.listener;

import me.ujun.simplesmp.SimpleSMP;
import me.ujun.simplesmp.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.Buffer;
import java.util.UUID;

public class GlideListener implements Listener {
    private final JavaPlugin plugin;


    public GlideListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        Player p = e.getPlayer();
        if (p.isGliding() && SimpleSMP.pvpPlayerTimer.containsKey(p.getUniqueId())) {
            ItemStack item = e.getItem();

            if (item.getType() == Material.ELYTRA) {
                int durability = getDurability(item);
                int damage = ConfigHandler.elytraDamageDuringFight;

                if (durability == 1) {
                    e.setCancelled(true);
                    return;
                }

                if (item.containsEnchantment(Enchantment.UNBREAKING)) {
                    int level = item.getEnchantmentLevel(Enchantment.UNBREAKING);
                    damage *= level;
                }



                if (durability - damage > 0) {
                    e.setDamage(damage);
                } else {
                    e.setCancelled(true);
                    item.setDurability((short) 431);
                }
            }
        }
    }

    int getDurability(ItemStack item) {
        int durability = -1;

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
           durability = item.getType().getMaxDurability() - damageable.getDamage();
        }

        return durability;
    }


    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent e) {
        if (!(e.getEntity() instanceof Player p)) {
            return;
        }

        if (ConfigHandler.elytraDisableDimension.contains(p.getWorld().getEnvironment())) {
            if (e.isGliding()) {
                e.setCancelled(true);
                p.sendActionBar("§c해당 차원에서는 겉날개를 사용할 수 없습니다");
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();

        if (!p.getInventory().contains(Material.ELYTRA)) {
           return;
        }

        if (ConfigHandler.elytraDisableDimension.contains(p.getWorld().getEnvironment())) {
            p.sendMessage("§c§l[주의] §f§l겉날개를 사용할 수 없는 차원입니다");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickupElytra(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (p.getInventory().contains(Material.ELYTRA)) return;
        if (p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType() == Material.ELYTRA) return;


        ItemStack item = event.getItem().getItemStack();
        if (item.getType() == Material.ELYTRA) {
            if (ConfigHandler.elytraDisableDimension.contains(p.getWorld().getEnvironment())) {
                p.sendMessage("§c§l[주의] §f§l겉날개를 사용할 수 없는 차원입니다");
            }
        }
    }
}
