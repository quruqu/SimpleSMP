package me.ujun.simplesmp.config;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigHandler {
    private final JavaPlugin plugin;
    private static ConfigHandler instance;

    Map<String, World.Environment> envMap = Map.of(
            "normal", World.Environment.NORMAL,
            "nether", World.Environment.NETHER,
            "end", World.Environment.THE_END
    );

    public static String fightMessage;
    public static String fightEndMessage;
    public static int fightTime;
    public static int explosionDamagePercent;
    public static List<String> fightBlockCommands = new ArrayList<>();
    public static int xpBottleDiamondRate;
    public static int netherrackDespawnTime;
    public static boolean onlyShowDeathLocationInSameWorld;
    public static boolean showDeathLocation;
    public static List<World.Environment> elytraDisableDimension = new ArrayList<>();
    public static int elytraDamageDuringFight;
    public static int maceCooldownPerDamage;
    public static double totemDropChance;



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
        fightBlockCommands = config.getStringList("blocked_commands_during_fight");
        explosionDamagePercent = config.getInt("explosion_damage_percent");
        xpBottleDiamondRate = config.getInt("xpbottle_diamond_rate");
        netherrackDespawnTime = config.getInt("netherrack_despawn_time");
        onlyShowDeathLocationInSameWorld = config.getBoolean("only_show_death_location_in_same_world");
        showDeathLocation = config.getBoolean("show_death_location");
        List<String> keyList = Objects.requireNonNull(config.getStringList("elytra_disable_dimension"));
        for (String key : keyList) {
            World.Environment env = envMap.get(key);
            if (env != null) {
                elytraDisableDimension.add(env);
            }
        }

        elytraDamageDuringFight = config.getInt("elytra_damage_during_fight");
        maceCooldownPerDamage = config.getInt("mace_cooldown_per_damage");
        totemDropChance = config.getDouble("totem_drop_chance");

    }
}
