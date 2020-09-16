package net.eve0415.spigot.VelocityManager;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;

@Plugin(id = "velocitymanager", name = "VelocityManager", version = "1.0-SNAPSHOT", description = "Manager for Velocity", authors = "eve0415")
public final class VelocityManager {
    public final ProxyServer server;
    public final Logger logger;
    public VelocityPluginMessenger messenger;

    @Inject
    public VelocityManager(final ProxyServer server, final Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInit(final ProxyInitializeEvent e) {
        this.server.getCommandManager().register(this.server.getCommandManager().metaBuilder("server").build(),
                new ServerCommand(this));
        this.messenger = new VelocityPluginMessenger(this);

        logger.info("VelocityManager enabled");
    }
}
