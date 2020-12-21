package de.raidcraft.rcinventory;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.raidcraft.rcinventory.util.BukkitSerialization;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SerializationTest {

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
    @DisplayName("Serialization")
    class Serialization {

        // XXX Test is ignored due to usage of PaperMC API which class adaptions are not working here
        @Test
        @Disabled
        @DisplayName("Serialize ItemStack")
        void serializeItemStack() throws IOException {

            ItemStack[] itemStacks = { new ItemStack(Material.COBBLESTONE)};

            String serialized = BukkitSerialization.itemStackArrayToBase64(itemStacks);
            ItemStack[] deserialized = BukkitSerialization.itemStackArrayFromBase64(serialized);

            assertThat(deserialized.length == itemStacks.length).isTrue();
            assertThat(deserialized[0].equals(itemStacks[0])).isTrue();

            String serialized2 = BukkitSerialization.itemStackArrayToBase64(deserialized);

            assertThat(serialized.equals(serialized2)).isTrue();
        }
    }
}
