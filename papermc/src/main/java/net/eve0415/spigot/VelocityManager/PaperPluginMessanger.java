package net.eve0415.spigot.VelocityManager;

import java.util.Optional;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public final class PaperPluginMessanger implements PluginMessageListener {
    private static final String CHANNEL = "velocitymanager:message";
    private final VelocityManagerPlugin instance;

    public PaperPluginMessanger(final VelocityManagerPlugin instance) {
        this.instance = instance;

        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, CHANNEL, this);
        instance.getServer().getMessenger().registerOutgoingPluginChannel(instance, CHANNEL);
    }

    public void sendOutgoingMessage(final ByteArrayDataOutput out, final Player player) {
        player.sendPluginMessage(instance, CHANNEL, out.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(final String channel, final Player player, final byte[] bytes) {
        final ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();

        final String subChannel = in.readUTF();

        if (subChannel.equalsIgnoreCase("nearbyPlayer")) {
            final String server = in.readUTF();
            final Optional<Player> nearest = player.getNearbyEntities(1000, 1000, 1000).stream()
                    .filter(e -> e instanceof Player).map(e -> ((Player) e)).findFirst();

            if (nearest.isPresent()) {
                out.writeUTF("connect");
                out.writeUTF(server);
                sendOutgoingMessage(out, nearest.get());
            } else {
                out.writeUTF("error");
                out.writeUTF("Cannot find nearby player");
                sendOutgoingMessage(out, player);
            }
        } else if (subChannel.equalsIgnoreCase("status")) {
            instance.getManager().handleMessage(in.readInt(), in.readUTF(), in.readUTF());
        }
    }
}
