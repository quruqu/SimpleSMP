package me.ujun.simplesmp.listener;

import me.ujun.simplesmp.SimpleSMP;
import me.ujun.simplesmp.config.ConfigHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RandomSpawnListener implements org.bukkit.event.Listener {

    private final Random random = new Random();
    private final Map<UUID, Boolean> noSpawnPoints = new HashMap<>();


    @EventHandler
    private void deathEvent(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Location bedLocation = player.getBedSpawnLocation();

        if (bedLocation == null) {
            Location spawnLocation = SimpleSMP.playerSpawnLocations.get(player.getUniqueId());

            player.setBedSpawnLocation(spawnLocation, true);
            noSpawnPoints.put(player.getUniqueId(), true);
        }


        if (!ConfigHandler.showDeathLocation) {
            return;
        }

        Location deathLocation = player.getLocation();


        int x = (int) deathLocation.getX();
        int y = (int) deathLocation.getY();
        int z = (int) deathLocation.getZ();


        if (ConfigHandler.onlyShowDeathLocationInSameWorld) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getWorld().equals(player.getWorld())) {
                    onlinePlayer.sendMessage(ChatColor.GOLD + player.getName() + "님의 " + ChatColor.RED + "사망 위치:" + ChatColor.GOLD + " X: " + x + " Y: " + y + " Z: " + z);
                }
            }
        } else {
            Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + "님의 " + ChatColor.RED + "사망 위치:" + ChatColor.GOLD + " X: " + x + " Y: " + y + " Z: " + z + " (" + player.getWorld().getName() +  ")");
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

        if (!SimpleSMP.playerSpawnLocations.containsKey(player.getUniqueId())) {
            Location spawnLocation = randomLocation();
            player.teleport(spawnLocation);
            SimpleSMP.playerSpawnLocations.put(player.getUniqueId(), spawnLocation);

        }
    }

    public Location randomLocation() {
        World world = Bukkit.getWorld("world");

        while (true) {

            int x = random.nextInt(4001) - 2000; // -1000 ~ +1000
            int z = random.nextInt(4001) - 2000; // -1000 ~ +1000
            int y = world.getHighestBlockYAt(x, z); // 지형 높이 위로 설정

            if (world.getHighestBlockAt(x, z).getType() == Material.LAVA) {
                continue;
            }

            return new Location(world, x + 0.5, y + 1, z + 0.5);
        }

    }

}
