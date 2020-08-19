package net.eve0415.spigot.VelocityManager;

import com.google.common.eventbus.Subscribe;
import com.google.common.io.ByteArrayDataOutput;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import org.checkerframework.checker.nullness.qual.NonNull;

public class PluginMessenger {
    private final VelocityManager instance;
    private static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create("velocitymanager", "update");

    public PluginMessenger(VelocityManager instance) {
        this.instance = instance;
        this.instance.server.getChannelRegistrar().register(CHANNEL);
    }

    public void sendOutgoingMessage(@NonNull ByteArrayDataOutput out) {
        for (RegisteredServer server : this.instance.server.getAllServers()) {
            server.sendPluginMessage(CHANNEL, out.toByteArray());
        }
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {
        this.instance.logger.info(e.toString());
    }
}