package de.raidcraft.rcinventory.listener;

import de.raidcraft.rcinventory.RCInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Delayed;

public class PlayerListener implements Listener, Runnable {

    private RCInventory plugin;
    private Map<UUID, Long> restoreCache = new HashMap<>();

    public PlayerListener(RCInventory plugin) {

        this.plugin = plugin;

        // Start task which checks every second
        // if some player stuck due to unknown
        // circumstances in restore map.
        // These players are immortal otherwise.
        Bukkit.getScheduler().runTaskTimer(plugin, this, 20, 10);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {

        // To avoid problems by setting inventory right after
        // login event we will delay this for one tick.
        restoreCache.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        DelayedPlayerRestoreTask task = new DelayedPlayerRestoreTask(event.getPlayer());
        Bukkit.getScheduler().runTaskLater(plugin, task, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeft(PlayerQuitEvent event) {

        plugin.getInventoryManager().savePlayerInventory(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKicked(PlayerKickEvent event) {

        plugin.getInventoryManager().savePlayerInventory(event.getPlayer());
    }

    private class DelayedPlayerRestoreTask implements Runnable {

        private Player player;

        public DelayedPlayerRestoreTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            plugin.getInventoryManager().restorePlayerInventory(player);
            restoreCache.remove(player.getUniqueId());
        }
    }

    @Override
    public void run() {
        for (Map.Entry<UUID, Long> entry : restoreCache.entrySet()) {
            long timeGone = System.currentTimeMillis() - entry.getValue();
            if(timeGone > 1000) {

                plugin.getLogger().warning("Found dirty entry in listener restore cache.");

                // Manually restore inventory
                Player player = Bukkit.getPlayer(entry.getKey());
                if(player != null) {
                    plugin.getInventoryManager().restorePlayerInventory(player);
                }

                restoreCache.remove(entry.getKey());

                // This is not critical,
                // if there are more entries they will be cleaned up next time
                // Better than handle concurrent list iteration
                return;
            }
        }
    }
}
