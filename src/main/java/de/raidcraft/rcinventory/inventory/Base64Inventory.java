package de.raidcraft.rcinventory.inventory;

import de.raidcraft.rcinventory.holder.InventoryHolder;
import de.raidcraft.rcinventory.util.BukkitSerialization;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

@Getter
public class Base64Inventory implements Inventory {

    private InventoryHolder holder;
    String world;
    private long creationMillis;
    String serializedInventory;
    ItemStack[] contents;

    public Base64Inventory(InventoryHolder holder) throws IOException {
        this.holder = holder;
        this.world = holder.getLocation().getWorld().getName();
        this.creationMillis = System.currentTimeMillis();

        this.serializedInventory = BukkitSerialization.itemStackArrayToBase64(holder.getContents());

        // We will deserialize instead of copying the holders inventory
        // to make sure we got the content which was actually saved
        contents = BukkitSerialization.itemStackArrayFromBase64(this.serializedInventory);
    }

    public Base64Inventory(InventoryHolder holder, String serialized, long creationMillis) throws IOException {

        this.holder = holder;
        this.creationMillis = creationMillis;

        this.serializedInventory = serialized;

        contents = BukkitSerialization.itemStackArrayFromBase64(this.serializedInventory);
    }

    @Override
    public String serialize() {
        return serializedInventory;
    }
}
