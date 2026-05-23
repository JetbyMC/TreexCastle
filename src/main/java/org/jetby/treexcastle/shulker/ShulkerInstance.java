package org.jetby.treexcastle.shulker;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
@Setter
public class ShulkerInstance {
    private final UUID id;
    private final ShulkerType type;
    private int durability;
    private int maxDurability;
    private Location location;
    private Inventory sharedLootInventory;
    private final List<ItemStack> loot;
    private int removeAfter = 0;
    private boolean looted = false;

    public ShulkerInstance(ShulkerType shulker, Location location) {
        this.id = UUID.randomUUID();
        this.type = shulker;
        this.durability = shulker.durability();
        this.maxDurability = shulker.durability();
        this.location = location;
        this.loot = ShulkerManager.getRandomLoot(type);
        this.removeAfter = shulker.removeAfter();
        SHULKER_INSTANCE_LIST.put(id, this);
    }

    public static ShulkerInstance of(ShulkerType shulker, Location location) {
        return new ShulkerInstance(shulker, location);
    }

    public static Map<UUID, ShulkerInstance> SHULKER_INSTANCE_LIST = new HashMap<>();

    public ShulkerInstance(ShulkerType shulker, int durability, int maxDurability, Location location) {
        this.id = UUID.randomUUID();
        this.type = shulker;
        this.durability = durability;
        this.maxDurability = maxDurability;
        this.location = location;
        this.loot = ShulkerManager.getRandomLoot(type);
        this.removeAfter = shulker.removeAfter();
        SHULKER_INSTANCE_LIST.put(id, this);
    }

}