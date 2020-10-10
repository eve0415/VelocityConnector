package net.eve0415.spigot.VelocityManager.Sign;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import net.eve0415.spigot.VelocityManager.VelocityManagerPlugin;
import net.md_5.bungee.api.ChatColor;

public final class SignManager {
    private final VelocityManagerPlugin instance;
    private final ArrayList<SignHandler> signs = new ArrayList<>();

    public SignManager(final VelocityManagerPlugin instance) {
        this.instance = instance;
    }

    public void newSign(final Player player, final Location location, final String name) {
        final SignHandler newSign = new SignHandler(location, player.getWorld().getUID(), name, this);
        signs.add(newSign);

        refreshStatus(newSign, player);
    }

    public void refreshStatus(final SignHandler sign, final Player player) {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("status");
        out.writeInt(sign.getIdentificator());
        out.writeUTF(sign.getName());
        instance.getMessenger().sendOutgoingMessage(out, player);
    }

    public SignHandler checkSign(final Location location) {
        for (final SignHandler sign : signs) {
            if (sign.getLocation().equals(location))
                return sign;
        }
        return null;
    }

    public ArrayList<SignHandler> checkSign(final String name) {
        final ArrayList<SignHandler> applicableSigns = new ArrayList<>();
        for (final SignHandler sign : signs) {
            if (sign.getName().equals(name))
                applicableSigns.add(sign);
        }

        if (applicableSigns.isEmpty()) {
            return null;
        } else {
            return applicableSigns;
        }
    }

    public void handleMessage(final int code, final String name, final String state) {
        final ArrayList<SignHandler> si = checkSign(name);

        if (si == null)
            return;
        for (final SignHandler s : si) {
            if (s.getBlock().getState() instanceof Sign) {
                if (state.equals("online")) {
                    s.setState(SignStatus.ONLINE);
                } else if (state.equals("offline")) {
                    s.setState(SignStatus.OFFLINE);
                } else {
                    remove(s);
                    return;
                }
                updateSign((Sign) s.getBlock().getState(), s);
            } else {
                remove(s);
            }
        }
    }

    private void updateSign(final Sign sign, final SignHandler s) {
        if (s.getState() == SignStatus.OFFLINE) {
            sign.setLine(0, ChatColor.RED + "[" + ChatColor.BOLD + s.getName() + ChatColor.RESET + ChatColor.RED + "]");
            sign.setLine(2, "OFFLINE");
        }
        if (s.getState() == SignStatus.ONLINE) {
            sign.setLine(0,
                    ChatColor.GREEN + "[" + ChatColor.BOLD + s.getName() + ChatColor.RESET + ChatColor.GREEN + "]");
            sign.setLine(2, "ONLINE");
        }

        sign.setLine(1, "");
        sign.setLine(3, "");
        sign.update(true);
    }

    public void remove(final SignHandler sign) {
        signs.remove(sign);
    }

    public VelocityManagerPlugin getInstance() {
        return instance;
    }
}
