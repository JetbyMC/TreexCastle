package org.jetby.treexcastle.shulker;

import org.jetby.libb.action.ActionContext;
import org.jetby.libb.action.ActionExecute;
import org.jetby.libb.action.record.ActionBlock;
import org.jetby.treexcastle.TreexCastle;
import org.jetby.treexcastle.configuration.ItemsConfiguration;
import org.jetby.treexcastle.configuration.TypesConfiguration;
import org.jetby.treexcastle.model.FlyingDropParticle;
import org.jetby.treexcastle.model.Holo;
import org.jetby.treexcastle.model.Mask;
import org.jetby.treexcastle.util.HoloUtil;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public record ShulkerType(String id,
                          Material material,
                          int durability,
                          TypesConfiguration.LootDelivery lootDelivery,
                          String deliveryGui,
                          String lootAmount,
                          int removeAfter,
                          boolean isMask,
                          int takeCooldown,
                          Map<String, Mask> maskMap,
                          FlyingDropParticle dropParticle,
                          int spawnChance,
                          Holo holo,
                          Holo holoRemove,
                          ActionBlock onSpawn,
                          ActionBlock onBreak,
                          ActionBlock onDespawn
) {
    public void spawn(Location location) {
        ShulkerInstance instance = ShulkerInstance.of(this, location);

        TreexCastle.INSTANCE.getLocations().acquire(location);

        if (holo.enable()) {
            createHolo(holo, instance, location);
        }

        location.getBlock().setType(material);
        if (onSpawn != null)
            ActionExecute.run(ActionContext.of(null, TreexCastle.INSTANCE).with(instance), onSpawn);
    }

    public void remove(TreexCastle plugin, ShulkerInstance instance) {
        try {
            instance.getLocation().getBlock().setType(Material.AIR);
            if (holo.enable())
                HoloUtil.remove(instance.getId().toString());
            plugin.getLocations().reset(instance.getLocation());
            ShulkerInstance.SHULKER_INSTANCE_LIST.remove(instance.getId());
        } catch (Exception ignored) {
        }
    }

    public void createHolo(Holo holo, ShulkerInstance instance, Location location) {
        if (!holo.enable()) return;
        HoloUtil.create(buildHoloLines(holo, instance), holoLocation(holo, location), instance.getId().toString());
    }
    public void updateHologram(Holo holo, ShulkerInstance instance) {
        if (!holo.enable()) {
            HoloUtil.remove(instance.getId().toString());
            return;
        }
        HoloUtil.update(buildHoloLines(holo, instance), holoLocation(holo, instance.getLocation()), instance.getId().toString());
    }

    private List<String> buildHoloLines(Holo holo, ShulkerInstance instance) {
        List<String> lines = new ArrayList<>(holo.lines());
        lines.replaceAll(s -> s.replace("{blocks_left}", String.valueOf(instance.getDurability())));
        lines.replaceAll(s -> s.replace("{time_left}", String.valueOf(instance.getRemoveAfter())));
        return lines;
    }

    public Location holoLocation(Holo holo, Location base) {
        return base.clone().add(holo.holoX(), holo.holoY(), holo.holoZ());
    }
}