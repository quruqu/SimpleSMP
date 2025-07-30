package me.ujun.simplesmp;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleSMP extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("bed").setExecutor(new Commands(this));
        Bukkit.getPluginManager().registerEvents(new Listener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }



}
