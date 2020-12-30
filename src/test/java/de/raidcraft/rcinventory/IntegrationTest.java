package de.raidcraft.rcinventory;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import de.raidcraft.rcinventory.database.TDatabaseInventory;
import io.ebean.Model;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTest {

    private ServerMock server;
    private RCInventory plugin;

    @BeforeEach
    void setUp() {

        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.load(RCInventory.class);
    }

    @AfterEach
    void tearDown() {

        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("InventoryManager")
    class InventoryManagerTests {

        @BeforeEach
        void setUp() {

            // Delete all entries
            TDatabaseInventory.find.all().forEach(Model::delete);
        }

        @Test
        @DisplayName("save-inventory")
        void storeInventory() {

            Player player = server.addPlayer();

            // Database must be empty
            assertThat(TDatabaseInventory.find.query().findCount() == 0).isTrue();

            // Add player inventory
            plugin.getInventoryManager().savePlayerInventory(player);

            // Check if added
            assertThat(TDatabaseInventory.find.query().findCount() == 1).isTrue();

            // Add again -> Due to missing changes must not be saved
            plugin.getInventoryManager().savePlayerInventory(player);

            // Must still be '1'
            assertThat(TDatabaseInventory.find.query().findCount() == 1).isTrue();

            Player player2 = server.addPlayer();

            // Add another players inventory
            plugin.getInventoryManager().savePlayerInventory(player2);

            // Must be '2'
            assertThat(TDatabaseInventory.find.query().findCount() == 2).isTrue();
        }

        @Test
        @DisplayName("cleanup")
        void cleanup() {

            final int playerCount = 7;
            final int backupCount = 5;
            final int inventorySaveCount = 10;

            // Add worlds
            WorldMock miningWorld = new WorldMock();
            miningWorld.setName("mining");
            server.addWorld(miningWorld);
            WorldMock eventWorld = new WorldMock();
            eventWorld.setName("event");
            server.addWorld(eventWorld);

            // Add some players
            List<Player> players = new ArrayList<>();
            for(int i = 0; i < playerCount; i++) {
                players.add(server.addPlayer());
            }

            // Database must be empty
            assertThat(TDatabaseInventory.find.query().findCount() == 0).isTrue();

            // Mining world:
            // Save inventories for player 10 times by changing level each time
            players.forEach(player -> {
                for(int i = 0; i < inventorySaveCount; i++) {
                    player.teleport(miningWorld.getSpawnLocation());
                    player.setLevel(i);
                    plugin.getInventoryManager().savePlayerInventory(player);

                    // Sleep to make sure the millisecond count of each entry differs
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Event world:
            // Save inventories for player 10 times by changing level each time
            players.forEach(player -> {
                for(int i = 0; i < inventorySaveCount; i++) {
                    player.teleport(eventWorld.getSpawnLocation());
                    player.setLevel(i);
                    plugin.getInventoryManager().savePlayerInventory(player);

                    // Sleep to make sure the millisecond count of each entry differs
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            // All new entries must be saved
            assertThat(TDatabaseInventory.find.query().findCount() == 2 * players.size() * inventorySaveCount)
                    .isTrue();

            // Set backup size to 5 inventories per player
            plugin.getPluginConfig().setPlayerInventoryBackupCount(backupCount);

            // Run cleanup
            plugin.getInventoryManager().cleanup();

            // Check if database entries was cleared
            assertThat(TDatabaseInventory.find.query().findCount() == 2 * players.size() * backupCount)
                    .isTrue();

            // Make sure that only the oldest entries where deleted
            List<TDatabaseInventory> allInventories = TDatabaseInventory.find.all();
            allInventories.forEach(inventory -> {
                assertThat(inventory.getLevel() >= backupCount).isTrue();
            });
        }

        @Test
        @DisplayName("world-config-partner-worlds")
        void partnerWorlds() {

            // Add worlds
            WorldMock miningWorld = new WorldMock();
            miningWorld.setName("mining");
            server.addWorld(miningWorld);
            WorldMock eventWorld = new WorldMock();
            eventWorld.setName("event");
            server.addWorld(eventWorld);

            // Add player to mining world
            Player player = server.addPlayer();

            // Database must be empty
            assertThat(TDatabaseInventory.find.query().findCount() == 0).isTrue();

            // Add player inventory (at mining world)
            player.teleport(miningWorld.getSpawnLocation());
            player.setSaturation(1.23F);
            plugin.getInventoryManager().savePlayerInventory(player);

            // Check if added
            assertThat(TDatabaseInventory.find.query().findCount() == 1).isTrue();

            // Add world configuration but without mining world
            PluginConfig.WorldConfig eventWorldConfig = new PluginConfig.WorldConfig();
            eventWorldConfig.addPartnerWorld("event");
            plugin.getPluginConfig().getWorlds().put("test", eventWorldConfig);

            // Teleport player to event world and try to restore
            player.teleport(eventWorld.getSpawnLocation());
            player.setSaturation(9.87F);
            plugin.getInventoryManager().restorePlayerInventory(player);

            // Saturation must still be 9.87
            assertThat(player.getSaturation() == 9.87F).isTrue();

            // Change world config to add mining world as partner world
            eventWorldConfig.addPartnerWorld("mining");

            // Restore inventory again
            plugin.getInventoryManager().restorePlayerInventory(player);

            // Saturation must now match the saved inventory saturation
            assertThat(player.getSaturation() == 1.23F).isTrue();
        }

        @Test
        @DisplayName("precache-inventory-no-world-config")
        void precacheInventoryNoWorldConfig() {

            // Add worlds
            WorldMock miningWorld = new WorldMock();
            miningWorld.setName("mining");
            server.addWorld(miningWorld);

            // Add player to mining world
            Player player = server.addPlayer();

            // Database must be empty
            assertThat(TDatabaseInventory.find.query().findCount() == 0).isTrue();

            // Add player inventory (at mining world)
            player.teleport(miningWorld.getSpawnLocation());
            player.setSaturation(1.23F);
            plugin.getInventoryManager().savePlayerInventory(player);
            player.setSaturation(4.67F);

            // Precache player inventory
            plugin.getInventoryManager().preloadAllLatestPlayerInventories(player.getName(), player.getUniqueId());

            // Clear database to be sure precached inventory is used
            TDatabaseInventory.find.all().forEach(Model::delete);

            // Database must be empty
            assertThat(TDatabaseInventory.find.query().findCount() == 0).isTrue();

            // Restore inventory
            plugin.getInventoryManager().restorePlayerInventory(player);

            // Saturation must now match the saved inventory saturation
            assertThat(player.getSaturation() == 1.23F).isTrue();
        }

        @Test
        @DisplayName("precache-inventory-world-config")
        void precacheInventoryWithWorldConfig() {

            // Add worlds
            WorldMock miningWorld = new WorldMock();
            miningWorld.setName("mining");
            server.addWorld(miningWorld);
            WorldMock eventWorld = new WorldMock();
            eventWorld.setName("event");
            server.addWorld(eventWorld);

            // Add player to mining world
            Player player = server.addPlayer();

            // Database must be empty
            assertThat(TDatabaseInventory.find.query().findCount() == 0).isTrue();


            // Add player inventory (at mining world)
            player.teleport(miningWorld.getSpawnLocation());
            player.setSaturation(1.23F);
            plugin.getInventoryManager().savePlayerInventory(player);
            player.setSaturation(4.67F);

            // Sleep to make sure the millisecond count of each entry differs
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Add player inventory (at event world)
            player.teleport(eventWorld.getSpawnLocation());
            player.setSaturation(3.21F);
            plugin.getInventoryManager().savePlayerInventory(player);
            player.setSaturation(5.78F);

            // Precache player inventory
            plugin.getInventoryManager().preloadAllLatestPlayerInventories(player.getName(), player.getUniqueId());

            // Clear database to be sure precached inventory is used
            TDatabaseInventory.find.all().forEach(Model::delete);

            // Database must be empty
            assertThat(TDatabaseInventory.find.query().findCount() == 0).isTrue();

            // Add world configuration but without mining world
            PluginConfig.WorldConfig miningWorldConfig = new PluginConfig.WorldConfig();
            miningWorldConfig.addPartnerWorld("mining");
            plugin.getPluginConfig().getWorlds().put("test", miningWorldConfig);

            // Teleport player back to mining world
            player.teleport(miningWorld.getSpawnLocation());

            // Restore inventory
            plugin.getInventoryManager().restorePlayerInventory(player);

            // Saturation must now match the saved inventory saturation
            assertThat(player.getSaturation() == 1.23F).isTrue();
        }

        @Test
        @DisplayName("precache-inventory-world-config-and-partner-worlds")
        void precacheInventoryWithWorldConfigPartnerWorld() {

            // Add worlds
            WorldMock miningWorld = new WorldMock();
            miningWorld.setName("mining");
            server.addWorld(miningWorld);
            WorldMock eventWorld = new WorldMock();
            eventWorld.setName("event");
            server.addWorld(eventWorld);

            // Add player to mining world
            Player player = server.addPlayer();

            // Database must be empty
            assertThat(TDatabaseInventory.find.query().findCount() == 0).isTrue();

            // Add player inventory (at event world)
            player.teleport(eventWorld.getSpawnLocation());
            player.setSaturation(3.21F);
            plugin.getInventoryManager().savePlayerInventory(player);
            player.setSaturation(5.78F);

            // Sleep to make sure the millisecond count of each entry differs
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Add player inventory (at mining world)
            player.teleport(miningWorld.getSpawnLocation());
            player.setSaturation(1.23F);
            plugin.getInventoryManager().savePlayerInventory(player);
            player.setSaturation(4.67F);

            // Precache player inventory
            plugin.getInventoryManager().preloadAllLatestPlayerInventories(player.getName(), player.getUniqueId());

            // Clear database to be sure precached inventory is used
            TDatabaseInventory.find.all().forEach(Model::delete);

            // Database must be empty
            assertThat(TDatabaseInventory.find.query().findCount() == 0).isTrue();

            // Add world config which contains both worlds
            PluginConfig.WorldConfig eventWorldConfig = new PluginConfig.WorldConfig();
            eventWorldConfig.addPartnerWorld("event");
            eventWorldConfig.addPartnerWorld("mining");
            plugin.getPluginConfig().getWorlds().put("test", eventWorldConfig);

            // Restore inventory
            plugin.getInventoryManager().restorePlayerInventory(player);

            // Saturation must now match the saved inventory saturation of mining world
            assertThat(player.getSaturation() == 1.23F).isTrue();
        }
    }

    @Nested
    @DisplayName("Commands")
    class Commands {

        private Player player;

        @BeforeEach
        void setUp() {
            player = server.addPlayer();
        }

        @Nested
        @DisplayName("/rcinventories")
        class AdminCommands {

            @Test
            @DisplayName("reload")
            void reload() {

                server.dispatchCommand(server.getConsoleSender(),"rcinventory reload");
            }
        }
    }
}
