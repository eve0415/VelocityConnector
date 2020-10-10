package net.eve0415.spigot.VelocityManager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import net.eve0415.spigot.VelocityManager.Sign.SignHandler;
import net.eve0415.spigot.VelocityManager.Sign.SignStatus;

public final class PaperEventHandler implements Listener {
    private final VelocityManagerPlugin instance;

    public PaperEventHandler(final VelocityManagerPlugin instance) {
        this.instance = instance;
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onSignChange(final SignChangeEvent e) {
        if (e.isCancelled())
            return;

        if (!e.getPlayer().hasPermission("velocitymanager.create.signserver"))
            return;

        if (!(ChatColor.stripColor(e.getLine(0)).startsWith("[") && ChatColor.stripColor(e.getLine(0)).endsWith("]")))
            return;

        instance.getManager().newSign(e.getPlayer(), (Location) e.getBlock().getLocation(),
                ChatColor.stripColor(e.getLine(0)).substring(1, (ChatColor.stripColor(e.getLine(0)).length() - 1)));
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        final Block block = e.getPlayer().getTargetBlock(100);

        if (!(block.getState() instanceof Sign))
            return;

        final SignHandler sign = instance.getManager().checkSign(block.getLocation());
        if (sign == null) {
            final Sign state = (Sign) block.getState();
            if (!(ChatColor.stripColor(state.getLine(0)).startsWith("[")
                    && ChatColor.stripColor(state.getLine(0)).endsWith("]")))
                return;

            instance.getManager().newSign(e.getPlayer(), (Location) block.getLocation(), ChatColor
                    .stripColor(state.getLine(0)).substring(1, (ChatColor.stripColor(state.getLine(0)).length() - 1)));
        } else {
            instance.getManager().refreshStatus(sign, e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (!(e.getClickedBlock().getState() instanceof Sign))
            return;

        final SignHandler sign = instance.getManager().checkSign(e.getClickedBlock().getLocation());
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();

        if (sign == null)
            return;
        if (sign.getState() == SignStatus.ONLINE) {
            out.writeUTF("connect");
            out.writeUTF(sign.getName());
            instance.getMessenger().sendOutgoingMessage(out, e.getPlayer());
        }
        if (sign.getState() == SignStatus.OFFLINE) {
            e.getPlayer().sendMessage(ChatColor.RED + "現在、サーバーが起動していないかオフラインのため、接続ができません");
        }
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (!(e.getBlock().getState() instanceof Sign))
            return;

        final SignHandler sign = instance.getManager().checkSign(e.getBlock().getLocation());
        if (sign == null)
            return;

        instance.getManager().remove(sign);
    }
}
