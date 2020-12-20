package de.raidcraft.template.commands;

import co.aikar.commands.BaseCommand;
import de.raidcraft.template.PluginTemplate;

public class AdminCommands extends BaseCommand {

    private final PluginTemplate plugin;

    public AdminCommands(PluginTemplate plugin) {
        this.plugin = plugin;
    }
}
