package me.ujun.simplesmp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Listener implements org.bukkit.event.Listener {

    private final Random random = new Random();
    private final Map<UUID, Boolean> noSpawnPoints = new HashMap<>();


    @EventHandler
    private void deathEvent(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Location bedLocation = player.getBedSpawnLocation();

        if (bedLocation == null) {
            player.setBedSpawnLocation(randomLocation(), true);
            noSpawnPoints.put(player.getUniqueId(), true);
        }


        Location deathLocation = player.getLocation();


        int x = (int) deathLocation.getX();
        int y = (int) deathLocation.getY();
        int z = (int) deathLocation.getZ();


        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(ChatColor.GOLD + player.getName() + "님의 " + ChatColor.RED + "사망 위치:" + ChatColor.GOLD + " X: " + x + " Y: " + y + " Z: " + z + " (" + player.getWorld().getName() +  ")");
        }
    }


    @EventHandler
    private void respawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (noSpawnPoints.containsKey(player.getUniqueId())) {
            player.setBedSpawnLocation(null);
            noSpawnPoints.remove(player.getUniqueId());
        }
    }

    @EventHandler
    private void firstJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            player.teleport(randomLocation());
        }
    }

    private Location randomLocation() {
        World world = Bukkit.getWorld("world");

        int x = random.nextInt(4001) - 2000; // -1000 ~ +1000
        int z = random.nextInt(4001) - 2000; // -1000 ~ +1000
        int y = world.getHighestBlockYAt(x, z) + 1; // 지형 높이 위로 설정

        Location location = new Location(world, x + 0.5, y, z + 0.5);

        return location;
    }

}
