package de.raidcraft.rcinventory.holder;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface InventoryHolder {

    UUID getUniqueId();

    Location getLocation();

    float getSaturation();

    int getLevel();

    float getExp();

    ItemStack[] getContents();
}
