package net.eve0415.spigot.VelocityManager;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class VelocityPluginMessenger {
    private final VelocityManager instance;
    private static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create("velocitymanager", "message");

    public VelocityPluginMessenger(final VelocityManager instance) {
        this.instance = instance;
        instance.getServer().getChannelRegistrar().register(CHANNEL);
        instance.getServer().getEventManager().register(instance, this);
    }

    public void sendOutgoingMessage(@NonNull final ByteArrayDataOutput out, final Player player) {
        final Optional<ServerConnection> server = player.getCurrentServer();
        server.get().sendPluginMessage(CHANNEL, out.toByteArray());
    }

    @Subscribe
    public void onPluginMessage(final PluginMessageEvent e) {
        final ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
        final String subChannel = in.readUTF();
        final ServerConnection connection = (ServerConnection) e.getSource();

        if (subChannel.equalsIgnoreCase("connect")) {
            final Optional<RegisteredServer> toConnect = instance.getServer().getServer(in.readUTF());
            connection.getPlayer().createConnectionRequest(toConnect.get()).fireAndForget();
        } else if (subChannel.equalsIgnoreCase("error")) {
            connection.getPlayer().sendMessage(Component.text(in.readUTF(), NamedTextColor.RED));
        } else if (subChannel.equalsIgnoreCase("status")) {
            final int code = in.readInt();
            final String name = in.readUTF();

            final ByteArrayDataOutput out = ByteStreams.newDataOutput();
            final Optional<RegisteredServer> server = instance.getServer().getServer(name);

            out.writeUTF("status");
            out.writeInt(code);
            out.writeUTF(name);

            if (server.isPresent()) {
                try {
                    server.get().ping().get(1000, TimeUnit.MILLISECONDS);
                    out.writeUTF("online");
                } catch (InterruptedException | ExecutionException | TimeoutException e1) {
                    out.writeUTF("offline");
                }
            } else {
                out.writeUTF("unknown");
            }
            sendOutgoingMessage(out, connection.getPlayer());
        }
    }
}
