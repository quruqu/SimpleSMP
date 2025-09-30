package me.ujun.simplesmp.command;

import me.ujun.simplesmp.config.ConfigHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public ReloadCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
       plugin.reloadConfig();
       ConfigHandler.getInstance().loadConfig();
       sender.sendMessage(ChatColor.GREEN + "reloaded config!");
       return true;
    }
}
