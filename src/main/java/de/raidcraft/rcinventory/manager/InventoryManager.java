package de.raidcraft.rcinventory.manager;

import de.raidcraft.rcinventory.Messages;
import de.raidcraft.rcinventory.PluginConfig;
import de.raidcraft.rcinventory.RCInventory;
import de.raidcraft.rcinventory.holder.PlayerHolder;
import de.raidcraft.rcinventory.inventory.Base64Inventory;
import de.raidcraft.rcinventory.inventory.Inventory;
import de.raidcraft.rcinventory.database.TDatabaseInventory;
import de.raidcraft.rcinventory.util.SchedulerUtil;
import io.ebean.annotation.Transactional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.*;

public class InventoryManager {

    private final static int SAVE_TASK_PROCESS_INTERVAL_MS = 60*1000;
    private RCInventory plugin;
    private BukkitTask saveTask = null;
    private Map<UUID, Inventory> cachedInventories = new HashMap<>();
    private Map<UUID, Map<String, TDatabaseInventory>> preloadedInventories= new HashMap<>();

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
        cachedInventories.put(player.getUniqueId(), inventory); // Update cache
        plugin.getLogger().info("Saved inventory of '" + player.getDisplayName() + "' into database");
    }

    public void preloadAllLatestPlayerInventories(String playerName, UUID playerId) {

        // Initialize players cache entry
        if(!preloadedInventories.containsKey(playerId)) {
            preloadedInventories.put(playerId, new HashMap<>());
        }

        Set<TDatabaseInventory> databaseInventories = TDatabaseInventory.getLatestOfAllWorlds(playerId);
        Map<String, TDatabaseInventory> preloadCache = preloadedInventories.get(playerId);

        databaseInventories.forEach(entry -> {
            // Make sure inventory deserialization is run here in async context
            entry.getInventory();
            preloadCache.put(entry.getWorld(), entry);
        });
    }

    private TDatabaseInventory getLatestPrecachedInventory(UUID playerId, List<String> allowedWorlds) {

        if(!preloadedInventories.containsKey(playerId)) {
            return null;
        }

        TDatabaseInventory foundInventory = null;
        Map<String, TDatabaseInventory> preloadCache = preloadedInventories.get(playerId);

        // Search for newest entry part of partner worlds
        for(Map.Entry<String, TDatabaseInventory> entry : preloadCache.entrySet()) {
            if(allowedWorlds != null && !allowedWorlds.contains(entry.getKey())) {
                continue;
            }

            if(foundInventory == null || foundInventory.getCreationMillis() < entry.getValue().getCreationMillis()) {
                foundInventory = entry.getValue();
            }
        }

        return foundInventory;
    }

    public void restorePlayerInventory(Player player) {

        TDatabaseInventory databaseInventory;
        // Check if there is a world configuration for current players location
        String currentWorld = player.getLocation().getWorld().getName();
        PluginConfig.WorldConfig worldConfig = plugin.getPluginConfig().getWorldConfig(currentWorld);
        if(worldConfig == null) {
            // Create default world config
            worldConfig = new PluginConfig.WorldConfig();
            worldConfig.addPartnerWorld(player.getWorld().getName());
        }

        // We only look for saved inventories on partner worlds

        // Check preload cache
        if(preloadedInventories.containsKey(player.getUniqueId())) {
            databaseInventory = getLatestPrecachedInventory(player.getUniqueId(), worldConfig.getPartnerWorlds());
        } else {
            databaseInventory = TDatabaseInventory.getLatest(player.getUniqueId(), worldConfig.getPartnerWorlds());
        }

        // Cleanup preload cache
        preloadedInventories.remove(player.getUniqueId());

        if(databaseInventory == null) {
            plugin.getLogger().info("No inventory to restore found for '" + player.getDisplayName() + "'");
            return;
        }

        Inventory inventory = databaseInventory.getInventory();

        cachedInventories.put(player.getUniqueId(), inventory); // Update cache

        // Restore everything
        //-------------------
        if(worldConfig.isSyncInventory()) {
            player.getInventory().setContents(inventory.getPlayerInventoryContents());
        }
        if(worldConfig.isSyncEnderChest()) {
            player.getEnderChest().setContents(inventory.getEnderChestContents());
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

    public void removeFromCache(UUID playerId) {

        cachedInventories.remove(playerId);
    }

    @Transactional
    public void cleanup() {

        // Get all old inventories from DB and delete them
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
