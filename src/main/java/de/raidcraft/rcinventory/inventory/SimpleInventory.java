package de.raidcraft.rcinventory.inventory;

import de.raidcraft.rcinventory.holder.InventoryHolder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class SimpleInventory implements Inventory {

    private final long creationMillis;
    private final InventoryHolder holder;
    private final ItemStack[] playerInventoryContents;
    private final ItemStack[] enderChestContents;
    private final String world;

    public SimpleInventory(long creationMillis,
                           InventoryHolder holder,
                           ItemStack[] playerInventoryContents,
                           ItemStack[] enderChestContents) {
        this.creationMillis = creationMillis;
        this.holder = holder;
        this.playerInventoryContents = playerInventoryContents;
        this.enderChestContents = enderChestContents;
        this.world = holder.getLocation().getWorld().getName();
    }

    @Override
    public String serialize() {
        // This is not useful and just for testing purpose
        return String.valueOf(playerInventoryContents.length);
    }

    @Override
    public String enderChestSerialize() {
        // This is not useful and just for testing purpose
        return String.valueOf(enderChestContents.length);
    }
}
