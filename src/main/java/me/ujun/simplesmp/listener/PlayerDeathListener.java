package me.ujun.simplesmp.listener;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;

import java.util.*;


public class PlayerDeathListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ItemStack[] contents = player.getInventory().getContents();

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


        if (target.getAmount() > 1) {
            target.setAmount(target.getAmount() - 1);
        } else {
            player.getInventory().removeItem(target);
        }
        player.sendMessage(Component.translatable(target.getType().translationKey())
                .hoverEvent(target.asHoverEvent())
                .append(Component.text("가 소실되었습니다")));
    }


    private int getItemWeight(ItemStack item) {
        Material type = item.getType();
         ItemRarity rarity = item.getData(DataComponentTypes.RARITY);
        int weight = 0;

        // 장비 재질 기반 + 겉날개
        switch (type) {
            case NETHERITE_SWORD, NETHERITE_AXE, NETHERITE_PICKAXE,
                 NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS:
                rarity = ItemRarity.COMMON;
                weight += 45;
                break;
            case DIAMOND_SWORD, DIAMOND_AXE, DIAMOND_PICKAXE,
                 DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS:
                rarity = ItemRarity.COMMON;
                weight += 30;
                break;
            case IRON_SWORD, IRON_AXE, IRON_PICKAXE,
                 IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS:
                rarity = ItemRarity.COMMON;
                weight += 15;
                break;
            case ELYTRA:
                rarity = ItemRarity.COMMON;
                weight += 40;
                break;
        }

        //희귀도 기반 (위의 장비는 반영 안됨)
        switch (rarity) {
            case EPIC -> weight += 50;
            case RARE -> weight+= 20;
            case UNCOMMON -> weight += 10;
            case COMMON -> weight += 5;
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
