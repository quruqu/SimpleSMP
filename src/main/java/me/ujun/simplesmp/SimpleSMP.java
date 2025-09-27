package me.ujun.simplesmp;

import me.ujun.simplesmp.commands.SMPCommand;
import me.ujun.simplesmp.commands.XpCommand;
import me.ujun.simplesmp.config.ConfigHandler;
import me.ujun.simplesmp.listener.ExplosionListener;
import me.ujun.simplesmp.listener.ItemSpawnListener;
import me.ujun.simplesmp.listener.PvpListener;
import me.ujun.simplesmp.listener.RandomSpawnListener;
import me.ujun.simplesmp.savings.DataFile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
        Bukkit.getPluginManager().registerEvents(new RandomSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new PvpListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ExplosionListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemSpawnListener(), this);



         run();
    }

    @Override
    public void onDisable() {
        dataFile.saveData(playerSpawnLocations, loggedOutDeadPlayers);
        for (UUID id : new ArrayList<>(playerDisplayGroup.keySet())) {
            Bukkit.getLogger().info(id + " 삭제함");
            PvpListener.removePlayerDisplay(id);
        }
    }


    private void run() {
        new BukkitRunnable() {
            @Override
            public void run() {

                Iterator<UUID> iterator = pvpPlayerTimer.keySet().iterator();
                while (iterator.hasNext()) {
                    UUID uuid = iterator.next();

                    int sec = pvpPlayerTimer.get(uuid);
                    Player player = Bukkit.getPlayer(uuid);
                    String fightMessage = ConfigHandler.fightMessage;
                    fightMessage = fightMessage.replace("%sec%", String.valueOf(sec));

                    if (sec == 0) {
                        if (player != null) {
                            player.sendActionBar(ConfigHandler.fightEndMessage);
                        } else {
                            PvpListener.removePlayerDisplay(uuid);
                        }
                        iterator.remove();
                        continue;
                    } else {
                        if (player != null) {
                            player.sendActionBar(fightMessage);
                        }

                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

                        if (!offlinePlayer.isOnline()) {
                            DisplayGroup displayGroup = playerDisplayGroup.get(uuid);
                            displayGroup.itemDisplay.setCustomName("§l" + offlinePlayer.getName() + ": " + fightMessage);
                        }
                    }

                    pvpPlayerTimer.put(uuid, sec-1);
                }
            }
        }.runTaskTimer(this, 0L, 20L);


        // todo
        // 겉날개 엔드에서 비활성화: config에서 true,false
        // 죽으면 일정 확률로 템 잃게
        // 토템 너프
    }

}
