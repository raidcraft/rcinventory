package de.raidcraft.rcinventory;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.ConfigurationElement;
import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;
import de.exlll.configlib.format.FieldNameFormatters;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@Getter
@Setter
public class PluginConfig extends BukkitYamlConfiguration {

    private DatabaseConfig database = new DatabaseConfig();
    @Comment("Delay in milliseconds between player login and inventory restore." +
            "This should be long enough to overwrite any other plugins which manipulates inventory after login.")
    private int restoreDelayMs = 500;
    @Comment("Writes a chat message to player after inventory was restored")
    private boolean restoreMessage = true;
    @Comment("Interval in minutes to save player inventories periodically. 0 = disabled")
    private int saveIntervalMin = 5;
    @Comment("Enable console messages about skipped inventory save events")
    private boolean logSkippedSaves = true;
    @Comment("Enable console messages about successfull inventory load events")
    private boolean logSuccessfulLoads = true;
    @Comment("Number of how many inventory backups are kept per player")
    private int playerInventoryBackupCount = 10;

    public PluginConfig(Path path) {

        super(path, BukkitYamlProperties.builder().setFormatter(FieldNameFormatters.LOWER_UNDERSCORE).build());
    }

    @ConfigurationElement
    @Getter
    @Setter
    public static class DatabaseConfig {

        private String username = "sa";
        private String password = "sa";
        private String driver = "h2";
        private String url = "jdbc:h2:~/inventory.db";
    }
}
