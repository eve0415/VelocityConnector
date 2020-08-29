package net.eve0415.spigot.VelocityManager;

import org.bukkit.plugin.java.JavaPlugin;

import net.eve0415.spigot.VelocityManager.sign.manager;

public final class VelocityManagerPlugin extends JavaPlugin {
    public static VelocityManagerPlugin instance;
    public PaperPluginMessanger messenger;
    public manager manager;

    @Override
    public void onEnable() {
        instance = this;
        this.messenger = new PaperPluginMessanger(this);
        this.manager = new manager(this);
        new eventHandler(this);
        getLogger().info("VelocityManager enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("VelocityManager disabled");
    }
}
