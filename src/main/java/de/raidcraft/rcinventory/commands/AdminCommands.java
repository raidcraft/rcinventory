package de.raidcraft.rcinventory.commands;

import co.aikar.commands.BaseCommand;
import de.raidcraft.rcinventory.RCInventory;

public class AdminCommands extends BaseCommand {

    private final RCInventory plugin;

    public AdminCommands(RCInventory plugin) {
        this.plugin = plugin;
    }
}
