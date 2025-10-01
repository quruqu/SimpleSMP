package me.ujun.simplesmp.command;

import me.ujun.simplesmp.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class XpCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (command.getName().equals("xpbottle")) {
            // 입력값 파싱
            int diamondsToUse = 1;
            if (args.length >= 1) {
                try {
                    diamondsToUse = Integer.parseInt(args[0]);
                    if (diamondsToUse <= 0) {
                        p.sendMessage(ChatColor.RED + "1 이상의 숫자를 입력하세요.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "숫자를 입력하세요.");
                    return true;
                }
            }

            // 다이아몬드 보유량 확인
            if (!p.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), diamondsToUse)) {
                p.sendMessage(ChatColor.RED + "다이아몬드가 부족합니다.");
                return true;
            }

            p.getInventory().removeItem(new ItemStack(Material.DIAMOND, diamondsToUse));

            ItemStack bottles = new ItemStack(Material.EXPERIENCE_BOTTLE, diamondsToUse * ConfigHandler.xpBottleDiamondRate);
            Map<Integer, ItemStack> leftover = p.getInventory().addItem(bottles);

            // 인벤토리 꽉 찼을 경우 바닥에 드롭
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), item);
                }
                p.sendMessage(ChatColor.YELLOW + "인벤토리가 부족하여 일부 아이템이 바닥에 떨어졌습니다.");
            }

            p.sendMessage(ChatColor.GREEN + "다이아몬드 " + diamondsToUse + "개를 경험치 병으로 교환했습니다!");
            return true;
        }
        return false;
    }
}
