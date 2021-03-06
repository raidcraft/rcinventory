package de.raidcraft.rcinventory.database;

import de.raidcraft.rcinventory.RCInventory;
import de.raidcraft.rcinventory.holder.SimpleHolder;
import de.raidcraft.rcinventory.inventory.Base64Inventory;
import de.raidcraft.rcinventory.inventory.Inventory;
import io.ebean.Finder;
import io.ebean.annotation.Transactional;
import lombok.Getter;
import lombok.Setter;
import net.silthus.ebean.BaseEntity;

import javax.persistence.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    @Lob
    @Basic(fetch= FetchType.EAGER)
    private String serializedEnderChest;
    private Float saturation;
    private Float exp;
    private int level;
    private long creationMillis;
    private String world;
    private Double health;
    @Transient
    Inventory inventory;

    public Inventory getInventory() {
        if(inventory == null) {
            try {
                this.inventory = asInventory();
            } catch (IOException e) {
                RCInventory.instance().getLogger().warning("Failed to deserialize '" +
                        holderId.toString() + "' inventory");
            }
        }

        return inventory;
    }

    public TDatabaseInventory(Inventory inventory) {

        this.holderId = inventory.getHolder().getUniqueId();
        this.serializedInventory = inventory.serialize();
        this.serializedEnderChest = inventory.enderChestSerialize();
        this.saturation = inventory.getHolder().getSaturation();
        this.exp = inventory.getHolder().getExp();
        this.level = inventory.getHolder().getLevel();
        this.creationMillis = inventory.getCreationMillis();
        this.world = inventory.getWorld();
        this.health = inventory.getHolder().getHealth();
        this.inventory = inventory;
    }

    private Base64Inventory asInventory() throws IOException {

        if(world == null) world = "";
        if(saturation == null) saturation = 0F;
        if(exp == null) exp = 0F;
        if(health == null) health = 0D;

        SimpleHolder simpleHolder = new SimpleHolder(holderId, world, exp, level, saturation, health);
        return new Base64Inventory(simpleHolder, serializedInventory, serializedEnderChest, creationMillis);
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

    public static TDatabaseInventory getLatest(UUID holderId, List<String> worlds) {
        List<TDatabaseInventory> databaseInventories =
                find.query().where().eq("holder_id", holderId).in("world", worlds)
                        .orderBy().desc("creation_millis")
                        .setMaxRows(1).findList();
        if(databaseInventories.size() < 1) return null;

        return databaseInventories.get(0);
    }

    @Transactional
    public static Set<TDatabaseInventory> getLatestOfAllWorlds(UUID holderId) {

        Set<TDatabaseInventory> distinctInventories =
                find.query().where().eq("holder_id", holderId).select("world").setDistinct(true)
                        .orderBy().desc("creation_millis").findSet();
        Set<TDatabaseInventory> latest = new HashSet<>();
        for(TDatabaseInventory entry : distinctInventories) {
            latest.add(getLatest(holderId, entry.getWorld()));
        }
        return latest;
    }

    public static List<TDatabaseInventory> getInventoriesOrderedByDate(UUID holderId) {

        return find.query().where().eq("holder_id", holderId)
                .orderBy().desc("creation_millis")
                .findList();
    }

    public static Set<UUID> getStoredHolders() {

        Set<TDatabaseInventory> distinctInventories = find.query().select("holderId").setDistinct(true).findSet();

        Set<UUID> distinctHolderIds = new HashSet<>();
        distinctInventories.forEach(entry -> distinctHolderIds.add(entry.holderId));
        return distinctHolderIds;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof TDatabaseInventory)) return false;

        TDatabaseInventory other = (TDatabaseInventory)obj;

        if(!holderId.equals(other.getHolderId())) return false;
        if(!serializedInventory.equals(other.getSerializedInventory())) return false;
        if(serializedEnderChest == null) return false; // Ender chest was not synchronized in every version
        if(!serializedEnderChest.equals(other.getSerializedEnderChest())) return false;
        if(!saturation.equals(other.getSaturation())) return false;
        if(!health.equals(other.getHealth())) return false;
        if(level != other.getLevel()) return false;
        if(!exp.equals(other.getExp())) return false;
        if(!world.equals(other.getWorld())) return false;

        return true;
    }
}
