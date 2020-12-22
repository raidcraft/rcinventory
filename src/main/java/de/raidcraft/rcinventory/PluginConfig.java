package de.raidcraft.rcinventory;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.ConfigurationElement;
import de.exlll.configlib.annotation.ElementType;
import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;
import de.exlll.configlib.format.FieldNameFormatters;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PluginConfig extends BukkitYamlConfiguration {

    public PluginConfig(Path path) {

        super(path, BukkitYamlProperties.builder().setFormatter(FieldNameFormatters.LOWER_UNDERSCORE).build());
    }

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

    @ElementType(WorldConfig.class)
    @Comment("World specific configuration")
    private Map<String, WorldConfig> worlds = initWorldConfig();

    private Map<String, WorldConfig> initWorldConfig() {
        Map<String, WorldConfig> worlds = new HashMap<>();
        List<String> partnerWorlds = new ArrayList<>();
        partnerWorlds.add("mining");
        partnerWorlds.add("lobby");
        worlds.put("world", new WorldConfig());

        return worlds;
    }

    public WorldConfig getWorldConfig(String world) {

        for(Map.Entry<String, WorldConfig> entry : worlds.entrySet()) {
            if(entry.getValue().getPartnerWorlds().contains(world)) {
                return entry.getValue();
            }
        }

        return null;
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

    @ConfigurationElement
    @Getter
    @Setter
    public static class WorldConfig {

        @Comment("List of connected worlds")
        private List<String> partnerWorlds = new ArrayList<>();
        @Comment("Sync players inventory and armor")
        private boolean syncInventory;
        @Comment("Sync players health")
        private boolean syncHealth;
        @Comment("Sync players saturation")
        private boolean syncSaturation;
        @Comment("Sync players Exp")
        private boolean syncExp;

        public WorldConfig() {
            this(null, true, true, true, true);
        }

        public WorldConfig(List<String> partnerWorlds, boolean syncInventory,
                           boolean syncHealth, boolean syncSaturation, boolean syncExp) {
            if(partnerWorlds != null) {
                this.partnerWorlds.addAll(partnerWorlds);
            }
            this.syncInventory = syncInventory;
            this.syncHealth = syncHealth;
            this.syncSaturation = syncSaturation;
            this.syncExp = syncExp;
        }

        public void addPartnerWorld(String partnerWorld) {
            partnerWorlds.add(partnerWorld);
        }
    }
}
