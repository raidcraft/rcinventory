package de.raidcraft.rcinventory.manager;

import de.raidcraft.rcinventory.Messages;
import de.raidcraft.rcinventory.RCInventory;
import de.raidcraft.rcinventory.holder.InventoryHolder;
import de.raidcraft.rcinventory.holder.PlayerHolder;
import de.raidcraft.rcinventory.inventory.Base64Inventory;
import de.raidcraft.rcinventory.inventory.Inventory;
import de.raidcraft.rcinventory.database.TDatabaseInventory;
import de.raidcraft.rcinventory.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.*;

public class InventoryManager {

    private final static int SAVE_TASK_PROCESS_INTERVAL_MS = 60*1000;
    private RCInventory plugin;
    private BukkitTask saveTask = null;

    public InventoryManager(RCInventory plugin) {

        this.plugin = plugin;
        setupSaveTask();
    }

    public void setupSaveTask() {

        if(saveTask != null) {
            Bukkit.getScheduler().cancelTask(saveTask.getTaskId());
            saveTask = null;
        }

        PeriodicSaveTask task = new PeriodicSaveTask();
        saveTask = Bukkit.getScheduler().runTaskTimer(plugin, task,
                SchedulerUtil.msInTicks(SAVE_TASK_PROCESS_INTERVAL_MS),
                SchedulerUtil.msInTicks(SAVE_TASK_PROCESS_INTERVAL_MS));
    }

    public void saveAllPlayersInventories() {

        Bukkit.getOnlinePlayers().forEach(player -> savePlayerInventory(player));
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
            if(plugin.getPluginConfig().isLogSkippedSaves()) {
                plugin.getLogger().info("Inventory of '" + player.getDisplayName() + "' already up-to-date");
            }
            return;
        }
        databaseInventory.save();
        plugin.getLogger().info("Saved inventory of '" + player.getDisplayName() + "' into database");
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
        if(plugin.getPluginConfig().isLogSuccessfulLoads()) {
            plugin.getLogger().info("Restored inventory of '" + player.getDisplayName() + "' from database");
        }
    }

    public void cleanup() {

        // Get all inventories from DB and delete them
        Set<UUID> storedHolders = TDatabaseInventory.getStoredHolders();

        storedHolders.forEach(holder -> {
            List<TDatabaseInventory> inventories = TDatabaseInventory.getInventoriesOrderedByDate(holder);
            if(inventories.size() < plugin.getPluginConfig().getPlayerInventoryBackupCount()) {
                return;
            }

            int count = 0;
            for(TDatabaseInventory inventory : inventories) {
                count++;

                if(count > plugin.getPluginConfig().getPlayerInventoryBackupCount()) {
                    inventory.delete();
                }
            }
        });
    }

    private class PeriodicSaveTask implements Runnable {

        private Map<UUID, Long> lastSaves = new HashMap<>();

        @Override
        public void run() {

            Bukkit.getOnlinePlayers().forEach(player -> {

                // Check if last save was recently
                if(lastSaves.containsKey(player.getUniqueId())) {
                    long timeGone = System.currentTimeMillis() - lastSaves.get(player.getUniqueId());
                    if(timeGone < SchedulerUtil.minInMs(plugin.getPluginConfig().getSaveIntervalMin())) {
                        return;
                    }
                }

                savePlayerInventory(player);
                lastSaves.put(player.getUniqueId(), System.currentTimeMillis());
            });
        }
    }
}
