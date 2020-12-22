package de.raidcraft.rcinventory;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.raidcraft.rcinventory.database.TDatabaseInventory;
import io.ebean.Model;
import org.bukkit.entity.Player;
import org.h2.expression.function.ToDateParser;
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

        }

        @Test
        @DisplayName("save-inventory")
        void storeInventory() {

            Player player = server.addPlayer();

            // Delete all entries
            TDatabaseInventory.find.all().forEach(Model::delete);

            // Database must be empty
            assertThat(TDatabaseInventory.find.query().findCount() == 0).isTrue();

            // Add inventory
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

            // Add some players
            List<Player> players = new ArrayList<>();
            for(int i = 0; i < playerCount; i++) {
                players.add(server.addPlayer());
            }

            // Delete all entries
            TDatabaseInventory.find.all().forEach(Model::delete);

            // Database must be empty
            assertThat(TDatabaseInventory.find.query().findCount() == 0).isTrue();

            // Save inventories for player 10 times by changing level each time
            players.forEach(player -> {
                for(int i = 0; i < inventorySaveCount; i++) {
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

            // All entries must be saved
            assertThat(TDatabaseInventory.find.query().findCount() == players.size() * inventorySaveCount)
                    .isTrue();

            // Set backup size to 5 inventories per player
            plugin.getPluginConfig().setPlayerInventoryBackupCount(backupCount);

            // Run cleanup
            plugin.getInventoryManager().cleanup();

            // Check if database entries was cleared
            assertThat(TDatabaseInventory.find.query().findCount() == players.size() * backupCount)
                    .isTrue();

            // Make sure that only the oldest entries where deleted
            List<TDatabaseInventory> allInventories = TDatabaseInventory.find.all();
            allInventories.forEach(inventory -> {
                assertThat(inventory.getLevel() < inventorySaveCount).isTrue();
            });
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
