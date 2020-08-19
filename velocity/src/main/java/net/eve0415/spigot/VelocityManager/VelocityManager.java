package net.eve0415.spigot.VelocityManager;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;

@Plugin(id = "velocitymanager", name = "VelocityManager", version = "1.0-SNAPSHOT", description = "Manager for Velocity", authors = "eve0415")
public class VelocityManager {
    public final ProxyServer server;
    public final Logger logger;
    public PluginMessenger messenger;

    @Inject
    public VelocityManager(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent e) {
        this.server.getCommandManager().register(new ServerCommand(this), "server");
        this.messenger = new PluginMessenger(this);
        logger.info("VelocityManager enabled");
    }
}
