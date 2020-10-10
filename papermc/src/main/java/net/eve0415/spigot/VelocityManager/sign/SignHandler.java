package net.eve0415.spigot.VelocityManager.Sign;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;

public final class SignHandler {
    private final SignManager manager;
    private final Location location;
    private final UUID world;
    private final String name;
    private SignStatus stat;
    private final int identificator;
    private final static Random rand = new Random();

    public SignHandler(final Location location, final UUID world, final String name, final SignManager manager) {
        this.manager = manager;
        this.location = location;
        this.world = world;
        this.name = name;
        this.stat = SignStatus.NEW;
        this.identificator = rand.nextInt(1000);
    }

    public void setState(final SignStatus state) {
        stat = state;
    }

    public Block getBlock() {
        return this.manager.getInstance().getServer().getWorld(world).getBlockAt(this.location);
    }

    public Location getLocation() {
        return location;
    }

    public UUID getWorld() {
        return world;
    }

    public String getName() {
        return name;
    }

    public SignStatus getState() {
        return stat;
    }

    public int getIdentificator() {
        return identificator;
    }
}
