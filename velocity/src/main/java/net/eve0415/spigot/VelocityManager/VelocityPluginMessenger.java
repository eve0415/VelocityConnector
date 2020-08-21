package net.eve0415.spigot.VelocityManager;

import java.util.Optional;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

public class VelocityPluginMessenger {
    private final VelocityManager instance;
    private static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create("velocitymanager", "message");

    public VelocityPluginMessenger(VelocityManager instance) {
        this.instance = instance;
        instance.server.getChannelRegistrar().register(CHANNEL);
        instance.server.getEventManager().register(instance, this);
    }

    public void sendOutgoingMessage(@NonNull ByteArrayDataOutput out, Player player) {
        Optional<ServerConnection> server = player.getCurrentServer();
        server.get().sendPluginMessage(CHANNEL, out.toByteArray());
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {
        ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
        String subChannel = in.readUTF();
        ServerConnection connection = (ServerConnection) e.getSource();

        if (subChannel.equalsIgnoreCase("connect")) {
            Optional<RegisteredServer> toConnect = this.instance.server.getServer(in.readUTF());
            connection.getPlayer().createConnectionRequest(toConnect.get()).fireAndForget();
        } else if (subChannel.equalsIgnoreCase("error")) {
            connection.getPlayer().sendMessage(TextComponent.of(in.readUTF(), TextColor.RED));
        }
    }
}