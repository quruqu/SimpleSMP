package me.ujun.simplesmp.listener;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

public class ToggleGlideListener implements Listener {

    @EventHandler
    public void onGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player p)) {
            return;
        }

        // 엔드 차원 체크
        if (p.getWorld().getEnvironment() == World.Environment.THE_END) {
            if (event.isGliding()) {
                event.setCancelled(true);
                p.sendActionBar (ChatColor.RED + "엔드에서는 겉날개를 사용할 수 없습니다!");
            }
        }
}
