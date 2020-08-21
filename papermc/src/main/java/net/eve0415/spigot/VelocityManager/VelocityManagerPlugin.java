package net.eve0415.spigot.VelocityManager;

import org.bukkit.plugin.java.JavaPlugin;

public class VelocityManagerPlugin extends JavaPlugin {
    public static VelocityManagerPlugin instance;
    public PaperPluginMessanger messenger;

    @Override
    public void onEnable() {
        instance = this;
        this.messenger = new PaperPluginMessanger(this);

        getLogger().info("VelocityManager enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("VelocityManager disabled");
    }
}
