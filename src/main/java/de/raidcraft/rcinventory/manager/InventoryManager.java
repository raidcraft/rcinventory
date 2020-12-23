package de.raidcraft.rcinventory.manager;

import de.raidcraft.rcinventory.Messages;
import de.raidcraft.rcinventory.PluginConfig;
import de.raidcraft.rcinventory.RCInventory;
import de.raidcraft.rcinventory.holder.InventoryHolder;
import de.raidcraft.rcinventory.holder.PlayerHolder;
import de.raidcraft.rcinventory.inventory.Base64Inventory;
import de.raidcraft.rcinventory.inventory.Inventory;
import de.raidcraft.rcinventory.database.TDatabaseInventory;
import de.raidcraft.rcinventory.util.SchedulerUtil;
import io.ebean.annotation.Transactional;
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

        // Check if there is a world configuration for current players location
        String currentWorld = player.getLocation().getWorld().getName();
        PluginConfig.WorldConfig worldConfig = plugin.getPluginConfig().getWorldConfig(currentWorld);
        TDatabaseInventory databaseInventory;
        if(worldConfig != null) {
            // If world config was found we only look
            // for saved inventories on partner worlds
            databaseInventory = TDatabaseInventory.getLatest(player.getUniqueId(), worldConfig.getPartnerWorlds());
        } else
        {
            // Create default world config
            worldConfig = new PluginConfig.WorldConfig();
            // If there is no world config we accept any saved inventory
            databaseInventory = TDatabaseInventory.getLatest(player.getUniqueId());
        }

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
        if(worldConfig.isSyncInventory()) {
            player.getInventory().setContents(inventory.getContents());
        }
        if(worldConfig.isSyncSaturation()) {
            player.setSaturation(inventory.getHolder().getSaturation());
        }
        if(worldConfig.isSyncExp()) {
            player.setLevel(inventory.getHolder().getLevel());
            player.setExp(inventory.getHolder().getExp());
        }
        if(worldConfig.isSyncHealth()) {
            if (inventory.getHolder().getHealth() > 0) {
                player.setHealth(inventory.getHolder().getHealth());
            }
        }
        if(plugin.getPluginConfig().isRestoreMessage()) {
            Messages.send(player, Messages.inventoryRestored(player, inventory));
        }
        if(plugin.getPluginConfig().isLogSuccessfulLoads()) {
            plugin.getLogger().info("Restored inventory of '" + player.getDisplayName() + "' from database");
        }
    }

    @Transactional
    public void cleanup() {

        // Get all inventories from DB and delete them
        Set<UUID> storedHolders = TDatabaseInventory.getStoredHolders();

        storedHolders.forEach(holder -> {
            List<TDatabaseInventory> inventories = TDatabaseInventory.getInventoriesOrderedByDate(holder);
            if(inventories.size() < plugin.getPluginConfig().getPlayerInventoryBackupCount()) {
                return;
            }

            Map<String, Integer> worldCount = new HashMap<>();
            for(TDatabaseInventory inventory : inventories) {
                if(!worldCount.containsKey(inventory.getWorld())) {
                    worldCount.put(inventory.getWorld(), 0);
                }

                worldCount.put(inventory.getWorld(), worldCount.get(inventory.getWorld()) + 1);

                if(worldCount.get(inventory.getWorld()) > plugin.getPluginConfig().getPlayerInventoryBackupCount()) {
                    inventory.delete();
                }
            }
        });
    }

    private class PeriodicSaveTask implements Runnable {

        private Map<UUID, Long> lastSaves = new HashMap<>();

        @Override
        public void run() {

            // Disabled if configured interval is zero
            if(plugin.getPluginConfig().getSaveIntervalMin() == 0) {
                return;
            }

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
