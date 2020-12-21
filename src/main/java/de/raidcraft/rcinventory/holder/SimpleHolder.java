package de.raidcraft.rcinventory.holder;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Getter
public class SimpleHolder implements InventoryHolder {

    private UUID uuid;
    private ItemStack[] contents = null;
    private Location location;
    private float exp;
    private float saturation;
    private int level;
    private double health;

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
