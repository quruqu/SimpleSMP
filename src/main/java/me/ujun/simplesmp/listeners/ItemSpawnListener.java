package me.ujun.simplesmp.listeners;

import me.ujun.simplesmp.config.ConfigHandler;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

import java.net.http.WebSocket;

public class ItemSpawnListener implements Listener {
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();

        if (item.getItemStack().getType() == Material.NETHERRACK) {
            // 100 ticks = 5초
            item.setTicksLived(6000 - ConfigHandler.netherrackDespawnTime);
        }
    }
}
