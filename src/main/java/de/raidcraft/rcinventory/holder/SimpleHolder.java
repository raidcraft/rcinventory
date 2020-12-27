package de.raidcraft.rcinventory.holder;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Getter
public class SimpleHolder implements InventoryHolder {

    private final UUID uuid;
    private final ItemStack[] playerInventoryContents = null;
    private final ItemStack[] enderChestContents = null;
    private final Location location;
    private final float exp;
    private final float saturation;
    private final int level;
    private final double health;

    public SimpleHolder(UUID uuid, String world, float exp, int level, float saturation, double health) {
        this.uuid = uuid;
        this.location = new Location(Bukkit.getWorld(world), 0, 0, 0);
        this.exp = exp;
        this.level = level;
        this.saturation = saturation;
        this.health = health;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }
}
