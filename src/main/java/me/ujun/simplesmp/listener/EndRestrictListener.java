package me.ujun.simplesmp.listener;

import me.ujun.simplesmp.SimpleSMP;
import me.ujun.simplesmp.config.ConfigHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.*;

public class EndRestrictListener implements Listener {

    public EndRestrictListener(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentHour = getCurrentHour();

            if (isWithinRange(currentHour)) {
                return;
            }

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
                    Location loc = p.getBedSpawnLocation();

                    if (loc == null) {
                        loc = SimpleSMP.playerSpawnLocations.get(p.getUniqueId());
                    }

                    p.sendMessage(ChatColor.RED + "엔드가 비활성화되어 강제퇴장됩니다");
                    p.teleport(loc);
                }
            }
        }, 0, 20L);
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        int currentHour = getCurrentHour();

        if (isWithinRange(currentHour)) {
            return;
        }


        int leftHour = -1;
        if (currentHour < ConfigHandler.endDimensionOpenTime) {
            leftHour += ConfigHandler.endDimensionOpenTime - currentHour;
        } else if (currentHour > ConfigHandler.endDimensionOpenTime) {
            leftHour += ConfigHandler.endDimensionOpenTime - currentHour + 168;
        } else {
            leftHour = 0;
        }
        Component leftHourComponent = Component.text(String.format("%d일 %d시간", leftHour/24, leftHour%24));

        if (leftHour == 0) {
            LocalTime now = LocalTime.now();
            int minute = now.getMinute();
            leftHourComponent = leftHourComponent.append(Component.text(String.format(" %d분", 60 - minute)));
        }
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("엔드 활성화까지 남은 시간: ")
                    .append(leftHourComponent.color(NamedTextColor.RED))
            );
        }
    }

    int getCurrentHour() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        int dayIndex = now.getDayOfWeek().getValue() % 7;
        return (dayIndex - 1) * 24 + now.getHour();
    }

    boolean isWithinRange(int current) {
        if (ConfigHandler.endDimensionOpenTime == ConfigHandler.endDimensionCloseTime) {
            return true;
        }

        if (ConfigHandler.endDimensionOpenTime < ConfigHandler.endDimensionCloseTime) {
            return current >= ConfigHandler.endDimensionOpenTime && current < ConfigHandler.endDimensionCloseTime;
        } else {
            return current >= ConfigHandler.endDimensionOpenTime || current < ConfigHandler.endDimensionCloseTime;
        }
    }
}
