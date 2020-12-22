package de.raidcraft.rcinventory.database;

import de.raidcraft.rcinventory.holder.SimpleHolder;
import de.raidcraft.rcinventory.inventory.Base64Inventory;
import de.raidcraft.rcinventory.inventory.Inventory;
import io.ebean.Finder;
import lombok.Getter;
import lombok.Setter;
import net.silthus.ebean.BaseEntity;

import javax.persistence.*;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "rcinventory_inventories")
public class TDatabaseInventory extends BaseEntity {

    public static final Finder<UUID, TDatabaseInventory> find = new Finder<>(TDatabaseInventory.class);

    private UUID holderId;
    @Lob
    @Basic(fetch= FetchType.EAGER)
    private String serializedInventory;
    private Float saturation;
    private Float exp;
    private int level;
    private long creationMillis;
    private String world;
    private Double health;

    public TDatabaseInventory(Inventory inventory) {

        this.holderId = inventory.getHolder().getUniqueId();
        this.serializedInventory = inventory.serialize();
        this.saturation = inventory.getHolder().getSaturation();
        this.exp = inventory.getHolder().getExp();
        this.level = inventory.getHolder().getLevel();
        this.creationMillis = inventory.getCreationMillis();
        this.world = inventory.getWorld();
        this.health = inventory.getHolder().getHealth();
    }

    public Base64Inventory asInventory() throws IOException {

        if(world == null) world = "";
        if(saturation == null) saturation = 0F;
        if(exp == null) exp = 0F;
        if(health == null) health = 0D;

        SimpleHolder simpleHolder = new SimpleHolder(holderId, world, exp, level, saturation, health);
        return new Base64Inventory(simpleHolder, serializedInventory, creationMillis);
    }

    public static TDatabaseInventory getLatest(UUID holderId) {
        List<TDatabaseInventory> databaseInventories =
                find.query().where().eq("holder_id", holderId)
                        .orderBy().desc("creation_millis")
                        .setMaxRows(1).findList();
        if(databaseInventories.size() < 1) return null;

        return databaseInventories.get(0);
    }

    public static TDatabaseInventory getLatest(UUID holderId, String world) {
        List<TDatabaseInventory> databaseInventories =
                find.query().where().eq("holder_id", holderId).eq("world", world)
                        .orderBy().desc("creation_millis")
                        .setMaxRows(1).findList();
        if(databaseInventories.size() < 1) return null;

        return databaseInventories.get(0);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof TDatabaseInventory)) return false;

        TDatabaseInventory other = (TDatabaseInventory)obj;

        if(!holderId.equals(other.getHolderId())) return false;
        if(!serializedInventory.equals(other.getSerializedInventory())) return false;
        if(!saturation.equals(other.getSaturation())) return false;
        if(!health.equals(other.getHealth())) return false;
        if(level != other.getLevel()) return false;
        if(!exp.equals(other.getExp())) return false;
        if(!world.equals(other.getWorld())) return false;

        return true;
    }
}
