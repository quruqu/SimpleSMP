package me.ujun.simplesmp.command;

import me.ujun.simplesmp.SimpleSMP;
import me.ujun.simplesmp.listener.RandomSpawnListener;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SMPCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private RandomSpawnListener randomSpawnListener = new RandomSpawnListener();

    public SMPCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }


        if (command.getName().equals("bed")) {
            Location bedLocation = player.getBedSpawnLocation();


            if (bedLocation == null) {
                player.sendMessage("침대가 설정되어 있지 않습니다!");
                return true;
            }

            Location initialLocation = player.getLocation();

            player.sendMessage("§63초 후 이동합니다!");

            new BukkitRunnable() {
                @Override
                public void run() {
                    // 3초 후에도 같은 위치에 있으면 텔레포트
                    if (player.isOnline() && player.getLocation().distance(initialLocation) < 0.1) {
                        player.teleport(bedLocation);
                        player.sendMessage("§a침대 위치로 이동했습니다!");
                    } else {
                        player.sendMessage("§c이동했기 때문에 텔레포트가 취소되었습니다.");
                    }
                }
            }.runTaskLater(plugin, 60L);

            return true;
        } else if (command.getName().equals("randomspawn")) {
            Location newSpawnPoint = randomSpawnListener.randomLocation();

            SimpleSMP.playerSpawnLocations.put(player.getUniqueId(), newSpawnPoint);
            player.sendMessage("§6스폰포인트를 재생성했습니다!");
        }

        return false;
    }
}
