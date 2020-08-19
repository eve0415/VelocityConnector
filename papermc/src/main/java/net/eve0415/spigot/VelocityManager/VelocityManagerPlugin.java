package net.eve0415.spigot.VelocityManager;

import org.bukkit.plugin.java.JavaPlugin;

public class VelocityManagerPlugin extends JavaPlugin {
    public static VelocityManagerPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("WebsocketIntegration enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("WebsocketIntegration disabled");
    }
}
