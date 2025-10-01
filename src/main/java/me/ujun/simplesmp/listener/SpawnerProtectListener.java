package me.ujun.simplesmp.listener;

import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;
import java.util.List;

public class SpawnerProtectListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().isOp()) return;

        Block block = event.getBlock();

        if (block.getType() == Material.SPAWNER) {
            CreatureSpawner spawner = (CreatureSpawner) block.getState();

            if (spawner.getSpawnedType() == org.bukkit.entity.EntityType.BLAZE) {
                event.getPlayer().sendMessage(ChatColor.RED + "블레이즈 스포너는 파괴할 수 없습니다");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        removeBlazeSpawnersFromList(event.blockList());
    }


    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        removeBlazeSpawnersFromList(event.blockList());
    }

    private void removeBlazeSpawnersFromList(List<Block> blocks) {
        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            Block block = it.next();
            if (block.getType() == Material.SPAWNER) {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                if (spawner.getSpawnedType() == EntityType.BLAZE) {
                    it.remove();
                }
            }
        }
    }
}
