package de.raidcraft.rcinventory.commands;

import co.aikar.commands.BaseCommand;
import de.raidcraft.rcinventory.RCInventory;

public class PlayerCommands extends BaseCommand {

    private final RCInventory plugin;

    public PlayerCommands(RCInventory plugin) {
        this.plugin = plugin;
    }
}
