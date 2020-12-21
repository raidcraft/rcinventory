package de.raidcraft.rcinventory.database;

import de.raidcraft.rcinventory.holder.SimpleHolder;
import de.raidcraft.rcinventory.inventory.Base64Inventory;
import de.raidcraft.rcinventory.inventory.Inventory;
import io.ebean.Finder;
import lombok.Getter;
import lombok.Setter;
import net.silthus.ebean.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "rcinventory_inventories")
public class TDatabaseInventory extends BaseEntity {

    public static final Finder<UUID, TDatabaseInventory> find = new Finder<>(TDatabaseInventory.class);

    UUID holderId;
    String serializedInventory;
    Float saturation;
    Float exp;
    int level;
    long creationMillis;
    String world;

    public TDatabaseInventory(Inventory inventory) {

        this.holderId = inventory.getHolder().getUniqueId();
        this.world = inventory.getWorld();
        this.serializedInventory = inventory.serialize();
        this.creationMillis = inventory.getCreationMillis();
    }

    public Base64Inventory asInventory() throws IOException {
        SimpleHolder simpleHolder = new SimpleHolder(holderId, world, exp, level, saturation);
        return new Base64Inventory(simpleHolder, serializedInventory, creationMillis);
    }

    public static TDatabaseInventory getLatest(UUID holderId) {
        List<TDatabaseInventory> databaseInventories =
                find.query().where().eq("player_id", holderId).orderBy().asc("creation_millis")
                        .setMaxRows(1).findList();
        if(databaseInventories.size() < 1) return null;

        return databaseInventories.get(0);
    }
}
