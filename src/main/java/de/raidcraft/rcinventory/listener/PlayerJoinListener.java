package de.raidcraft.rcinventory.listener;

import de.raidcraft.rcinventory.RCInventory;
import de.raidcraft.rcinventory.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Delayed;

public class PlayerJoinListener implements Listener, Runnable {

    private RCInventory plugin;
    private Map<UUID, Long> restoreCache = new HashMap<>();

    public PlayerJoinListener(RCInventory plugin) {

        this.plugin = plugin;

        // Start task which checks every second
        // if some player stuck due to unknown
        // circumstances in restore map.
        // These players are immortal otherwise.
        Bukkit.getScheduler().runTaskTimer(plugin, this, 20, 10);
    }

    @EventHandler
    public void onPlayerAsyncPreJoin(AsyncPlayerPreLoginEvent event) {

        plugin.getInventoryManager().preloadAllLatestPlayerInventories(
                event.getPlayerProfile().getName(), event.getPlayerProfile().getId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {

        // To avoid problems by setting inventory right after
        // login event we will delay this for one tick.
        restoreCache.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        DelayedPlayerRestoreTask task = new DelayedPlayerRestoreTask(event.getPlayer());
        Bukkit.getScheduler().runTaskLater(plugin, task,
                SchedulerUtil.msInTicks(plugin.getPluginConfig().getRestoreDelayMs()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player)event.getEntity();
        if(!restoreCache.containsKey(player.getUniqueId())) {
            return;
        }

        // Cancel event due to ongoing restore process
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!restoreCache.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        // Cancel event due to ongoing restore process
        event.setCancelled(true);
    }

    private class DelayedPlayerRestoreTask implements Runnable {

        private Player player;

        public DelayedPlayerRestoreTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            if(restoreCache.containsKey(player.getUniqueId())) {
                plugin.getInventoryManager().restorePlayerInventory(player);
                restoreCache.remove(player.getUniqueId());
            }
        }
    }

    @Override
    public void run() {
        for (Map.Entry<UUID, Long> entry : restoreCache.entrySet()) {
            long timeGone = System.currentTimeMillis() - entry.getValue();
            if(timeGone > 500 + plugin.getPluginConfig().getRestoreDelayMs()) {

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
