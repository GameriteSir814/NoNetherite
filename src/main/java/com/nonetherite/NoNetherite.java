package com.nonetherite;

import org.bukkit.plugin.java.JavaPlugin;

public class NoNetherite extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new NoNetheriteListener(this), this);
        getLogger().info("NoNetherite enabled - netherite is fully blocked.");
    }
}
