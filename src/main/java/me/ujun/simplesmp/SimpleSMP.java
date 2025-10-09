package me.ujun.simplesmp;

import me.ujun.simplesmp.command.ReloadCommand;
import me.ujun.simplesmp.command.SMPCommand;
import me.ujun.simplesmp.command.XpCommand;
import me.ujun.simplesmp.config.ConfigHandler;
import me.ujun.simplesmp.listener.*;
import me.ujun.simplesmp.saving.DataFile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalTime;
import java.util.*;


public final class SimpleSMP extends JavaPlugin {
    private DataFile dataFile;
    public static HashMap<UUID, Location> playerSpawnLocations = new HashMap<>();
    public static Map<UUID, DisplayGroup> playerDisplayGroup = new HashMap<>();
    public static HashMap<UUID, Integer> pvpPlayerTimer = new HashMap<>();
    public static HashMap<UUID, Set<UUID>> lastAttackers = new HashMap<>();
    public static HashMap<UUID, UUID> interactionToPlayerMap = new HashMap<>();
    public static Set<UUID> loggedOutDeadPlayers = new HashSet<>();



    @Override
    public void onEnable() {
        dataFile = new DataFile(getDataFolder());
        dataFile.loadData(playerSpawnLocations, loggedOutDeadPlayers);

        saveDefaultConfig();
        ConfigHandler.init(this);

        getCommand("bed").setExecutor(new SMPCommand(this));
        getCommand("randomspawn").setExecutor(new SMPCommand(this));
        getCommand("xpbottle").setExecutor(new XpCommand());
        getCommand("simplesmp-reload").setExecutor(new ReloadCommand(this));
        Bukkit.getPluginManager().registerEvents(new RandomSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new PvpListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ExplosionListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new GlideListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MaceHitListener(), this);
        Bukkit.getPluginManager().registerEvents(new EnderChestRestrictListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new SpawnerProtectListener(), this);
        Bukkit.getPluginManager().registerEvents(new EndRestrictListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DragonEggProtectListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemCraftListener(), this);
    }

    @Override
    public void onDisable() {
        dataFile.saveData(playerSpawnLocations, loggedOutDeadPlayers);
        for (UUID id : new ArrayList<>(playerDisplayGroup.keySet())) {
            Bukkit.getLogger().info(id + " 삭제함");
            PvpListener.removePlayerDisplay(id);
        }
    }
}
