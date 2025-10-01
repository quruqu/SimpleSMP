package me.ujun.simplesmp.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class EnderChestRestrictListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.ENDER_CHEST) {
            ItemStack cursor = event.getCursor();
            ItemStack current = event.getCurrentItem();

            if (isRestricted(cursor.getType()) || (current != null && isRestricted(current.getType()))) {
                event.getWhoClicked().sendMessage(ChatColor.RED + "엔더 상자 보관이 제한된 아이템입니다");
                event.setCancelled(true);
            }
        }
    }

    private boolean isRestricted(Material type) {
        if (type == null) {
            return false;
        }

        if (type == Material.DRAGON_EGG) return true;

        return (Tag.SHULKER_BOXES.isTagged(type));

    }
}