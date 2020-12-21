package de.raidcraft.rcinventory.api.holder;

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

    public SimpleHolder(UUID uuid, String world, float exp, int level, float saturation) {
        this.uuid = uuid;
        this.location = new Location(Bukkit.getWorld(world), 0, 0, 0);
        this.exp = exp;
        this.level = level;
        this.saturation = saturation;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }
}
