package me.ujun.simplesmp.listener;

import me.ujun.simplesmp.DisplayGroup;
import me.ujun.simplesmp.SimpleSMP;
import me.ujun.simplesmp.config.ConfigHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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

import java.util.*;

import static me.ujun.simplesmp.SimpleSMP.playerDisplayGroup;
import static me.ujun.simplesmp.SimpleSMP.pvpPlayerTimer;

public class PvpListener implements Listener {

//    private final JavaPlugin plugin;
    Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    Map<UUID, Integer> playerExp = new HashMap<>();
    Map<UUID, Integer> playerLevel = new HashMap<>();
    Map<Player, BossBar> playerBars = new HashMap<>();
    Map<Player, Integer> playerTicks = new HashMap<>();


    public PvpListener(JavaPlugin plugin) {

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (UUID uuid : new HashSet<>(pvpPlayerTimer.keySet())) {

                int sec = pvpPlayerTimer.get(uuid);
                Player player = Bukkit.getPlayer(uuid);
                String fightMessage = ConfigHandler.fightMessage;
                fightMessage = fightMessage.replace("%sec%", String.valueOf(sec));

                if (sec == 0) {
                    if (player != null) {
                        removePersonalBar(player);
                        player.sendActionBar(Component.text(ConfigHandler.fightEndMessage));
                    } else {
                        PvpListener.removePlayerDisplay(uuid);
                    }
                    pvpPlayerTimer.remove(uuid);
                    continue;
                } else {
                    if (player != null) {
                        setBar(player, fightMessage, (double) (sec) / (ConfigHandler.fightTime));
                    }

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

                    if (!offlinePlayer.isOnline()) {
                        DisplayGroup displayGroup = playerDisplayGroup.get(uuid);
                        displayGroup.itemDisplay.setCustomName("§l" + offlinePlayer.getName() + ": " + fightMessage);
                    }
                }

                pvpPlayerTimer.put(uuid, sec -1);

            }

        }, 0L, 20L);
    }

    public static void removePlayerDisplay(UUID playerId) {

        DisplayGroup group = playerDisplayGroup.get(playerId);

        if (group.itemDisplay != null && !group.itemDisplay.isDead()) {
            group.itemDisplay.remove();
        }
        if (group.interaction != null && !group.interaction.isDead()) {
            group.interaction.remove();
        }
        if (group.armorStand != null && !group.armorStand.isDead()) {
            group.armorStand.remove();
        }

        playerDisplayGroup.remove(playerId);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (pvpPlayerTimer.containsKey(player.getUniqueId())) {
            removePersonalBar(player);

            savedInventories.put(player.getUniqueId(), player.getInventory().getContents());
            playerExp.put(player.getUniqueId(), player.getTotalExperience());
            playerLevel.put(player.getUniqueId(), player.getLevel());

            spawnPlayerDisplay(player);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (pvpPlayerTimer.containsKey(player.getUniqueId())) {
            String fightMessage = ConfigHandler.fightMessage;
            fightMessage = fightMessage.replace("%sec%", String.valueOf(pvpPlayerTimer.get(player.getUniqueId())));

            setBar(player, fightMessage, (double) pvpPlayerTimer.get(player.getUniqueId()) / (ConfigHandler.fightTime));
        }

        if (playerDisplayGroup.containsKey(player.getUniqueId())) {
            DisplayGroup displayGroup = playerDisplayGroup.get(player.getUniqueId());

            Location loc = displayGroup.armorStand.getLocation();
            player.teleport(loc);
        }

        if (SimpleSMP.loggedOutDeadPlayers.contains(player.getUniqueId())) {
            Bukkit.broadcast(Component.text("전투 도중 나간 플레이어가 사망 처리되었습니다!").color(NamedTextColor.RED));
            player.getInventory().clear();
            player.setExp(0);
            player.setLevel(0);
            player.setHealth(0);
            SimpleSMP.loggedOutDeadPlayers.remove(player.getUniqueId());
        }

        if (playerDisplayGroup.containsKey(player.getUniqueId())) {
            removePlayerDisplay(player.getUniqueId());
        }


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
        Location base = player.getLocation().setRotation(player.getYaw() + 180, 0);
        UUID playerId = player.getUniqueId();
        String fightMessage = ConfigHandler.fightMessage;
        fightMessage = fightMessage.replace("%sec%", String.valueOf(pvpPlayerTimer.get(playerId)));

        // 머리
        ItemDisplay itemDisplay = (ItemDisplay) base.getWorld().spawnEntity(base.clone().add(0, 0.5, 0), EntityType.ITEM_DISPLAY);
        itemDisplay.setCustomName("§l" + player.getName() + ": " + fightMessage);
        itemDisplay.setCustomNameVisible(true);
        itemDisplay.setGlowing(true);
        itemDisplay.setItemStack(getPlayerHead(playerId));

        // 인터랙션
        Interaction interaction = base.getWorld().spawn(base.clone(), Interaction.class, i -> {
            i.setInteractionWidth(0.8F);
            i.setInteractionHeight(-0.8F);
            i.setResponsive(true);
        });

        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(base.clone(), EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(true);
        stand.setSilent(true);
        stand.setInvulnerable(true);
        Objects.requireNonNull(stand.getAttribute(Attribute.SCALE)).setBaseValue(0.255F);

        stand.addPassenger(itemDisplay);
        stand.addPassenger(interaction);

        // 그룹으로 묶어 등록
        DisplayGroup group = new DisplayGroup();
        group.itemDisplay = itemDisplay;
        group.interaction = interaction;
        group.armorStand = stand;

        SimpleSMP.interactionToPlayerMap.put(interaction.getUniqueId(), playerId);
        playerDisplayGroup.put(player.getUniqueId(), group);
    }

    @EventHandler
    public void onInteract(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Interaction interaction)) return;

        if (!SimpleSMP.interactionToPlayerMap.containsKey(interaction.getUniqueId())) {
            return;
        }

        UUID targetId = SimpleSMP.interactionToPlayerMap.get(interaction.getUniqueId());
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);

        dropOfflinePlayerInventory(targetId, interaction.getLocation());
        pvpPlayerTimer.remove(targetId);
//        pvpPlayerTimer.remove(event.getDamager().getUniqueId());
        removePlayerDisplay(targetId);
        removeAttackerTimer(targetId);

        int droppedExp = Math.min(7 * playerLevel.get(targetId), playerExp.get(targetId));
        ExperienceOrb orb = interaction.getLocation().getWorld().spawn(interaction.getLocation(), ExperienceOrb.class);
        orb.setExperience(droppedExp);

        interaction.getWorld().playSound(interaction.getLocation(), Sound.ENTITY_PLAYER_DEATH, SoundCategory.PLAYERS, 1.0f, 1.0f);
        SimpleSMP.loggedOutDeadPlayers.add(targetId);
        Bukkit.broadcast(Component.text(target.getName() + "님이 전투 도중 퇴장해 사망했습니다.").color(NamedTextColor.RED));

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


    @EventHandler(ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {

        Player attacker;
        Projectile projectile;


        if (event.getDamager() instanceof Projectile) {
            projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            } else {
                return;
            }
        } else if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else {
            return;
        }
        if (!(event.getEntity() instanceof Player damaged)) return;


        UUID attackerId = attacker.getUniqueId();

        UUID damagedId = damaged.getUniqueId();

        if (attacker.equals(damaged)) return;


        pvpPlayerTimer.put(attackerId, ConfigHandler.fightTime);
        pvpPlayerTimer.put(damagedId, ConfigHandler.fightTime);

        SimpleSMP.lastAttackers.computeIfAbsent(damagedId, k -> new HashSet<>()).add(attackerId);


        String fightMessage = ConfigHandler.fightMessage;
        fightMessage = fightMessage.replace("%sec%", String.valueOf(ConfigHandler.fightTime));

//        attacker.sendActionBar(Component.text(fightMessage));
//        damaged.sendActionBar(Component.text(fightMessage));
        setBar(attacker, fightMessage, 1.0);
        setBar(damaged, fightMessage, 1.0);


    }

    public void setBar(Player player, String title, double progress) {
        if (playerBars.containsKey(player)) {
            BossBar bar = playerBars.get(player);

            bar.setTitle(title);
            bar.setProgress(progress);
            return;
        }

        BossBar bar = Bukkit.createBossBar(title,
                BarColor.RED,
                BarStyle.SOLID
        );
        bar.addPlayer(player);
        bar.setProgress(1.0);
        bar.setVisible(true);
        playerBars.put(player, bar);
    }

    public void removePersonalBar(Player player) {
//        player.sendMessage("바 삭제");
        BossBar bar = playerBars.remove(player);
        if (bar != null) {
            bar.removeAll();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getPlayer();

        pvpPlayerTimer.remove(dead.getUniqueId());
        removeAttackerTimer(dead.getUniqueId());
        removePersonalBar(dead);
    }

    private void removeAttackerTimer(UUID deadID) {
        if (SimpleSMP.lastAttackers.containsKey(deadID)) {
            for (UUID uuid : new ArrayList<>(SimpleSMP.lastAttackers.get(deadID))) {
                if (pvpPlayerTimer.containsKey(uuid)) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        removePersonalBar(player);
                        player.sendActionBar(Component.text(ConfigHandler.fightEndMessage));
                    }
                }
                pvpPlayerTimer.remove(uuid);
            }
            SimpleSMP.lastAttackers.remove(deadID);
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (!pvpPlayerTimer.containsKey(player.getUniqueId())) {
            return;
        }
        for (String blocked : ConfigHandler.fightBlockCommands) {
            if (command.startsWith(blocked.toLowerCase())) {
                player.sendMessage(Component.text("PVP 중에는 이 명령어를 사용할 수 없습니다!").color(NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }
        }

    }
}
