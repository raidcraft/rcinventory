package de.raidcraft.rcinventory.inventory;

import de.raidcraft.rcinventory.holder.InventoryHolder;
import de.raidcraft.rcinventory.util.BukkitSerialization;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

@Getter
public class Base64Inventory implements Inventory {

    private final InventoryHolder holder;
    String world;
    private final long creationMillis;
    String serializedInventory;
    String serializedEnderChestInventory;
    ItemStack[] playerInventoryContents;
    ItemStack[] enderChestContents;

    public Base64Inventory(InventoryHolder holder) throws IOException {
        this.holder = holder;
        this.world = holder.getLocation().getWorld().getName();
        this.creationMillis = System.currentTimeMillis();

        this.serializedInventory = BukkitSerialization.itemStackArrayToBase64(holder.getPlayerInventoryContents());
        this.serializedEnderChestInventory = BukkitSerialization.itemStackArrayToBase64(holder.getEnderChestContents());

        // We will deserialize instead of copying the holders inventory
        // to make sure we got the content which was actually saved
        playerInventoryContents = BukkitSerialization.itemStackArrayFromBase64(this.serializedInventory);
        enderChestContents = BukkitSerialization.itemStackArrayFromBase64(this.serializedEnderChestInventory);
    }

    public Base64Inventory(InventoryHolder holder, String serializedPlayerInventory,
                           String serializedEnderChestInventory, long creationMillis) throws IOException {

        this.holder = holder;
        this.creationMillis = creationMillis;

        this.serializedInventory = serializedPlayerInventory;
        this.serializedEnderChestInventory = serializedEnderChestInventory;

        playerInventoryContents = BukkitSerialization.itemStackArrayFromBase64(this.serializedInventory);
        enderChestContents = BukkitSerialization.itemStackArrayFromBase64(this.serializedEnderChestInventory);
    }

    @Override
    public String serialize() {
        return serializedInventory;
    }

    @Override
    public String enderChestSerialize() {
        return serializedEnderChestInventory;
    }
}
