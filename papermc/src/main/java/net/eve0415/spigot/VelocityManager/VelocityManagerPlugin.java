package net.eve0415.spigot.VelocityManager;

import org.bukkit.plugin.java.JavaPlugin;

import net.eve0415.spigot.VelocityManager.sign.SignManager;

public final class VelocityManagerPlugin extends JavaPlugin {
    private static VelocityManagerPlugin instance;
    private PaperPluginMessanger messenger;
    private SignManager manager;

    @Override
    public void onEnable() {
        getLogger().info("Loading VelocityManager...");
        instance = this;
        this.messenger = new PaperPluginMessanger(this);
        this.manager = new SignManager(this);
        new PaperEventHandler(this);
        getLogger().info("Successfully enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Successfully disabled");
    }

    public static VelocityManagerPlugin getInstance() {
        return instance;
    }

    public PaperPluginMessanger getMessenger() {
        return messenger;
    }

    public SignManager getManager() {
        return manager;
    }
}
