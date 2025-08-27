package me.ujun.simplesmp.savings;

import net.kyori.adventure.text.BlockNBTComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataFile {
    private final File dataFile;
    private final FileConfiguration dataConfig;


    public DataFile(File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.dataFile = new File(dataFolder, "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }



    public void loadData(HashMap<UUID, Location> playerSpawnLocations, Set<UUID> loggedOutDeadPlayers) {
        if (!dataConfig.isConfigurationSection("player_spawns")) return;


        ConfigurationSection section1 = dataConfig.getConfigurationSection("player_spawns");
        for (String uuidStr : section1.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            String path = "player_spawns." + uuidStr;

            World world = Bukkit.getWorld(dataConfig.getString(path + ".world"));
            double x = dataConfig.getDouble(path + ".x");
            double y = dataConfig.getDouble(path + ".y");
            double z = dataConfig.getDouble(path + ".z");
            float yaw = (float) dataConfig.getDouble(path + ".yaw");
            float pitch = (float) dataConfig.getDouble(path + ".pitch");

            if (world != null) {
                playerSpawnLocations.put(uuid, new Location(world, x, y, z, yaw, pitch));
                Bukkit.getLogger().info("Config file location: " + dataFile.getAbsolutePath());
            }
        }

        ConfigurationSection section2 = dataConfig.getConfigurationSection("logout_players");

        if (section2 != null) {
            for (String key : section2.getKeys(false)) {
                String uuidStr = section2.getString(key);
                if (uuidStr != null) {
                    loggedOutDeadPlayers.add(UUID.fromString(uuidStr));
                }
            }
        }
    }

    public void saveData(HashMap<UUID, Location> playerSpawnLocations, Set<UUID> loggedOutDeadPlayers) {

        
        for (Map.Entry<UUID, Location> entry : playerSpawnLocations.entrySet()) {
            UUID uuid = entry.getKey();
            Location loc = entry.getValue();

            String path = "player_spawns." + uuid.toString();
            dataConfig.set(path + ".world", loc.getWorld().getName());
            dataConfig.set(path + ".x", loc.getX());
            dataConfig.set(path + ".y", loc.getY());
            dataConfig.set(path + ".z", loc.getZ());
            dataConfig.set(path + ".yaw", loc.getYaw());
            dataConfig.set(path + ".pitch", loc.getPitch());

            Bukkit.getLogger().info("asdf");
            Bukkit.getLogger().info("Config file location: " + dataFile.getAbsolutePath());
        }

        dataConfig.set("logout_players", null);
        int i = 0;
        for (UUID uuid : loggedOutDeadPlayers) {
            dataConfig.set("logout_players." + i, uuid.toString());
            i++;
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
