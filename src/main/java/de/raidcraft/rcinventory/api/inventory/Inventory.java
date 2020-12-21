package de.raidcraft.rcinventory.api.inventory;

import de.raidcraft.rcinventory.api.holder.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface Inventory {

    long getCreationMillis();

    InventoryHolder getHolder();

    ItemStack[] getContents();

    String getWorld();

    String serialize();
}
