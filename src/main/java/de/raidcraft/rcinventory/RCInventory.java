package de.raidcraft.rcinventory;

import co.aikar.commands.PaperCommandManager;
import de.raidcraft.rcinventory.commands.AdminCommands;
import de.raidcraft.rcinventory.commands.PlayerCommands;
import de.raidcraft.rcinventory.database.TDatabaseInventory;
import de.raidcraft.rcinventory.listener.PlayerJoinListener;
import de.raidcraft.rcinventory.listener.PlayerLeaveListener;
import de.raidcraft.rcinventory.manager.InventoryManager;
import io.ebean.Database;
import kr.entree.spigradle.annotations.PluginMain;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.Config;
import net.silthus.ebean.EbeanWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

@PluginMain
public class RCInventory extends JavaPlugin {

    @Getter
    @Accessors(fluent = true)
    private static RCInventory instance;

    private Database database;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private PluginConfig pluginConfig;

    private PaperCommandManager commandManager;

    @Getter
    private InventoryManager inventoryManager;

    @Getter
    private static boolean testing = false;

    public RCInventory() {
        instance = this;
    }

    public RCInventory(
            JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        instance = this;
        testing = true;
    }

    @Override
    public void onEnable() {

        loadConfig();
        setupDatabase();
        if (!testing) {
            setupListener();
            setupCommands();
        }

        inventoryManager = new InventoryManager(this);
    }

    public void reload() {

        loadConfig();
    }

    @Override
    public void onDisable() {

        inventoryManager.saveAllPlayersInventories();
    }

    private void loadConfig() {

        getDataFolder().mkdirs();
        pluginConfig = new PluginConfig(new File(getDataFolder(), "config.yml").toPath());
        pluginConfig.loadAndSave();
    }

    private void setupListener() {

        PlayerJoinListener playerJoinListener = new PlayerJoinListener(this);
        Bukkit.getPluginManager().registerEvents(playerJoinListener, this);

        PlayerLeaveListener playerLeaveListener = new PlayerLeaveListener(this);
        Bukkit.getPluginManager().registerEvents(playerLeaveListener, this);
    }

    private void setupCommands() {

        this.commandManager = new PaperCommandManager(this);

        commandManager.registerCommand(new AdminCommands(this));
        commandManager.registerCommand(new PlayerCommands(this));
    }

    private void setupDatabase() {

        this.database = new EbeanWrapper(Config.builder(this)
                .entities(
                        TDatabaseInventory.class
                )
                .build()).connect();
    }
}
