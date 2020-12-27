package de.raidcraft.rcinventory.holder;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerHolder implements InventoryHolder {

    Player player;

    public PlayerHolder(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }

    @Override
    public float getSaturation() {
        return player.getSaturation();
    }

    @Override
    public int getLevel() {
        return player.getLevel();
    }

    @Override
    public float getExp() {
        return player.getExp();
    }

    @Override
    public ItemStack[] getPlayerInventoryContents() {
        return player.getInventory().getContents();
    }

    @Override
    public ItemStack[] getEnderChestContents() {
        return player.getEnderChest().getContents();
    }

    @Override
    public double getHealth() {
        return player.getHealth();
    }
}
