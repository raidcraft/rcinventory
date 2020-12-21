package de.raidcraft.rcinventory.inventory;

import de.raidcraft.rcinventory.holder.InventoryHolder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class SimpleInventory implements Inventory {

    private long creationMillis;
    private InventoryHolder holder;
    private ItemStack[] contents;
    private String world;

    public SimpleInventory(long creationMillis, InventoryHolder holder, ItemStack[] contents) {
        this.creationMillis = creationMillis;
        this.holder = holder;
        this.contents = contents;
        this.world = holder.getLocation().getWorld().getName();
    }

    @Override
    public String serialize() {
        // This is not useful and just for testing purpose
        return String.valueOf(contents.length);
    }
}
