package de.raidcraft.template.commands;

import co.aikar.commands.BaseCommand;
import de.raidcraft.template.PluginTemplate;

public class PlayerCommands extends BaseCommand {

    private final PluginTemplate plugin;

    public PlayerCommands(PluginTemplate plugin) {
        this.plugin = plugin;
    }
}
