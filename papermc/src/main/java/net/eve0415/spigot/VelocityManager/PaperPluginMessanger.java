package net.eve0415.spigot.VelocityManager;

import java.util.Optional;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class PaperPluginMessanger implements PluginMessageListener {
    private static final String CHANNEL = "velocitymanager:message";
    private final VelocityManagerPlugin instance;

    public PaperPluginMessanger(VelocityManagerPlugin instance) {
        this.instance = instance;

        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, CHANNEL, this);
        instance.getServer().getMessenger().registerOutgoingPluginChannel(instance, CHANNEL);
    }

    public void sendOutgoingMessage(ByteArrayDataOutput out, Player player) {
        player.sendPluginMessage(instance, CHANNEL, out.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        String subChannel = in.readUTF();

        if (subChannel.equalsIgnoreCase("nearbyPlayer")) {
            String server = in.readUTF();
            Optional<Player> nearest = player.getNearbyEntities(1000, 1000, 1000).stream()
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
        }
    }
}