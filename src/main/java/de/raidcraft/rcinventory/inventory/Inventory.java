package de.raidcraft.rcinventory.inventory;

import de.raidcraft.rcinventory.holder.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface Inventory {

    long getCreationMillis();

    InventoryHolder getHolder();

    ItemStack[] getContents();

    String getWorld();

    String serialize();
}
