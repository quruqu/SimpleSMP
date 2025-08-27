package me.ujun.simplesmp.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ConfigHandler {
    private final JavaPlugin plugin;
    private static ConfigHandler instance;

    public static String fightMessage;
    public static String fightEndMessage;
    public static int fightTime;
    public static int explosionDamagePercent;
    public static List<String> fightBlockCommands = new ArrayList<>();
    public static int xpBottleDiamondRate;
    public static int netherrackDespawnTime;
    public static boolean onlyShowDeathLocationInSameWorld;
    public static boolean showDeathLocation;



    public ConfigHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static ConfigHandler getInstance() {
        return instance;
    }

    public static void init(JavaPlugin plugin) {
        instance = new ConfigHandler(plugin);
        instance.loadConfig();
    }


    public void loadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        fightMessage = config.getString("fight_message", "%sec% sec remain fighting mode!");
        fightEndMessage = config.getString("fight_end_message", "fight end!");
        fightTime = config.getInt("fight_time", 15);
        fightBlockCommands = config.getStringList("blocked-commands-during-fight");
        explosionDamagePercent = config.getInt("explosion_damage_percent");
        xpBottleDiamondRate = config.getInt("xpbottle_diamond_rate");
        netherrackDespawnTime = config.getInt("netherrack_despawn_time");
        onlyShowDeathLocationInSameWorld = config.getBoolean("only_show_death_location_in_same_world");
        showDeathLocation = config.getBoolean("show_death_location");

    }
}
