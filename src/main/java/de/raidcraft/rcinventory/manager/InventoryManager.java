package de.raidcraft.rcinventory.manager;

import de.raidcraft.rcinventory.RCInventory;
import de.raidcraft.rcinventory.api.holder.InventoryHolder;
import de.raidcraft.rcinventory.api.inventory.Base64Inventory;
import de.raidcraft.rcinventory.api.inventory.Inventory;
import de.raidcraft.rcinventory.database.TDatabaseInventory;
import org.bukkit.entity.Player;

import java.io.IOException;

public class InventoryManager {

    RCInventory plugin;

    public InventoryManager(RCInventory plugin) {
        this.plugin = plugin;
    }

    public void savePlayerInventory(Player player) {

        Inventory inventory;
        try {
            inventory = new Base64Inventory((InventoryHolder)player);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to serialize '" + player.getDisplayName()
                    + "' inventory: " + e.getMessage());
            return;
        }

        // Save inventory to database
        TDatabaseInventory databaseInventory = new TDatabaseInventory(inventory);
        databaseInventory.save();
        plugin.getLogger().info("Synced inventory of '" + player.getDisplayName() + "' into database");
    }

    public void restorePlayerInventory(Player player) {

        TDatabaseInventory databaseInventory = TDatabaseInventory.getLatest(player.getUniqueId());
        if(databaseInventory == null) {
            plugin.getLogger().info("No inventory to restore found for '" + player.getDisplayName() + "'");
            return;
        }

        Inventory inventory;
        try {
            inventory = databaseInventory.asInventory();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to deserialize '" + player.getDisplayName() + "' inventory");
            return;
        }

        // Restore everything
        //-------------------
        player.getInventory().setContents(inventory.getContents());
        player.setSaturation(inventory.getHolder().getSaturation());
        player.setLevel(inventory.getHolder().getLevel());
        player.setExp(inventory.getHolder().getExp());
    }
}