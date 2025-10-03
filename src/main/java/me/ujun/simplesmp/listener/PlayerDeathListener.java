package me.ujun.simplesmp.listener;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;

import java.util.*;


public class PlayerDeathListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ItemStack[] contents = player.getInventory().getContents();


        if (player.getKiller() != null) {
            return;
        }

        List<Map.Entry<ItemStack, Integer>> items = new ArrayList<>();

        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() == Material.DRAGON_EGG) continue;

            int weight = getItemWeight(item);
            if (weight > 0) {
                items.add(new AbstractMap.SimpleEntry<>(item, weight));
            }
        }

        if (items.isEmpty()) return;


        items.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<Map.Entry<ItemStack, Integer>> top = items.subList(0, Math.min(3, items.size()));
        Map.Entry<ItemStack, Integer> chosen = top.get(random.nextInt(top.size()));
        ItemStack target = chosen.getKey();


        int amount = target.getAmount();
        int removeAmount = (amount > 1) ? random.nextInt(1, amount) : 1;

        player.sendMessage(Component.text("아이템").color(NamedTextColor.RED).hoverEvent(target.asHoverEvent()).append(Component.text("이 소실되었습니다 (")).append(Component.translatable(target.getType())).append(Component.text(String.format(" x%d)", removeAmount))));

        if (amount - removeAmount > 0) {
            target.setAmount(amount - removeAmount);
        } else {
            Inventory inv = player.getInventory();

            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item == null) {
                    continue;
                }

                if (item.equals(target)) {
                    inv.setItem(i, null);
                    return;
                }
            }
        }
    }


    private int getItemWeight(ItemStack item) {
        Material type = item.getType();
        ItemRarity rarity = item.getData(DataComponentTypes.RARITY);

        if (rarity == null) {
            rarity = ItemRarity.COMMON;
        }

        int weight = 0;


        // 장비 재질 기반 + 겉날개 + 삼지창
        switch (type) {
            case NETHERITE_SWORD, NETHERITE_AXE, NETHERITE_PICKAXE, NETHERITE_HELMET, NETHERITE_CHESTPLATE,
                 NETHERITE_LEGGINGS, NETHERITE_BOOTS:
                rarity = ItemRarity.COMMON;
                weight += 45;
                break;
            case DIAMOND_SWORD, DIAMOND_AXE, DIAMOND_PICKAXE, DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS,
                 DIAMOND_BOOTS, TRIDENT:
                rarity = ItemRarity.COMMON;
                weight += 30;
                break;
            case IRON_SWORD, IRON_AXE, IRON_PICKAXE, IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS:
                rarity = ItemRarity.COMMON;
                weight += 15;
                break;
            case GOLDEN_APPLE, TURTLE_SCUTE, ANCIENT_DEBRIS, NETHERITE_INGOT:
                rarity = ItemRarity.RARE;
                break;
            case ENDER_EYE, ENDER_PEARL, BLAZE_POWDER, BLAZE_ROD, DIAMOND:
                rarity = ItemRarity.UNCOMMON;
                break;
        }

        //희귀도 기반 (위의 장비는 반영 안됨)
        switch (rarity) {
            case EPIC -> weight += 50;
            case RARE -> weight += 20;
            case UNCOMMON -> weight += 10;
        }


        // 인첸트 개수와 레벨 반영
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            Map<Enchantment, Integer> enchants = item.getItemMeta().getEnchants();
            for (int lvl : enchants.values()) {
                weight += lvl;
            }
        }
        Bukkit.broadcast(Component.text(item.getType() + ": " + weight + " | " + rarity + " | " + rarity));

        return weight;
    }
}
