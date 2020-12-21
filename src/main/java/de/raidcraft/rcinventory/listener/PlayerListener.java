package de.raidcraft.rcinventory.listener;

import de.raidcraft.rcinventory.RCInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private RCInventory plugin;

    public PlayerListener(RCInventory plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        plugin.getInventoryManager().savePlayerInventory(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeft(PlayerQuitEvent event) {

        plugin.getInventoryManager().restorePlayerInventory(event.getPlayer());
    }
}
