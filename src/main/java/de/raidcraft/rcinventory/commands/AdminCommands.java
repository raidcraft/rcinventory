package de.raidcraft.rcinventory.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import de.raidcraft.rcinventory.RCInventory;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("rcinventory")
public class AdminCommands extends BaseCommand {

    private final RCInventory plugin;

    public AdminCommands(RCInventory plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reload")
    @CommandPermission("rcinventory.reload")
    public void reload(CommandSender sender) {

        plugin.reload();
        sender.sendMessage("RCInventory reload successful!");
    }
}
