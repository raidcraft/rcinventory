package de.raidcraft.rcinventory.listener;

import de.raidcraft.rcinventory.RCInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    private RCInventory plugin;

    public PlayerLeaveListener(RCInventory plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeft(PlayerQuitEvent event) {

        plugin.getInventoryManager().savePlayerInventory(event.getPlayer());
        plugin.getInventoryManager().removeFromCache(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKicked(PlayerKickEvent event) {

        plugin.getInventoryManager().savePlayerInventory(event.getPlayer());
        plugin.getInventoryManager().removeFromCache(event.getPlayer().getUniqueId());
    }
}
