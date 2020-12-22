package de.raidcraft.rcinventory.manager;

import de.raidcraft.rcinventory.Messages;
import de.raidcraft.rcinventory.RCInventory;
import de.raidcraft.rcinventory.holder.InventoryHolder;
import de.raidcraft.rcinventory.holder.PlayerHolder;
import de.raidcraft.rcinventory.inventory.Base64Inventory;
import de.raidcraft.rcinventory.inventory.Inventory;
import de.raidcraft.rcinventory.database.TDatabaseInventory;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;

public class InventoryManager {

    RCInventory plugin;

    public InventoryManager(RCInventory plugin) {
        this.plugin = plugin;
    }

    public void savePlayerInventory(Player player) {

        Inventory inventory;
        PlayerHolder playerHolder = new PlayerHolder(player);
        try {
            inventory = new Base64Inventory(playerHolder);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to serialize '" + player.getDisplayName()
                    + "' inventory: " + e.getMessage());
            return;
        }

        // Get latest saved inventory of this player
        TDatabaseInventory latestInventory = TDatabaseInventory.getLatest(playerHolder.getUniqueId(),
                playerHolder.getLocation().getWorld().getName());

        // Save inventory to database
        TDatabaseInventory databaseInventory = new TDatabaseInventory(inventory);
        if(latestInventory != null && latestInventory.equals(databaseInventory)) {
            plugin.getLogger().info("Inventory of '" + player.getDisplayName() + "' already synced");
            return;
        }
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
        if(inventory.getHolder().getHealth() > 0) {
            player.setHealth(inventory.getHolder().getHealth());
        }
        if(plugin.getPluginConfig().isRestoreMessage()) {
            Messages.send(player, Messages.inventoryRestored(player, inventory));
        }
        plugin.getLogger().info("Restored inventory of '" + player.getDisplayName() + "' from database");
    }
}