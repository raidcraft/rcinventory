package de.raidcraft.rcinventory;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.raidcraft.rcinventory.database.TDatabaseInventory;
import de.raidcraft.rcinventory.holder.SimpleHolder;
import de.raidcraft.rcinventory.inventory.SimpleInventory;
import io.ebean.Model;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

public class UnitTests {
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
    @DisplayName("TDatabaseInventory")
    class TDatabaseInventoryTests {
        @Test
        @DisplayName("equals")
        void equalsTest() {

            UUID holderId = server.addPlayer().getUniqueId();
            SimpleHolder simpleHolder = new SimpleHolder(holderId, "world", 1, 2, 3, 4);
            ItemStack[] contents = { new ItemStack(Material.COBBLESTONE) };
            SimpleInventory simpleInventory = new SimpleInventory(1, simpleHolder, contents);

            TDatabaseInventory databaseInventory1 = new TDatabaseInventory(simpleInventory);
            TDatabaseInventory databaseInventory2 = new TDatabaseInventory(simpleInventory);

            assertThat(databaseInventory1.equals(databaseInventory2)).isTrue();
        }

        @Test
        @DisplayName("not-equals")
        void notEqualsTest() {

            UUID holderId = server.addPlayer().getUniqueId();
            SimpleHolder simpleHolder = new SimpleHolder(holderId, "world", 1, 2, 3, 4);
            ItemStack[] contents1 = { new ItemStack(Material.COBBLESTONE) };
            ItemStack[] contents2 = { new ItemStack(Material.COBBLESTONE), new ItemStack(Material.SAND)};
            SimpleInventory simpleInventory1 = new SimpleInventory(1, simpleHolder, contents1);
            SimpleInventory simpleInventory2 = new SimpleInventory(1, simpleHolder, contents2);

            TDatabaseInventory databaseInventory1 = new TDatabaseInventory(simpleInventory1);
            TDatabaseInventory databaseInventory2 = new TDatabaseInventory(simpleInventory2);

            assertThat(!databaseInventory1.equals(databaseInventory2)).isTrue();
        }
    }
}
