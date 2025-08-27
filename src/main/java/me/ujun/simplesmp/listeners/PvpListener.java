package me.ujun.simplesmp.listeners;

import me.ujun.simplesmp.DisplayGroup;
import me.ujun.simplesmp.SimpleSMP;
import me.ujun.simplesmp.config.ConfigHandler;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.css.CSSImportRule;

import javax.swing.plaf.SplitPaneUI;
import java.util.*;

public class PvpListener implements Listener {

    Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    Map<UUID, Integer> playerExp = new HashMap<>();
    Map<UUID, Integer> playerLevel = new HashMap<>();

    private final JavaPlugin plugin;


    public PvpListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }



    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (SimpleSMP.pvpPlayerTimer.containsKey(player.getUniqueId())) {
           savedInventories.put(player.getUniqueId(), player.getInventory().getContents());
           playerExp.put(player.getUniqueId(), player.getTotalExperience());
           playerLevel.put(player.getUniqueId(), player.getLevel());

           spawnPlayerDisplay(player);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (SimpleSMP.playerDisplayGroup.containsKey(player.getUniqueId())) {
            DisplayGroup displayGroup = SimpleSMP.playerDisplayGroup.get(player.getUniqueId());

            Location loc = displayGroup.armorStand.getLocation();
            player.teleport(loc);
        }

        if (SimpleSMP.loggedOutDeadPlayers.contains(player.getUniqueId())) {
            Bukkit.broadcastMessage(ChatColor.RED + "전투 도중 나간 플레이어가 사망 처리되었습니다!");
            player.getInventory().clear();
            player.setExp(0);
            player.setLevel(0);
            player.setHealth(0);
            SimpleSMP.loggedOutDeadPlayers.remove(player.getUniqueId());
        }

        if (SimpleSMP.playerDisplayGroup.containsKey(player.getUniqueId())) {
            removePlayerDisplay(player.getUniqueId());
        }


    }



    public static void removePlayerDisplay(UUID playerId) {

        DisplayGroup group = SimpleSMP.playerDisplayGroup.get(playerId);

        if (group.itemDisplay != null && !group.itemDisplay.isDead()) {
            group.itemDisplay.remove();
        }
        if (group.interaction != null && !group.interaction.isDead()) {
            group.interaction.remove();
        }
        if (group.armorStand != null && !group.armorStand.isDead()) {
            group.armorStand.remove();
        }

        SimpleSMP.playerDisplayGroup.remove(playerId);
    }


    private ItemStack getPlayerHead(UUID uuid) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(target);
        skull.setItemMeta(meta);
        return skull;
    }


    public void spawnPlayerDisplay(Player player) {
        Location base = player.getLocation().setRotation(player.getYaw()+180, 0);
        UUID playerId = player.getUniqueId();
        String fightMessage = ConfigHandler.fightMessage;
        fightMessage = fightMessage.replace("%sec%", String.valueOf(SimpleSMP.pvpPlayerTimer.get(playerId)));

        // 머리
        ItemDisplay itemDisplay = (ItemDisplay) base.getWorld().spawnEntity(base.clone().add(0, 0.5, 0), EntityType.ITEM_DISPLAY);
        itemDisplay.setCustomName("§l" + player.getName() + ": " + fightMessage);
        itemDisplay.setCustomNameVisible(true);
        itemDisplay.setGlowing(true);
        itemDisplay.setItemStack(getPlayerHead(playerId));

        // 인터랙션
        Interaction interaction = (Interaction) base.getWorld().spawn(base.clone(), Interaction.class, i -> {
            i.setInteractionWidth(0.8F);
            i.setInteractionHeight(-0.8F);
            i.setResponsive(true);
        });

        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(base.clone(), EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(true);
        stand.setSilent(true);
        stand.setInvulnerable(true);
        stand.getAttribute(Attribute.SCALE).setBaseValue(0.255F);

        stand.addPassenger(itemDisplay);
        stand.addPassenger(interaction);

        // 그룹으로 묶어 등록
        DisplayGroup group = new DisplayGroup();
        group.itemDisplay = itemDisplay;
        group.interaction = interaction;
        group.armorStand = stand;

        SimpleSMP.interactionToPlayerMap.put(interaction.getUniqueId(), playerId);
        SimpleSMP.playerDisplayGroup.put(player.getUniqueId(), group);
    }

    @EventHandler
    public void onInteract(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Interaction)) return;

        Interaction interaction = (Interaction) event.getEntity();

        if (!SimpleSMP.interactionToPlayerMap.containsKey(interaction.getUniqueId())) {
            return;
        }

        UUID targetId = SimpleSMP.interactionToPlayerMap.get(interaction.getUniqueId());
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);

        dropOfflinePlayerInventory(targetId, interaction.getLocation());
        SimpleSMP.pvpPlayerTimer.remove(targetId);
        SimpleSMP.pvpPlayerTimer.remove(event.getDamager().getUniqueId());
        removePlayerDisplay(targetId);
        removeAttackerTimer(targetId);

        int droppedExp = (int) Math.min(7 * playerLevel.get(targetId), playerExp.get(targetId));
        ExperienceOrb orb = interaction.getLocation() .getWorld().spawn(interaction.getLocation(), ExperienceOrb.class);
        orb.setExperience(droppedExp);

        interaction.getWorld().playSound(interaction.getLocation(), Sound.ENTITY_PLAYER_DEATH, SoundCategory.PLAYERS, 1.0f, 1.0f);
        SimpleSMP.loggedOutDeadPlayers.add(targetId);
        Bukkit.broadcastMessage(ChatColor.RED + target.getName() + "님이 전투 도중 퇴장해 사망했습니다.");

        event.setCancelled(true);
    }

    public void dropInventoryContents(Location location, ItemStack[] contents) {
        World world = location.getWorld();
        if (world == null) return;

        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) continue;
            world.dropItemNaturally(location, item.clone()); // 복사본 드롭
        }
    }

    public void dropOfflinePlayerInventory(UUID playerId, Location location) {

        ItemStack[] contents = savedInventories.get(playerId);

        dropInventoryContents(location, contents);

    }


    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;

        Player attacker;
        Projectile projectile;


        if (event.getDamager() instanceof Projectile) {
            projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            } else {
                return;
            }
        } else if (event.getDamager() instanceof  Player) {
            attacker = (Player) event.getDamager();
        } else {
            return;
        }
        if (!(event.getEntity() instanceof Player)) return;


        UUID attackerId = attacker.getUniqueId();

        Player damaged = (Player) event.getEntity();
        UUID damagedId = damaged.getUniqueId();

        if (attacker.equals(damaged)) return;


        SimpleSMP.pvpPlayerTimer.put(attackerId, ConfigHandler.fightTime);
        SimpleSMP.pvpPlayerTimer.put(damagedId, ConfigHandler.fightTime);

        SimpleSMP.lastAttackers.computeIfAbsent(damagedId, k -> new HashSet<>()).add(attackerId);


        String fightMessage = ConfigHandler.fightMessage;
        fightMessage = fightMessage.replace("%sec%", String.valueOf(ConfigHandler.fightTime));

        attacker.sendActionBar(fightMessage);
        damaged.sendActionBar(fightMessage);

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getPlayer();

        SimpleSMP.pvpPlayerTimer.remove(dead.getUniqueId());
        removeAttackerTimer(dead.getUniqueId());
    }

    private void removeAttackerTimer(UUID deadID) {
        if (SimpleSMP.lastAttackers.containsKey(deadID)) {
            for (UUID uuid : new ArrayList<>(SimpleSMP.lastAttackers.get(deadID))) {
                if (SimpleSMP.pvpPlayerTimer.containsKey(uuid)) {
                    Player player = Bukkit.getPlayer(uuid);
                    player.sendActionBar(ConfigHandler.fightEndMessage);
                }
                SimpleSMP.pvpPlayerTimer.remove(uuid);
            }

            SimpleSMP.lastAttackers.remove(deadID);
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (!SimpleSMP.pvpPlayerTimer.containsKey(player.getUniqueId())) {
            return;
        }
            for (String blocked :ConfigHandler.fightBlockCommands) {
                if (command.startsWith(blocked.toLowerCase())) {
                    player.sendMessage(ChatColor.RED + "PVP 중에는 이 명령어를 사용할 수 없습니다!");
                    event.setCancelled(true);
                    return;
                }
            }

    }
}
