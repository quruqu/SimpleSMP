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
        final int[] lastHour = { -1 };
        final int[] lastMinute = { -1 };
        final List<Integer> warningMinute = Arrays.asList(1, 5, 10, 30);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (UUID uuid : new HashSet<>(pvpPlayerTimer.keySet())) {

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
                    pvpPlayerTimer.remove(uuid);
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

                pvpPlayerTimer.put(uuid, sec - 1);
            }


            int currentHour = LocalTime.now().getHour();

            if (currentHour != lastHour[0]) {
                lastHour[0] = currentHour;

                if (currentHour == ConfigHandler.elytraDisabledTimeStart - 1) {
                    Bukkit.broadcast(Component.text("1시간 후 겉날개 사용이 제한됩니다"));
                } else if (currentHour == ConfigHandler.elytraDisabledTimeStart) {
                    Bukkit.broadcast(Component.text("같날개 사용이 제한됩니다"));
                } else if (currentHour == ConfigHandler.elytraDisabledTimeEnd) {
                    Bukkit.broadcast(Component.text("같날개 사용 제한이 해제됩니다"));
                }
            }

            if (currentHour == ConfigHandler.elytraDisabledTimeStart - 1) {
                int currentMinute = LocalTime.now().getMinute();

                if (currentMinute != lastMinute[0]) {
                    lastMinute[0] = currentMinute;

                    int leftMin = 60 - currentMinute;
                    if (warningMinute.contains(leftMin)) {
                        Bukkit.broadcast(Component.text(String.format("%d분 후 겉날개 사용이 제한됩니다.", leftMin)));
                    }
                }
            }
        }, 0L, 20L);



        // todo
        // 엔더 드래곤 버프
        // 죽을 시 확률적으로 아이템 삭제
    }

}
