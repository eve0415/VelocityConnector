package net.eve0415.spigot.VelocityManager.sign;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;

public final class handler {
    private final manager manager;
    private final Location location;
    private final UUID world;
    private final String name;
    private status stat;
    private final int identificator;
    private final static Random rand = new Random();

    public handler(final Location location, final UUID world, final String name, final manager manager) {
        this.manager = manager;
        this.location = location;
        this.world = world;
        this.name = name;
        this.stat = status.NEW;
        this.identificator = rand.nextInt(1000);
    }

    public void setState(final status state) {
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

    public status getState() {
        return stat;
    }

    public int getIdentificator() {
        return identificator;
    }
}