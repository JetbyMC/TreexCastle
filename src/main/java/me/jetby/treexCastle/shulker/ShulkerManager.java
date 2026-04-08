package me.jetby.treexCastle.shulker;

import lombok.Getter;
import me.jetby.libb.Keys;
import me.jetby.libb.util.Randomizer;
import me.jetby.treexCastle.TreexCastle;
import me.jetby.treexCastle.configuration.ItemsConfiguration;
import me.jetby.treexCastle.model.Mask;
import me.jetby.treexCastle.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static me.jetby.treexCastle.gui.MainGui.CHANCE;

public class ShulkerManager implements Listener {
    private final TreexCastle plugin;

    @Getter
    private int timeToStart;

    public ShulkerManager(TreexCastle plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void runTimer() {
        int time = plugin.getCfg().getTime();
        timeToStart = time;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (timeToStart <= 0) {
                spawnAllPossible();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    for (String string : plugin.getCfg().getMsg()) {
                        player.sendMessage(string);
                    }
                }

                timeToStart = time;
            }
            timeToStart--;


        }, 0L, 20L);

    }

    public static List<ItemStack> getRandomLoot(ShulkerType shulker) {
        if (shulker.items() == null || shulker.items().isEmpty()) {
            return List.of();
        }

        List<ItemStack> pool = new ArrayList<>();
        for (ItemsConfiguration.ItemsData item : shulker.items()) {
            if (Randomizer.rand(100) < item.chance()) {
                ItemStack stack = item.itemStack().clone();
                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.getPersistentDataContainer().remove(CHANCE);
                    stack.setItemMeta(meta);
                }
                pool.add(stack);
            }
        }

        if (pool.isEmpty()) {
            shulker.items().forEach(i -> {
                ItemStack stack = i.itemStack().clone();
                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.getPersistentDataContainer().remove(CHANCE);
                    stack.setItemMeta(meta);
                }
                pool.add(stack);
            });
        }

        int count = getCount(shulker);
        if (count <= 0) return List.of();

        List<ItemStack> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(pool.get(Randomizer.rand(pool.size())).clone());
        }
        return result;
    }

    public static int getCount(ShulkerType shulker) {
        if (shulker.lootAmount() == null || shulker.items() == null || shulker.items().isEmpty()) return 0;

        try {
            if (shulker.lootAmount().contains("-")) {
                int min, max;
                String[] parts = shulker.lootAmount().split("-");
                min = Integer.parseInt(parts[0].trim());
                max = Integer.parseInt(parts[1].trim());
                return Randomizer.randInteger(min, max);
            } else {
                return Integer.parseInt(shulker.lootAmount());
            }
        } catch (NumberFormatException e) {
            Logger.error("Invalid lootAmount: " + shulker.lootAmount());
            return 0;
        }
    }

    public static void dropLoot(List<ItemStack> items, ShulkerType shulker, Location location) {
        int count = getCount(shulker);

        for (int i = 0; i < count; i++) {
            ItemStack original = items.get(Randomizer.rand(items.size())).clone();
            ItemStack toDrop = applyMask(shulker, original);
            Item droppedItem = location.getWorld().dropItemNaturally(location, toDrop);

            if (shulker.isMask()) {
                final ItemStack finalOriginal = original;
                final ItemStack finalToDrop = toDrop;

                Bukkit.getScheduler().runTaskLater(TreexCastle.INSTANCE, () -> {
                    if (droppedItem.isValid() && finalToDrop.hasItemMeta()) {
                        droppedItem.customName(finalToDrop.getItemMeta().displayName());
                        droppedItem.setCustomNameVisible(true);
                    }
                }, 1L);

                Bukkit.getScheduler().runTask(TreexCastle.INSTANCE, () -> {
                    droppedItem.setMetadata("treexcastle_originalItem",
                            new FixedMetadataValue(TreexCastle.INSTANCE, finalOriginal));
                });
            }
        }
    }
    public static void dropFlyingItem(ShulkerType shulker, ItemStack originalItem, Location location) {

        ItemStack itemToDrop = originalItem != null ? originalItem.clone() : new ItemStack(Material.DIAMOND);

        Mask selectedMask = null;

        if (shulker.isMask() && !shulker.maskMap().isEmpty()) {
            List<String> maskKeys = new ArrayList<>(shulker.maskMap().keySet());
            String randomMaskKey = maskKeys.get(Randomizer.rand(maskKeys.size()));
            selectedMask = shulker.maskMap().get(randomMaskKey);

            ItemStack maskedItem = new ItemStack(selectedMask.material());
            ItemMeta meta = maskedItem.getItemMeta();
            if (meta != null) {
                meta.displayName(selectedMask.name());

                if (selectedMask.enchanted()) {
                    meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                }

                meta.getPersistentDataContainer().set(
                        TreexCastle.ITEM_KEY,
                        PersistentDataType.STRING,
                        UUID.randomUUID().toString()
                );

                maskedItem.setItemMeta(meta);
            }
            itemToDrop = maskedItem;
        }

        Item droppedItem = location.getWorld().dropItemNaturally(location, itemToDrop);
        droppedItem.setPickupDelay(shulker.dropParticle().pickupDelay());

        if (shulker.isMask() && selectedMask != null) {
            final Mask finalMask = selectedMask;
            Bukkit.getScheduler().runTaskLater(TreexCastle.INSTANCE, () -> {
                if (droppedItem.isValid()) {
                    droppedItem.customName(finalMask.name());
                    droppedItem.setCustomNameVisible(true);
                }
            }, 1L);
        }

        if (shulker.isMask() && originalItem != null) {
            Bukkit.getScheduler().runTask(TreexCastle.INSTANCE, () -> {
                droppedItem.setMetadata("treexcastle_originalItem",
                        new FixedMetadataValue(TreexCastle.INSTANCE, originalItem.clone()));
            });
        }

        double targetY = location.getY() + Randomizer.rand(shulker.dropParticle().minY(), shulker.dropParticle().maxY());
        double vertSpeed = Math.sqrt(2 * 0.08 * (targetY - location.getY()));
        double angle = Randomizer.randDouble() * Math.PI * 2;
        double horSpeed = shulker.dropParticle().minSpeed() + (shulker.dropParticle().maxSpeed() - shulker.dropParticle().minSpeed()) * Randomizer.randDouble();
        Vector velocity = new Vector(Math.cos(angle) * horSpeed, vertSpeed, Math.sin(angle) * horSpeed);
        droppedItem.setVelocity(velocity);

        location.getWorld().playSound(location, shulker.dropParticle().sound(),
                shulker.dropParticle().volume(),
                shulker.dropParticle().pitch());

        new BukkitRunnable() {
            final Vector lastPosition = droppedItem.getLocation().toVector();

            @Override
            public void run() {
                if (!droppedItem.isValid()) {
                    cancel();
                    return;
                }
                Vector currentPosition = droppedItem.getLocation().toVector();
                Vector step = currentPosition.clone().subtract(lastPosition).multiply(1.0 / shulker.dropParticle().amount());
                for (int i = 0; i < shulker.dropParticle().amount(); i++) {
                    Vector particlePos = lastPosition.clone().add(step.clone().multiply(i));
                    location.getWorld().spawnParticle(shulker.dropParticle().particle(),
                            particlePos.toLocation(location.getWorld()), 0, 0, 0, 0, 0);
                }
                lastPosition.copy(currentPosition);
            }
        }.runTaskTimerAsynchronously(TreexCastle.INSTANCE, 0, 1);
    }
    public void removeAllClones() {
        List<ShulkerInstance> toRemove = new ArrayList<>(ShulkerInstance.SHULKER_INSTANCE_LIST.values());
        for (ShulkerInstance shulker : toRemove) {
            try {
                shulker.getType().remove(plugin, shulker);
            } catch (Exception ex) {
                Logger.error("Error deleting clone " + shulker.getId() + ": " + ex.getMessage());
            }
        }
    }
    public static ItemStack applyMask(ShulkerType shulker, ItemStack originalItem) {
        if (!shulker.isMask() || shulker.maskMap().isEmpty()) return originalItem;

        List<String> maskKeys = new ArrayList<>(shulker.maskMap().keySet());
        String randomMaskKey = maskKeys.get(Randomizer.rand(maskKeys.size()));
        Mask randomMask = shulker.maskMap().get(randomMaskKey);

        ItemStack maskedItem = new ItemStack(randomMask.material());
        ItemMeta meta = maskedItem.getItemMeta();
        if (meta != null) {
            meta.displayName(randomMask.name());
            if (randomMask.enchanted()) {
                meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.getPersistentDataContainer().set(
                    TreexCastle.ITEM_KEY,
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString()
            );
            maskedItem.setItemMeta(meta);
        }
        return maskedItem;
    }
    public void spawnAllPossible() {
        if (plugin.getTypes().getShulkers().isEmpty()) return;

        for (Location location : plugin.getLocations().getLocations()) {
            if (location == null) continue;

            boolean occupied = plugin.getLocations().isOccupied(location);
            if (occupied) continue;

            String type = getRandomType();
            if (type == null) continue;

            plugin.getLocations().addLocation(location);
            plugin.getTypes().getShulkers().get(type).spawn(location);
        }
    }

    public String getRandomType() {
        if (plugin.getTypes().getShulkers().isEmpty()) return null;

        int total = 0;
        for (ShulkerType sh : plugin.getTypes().getShulkers().values()) {
            total += Math.max(0, sh.spawnChance());
        }
        if (total <= 0) return null;

        int cum = 0;
        for (Map.Entry<String, ShulkerType> e : plugin.getTypes().getShulkers().entrySet()) {
            cum += Math.max(0, e.getValue().spawnChance());
            if (Randomizer.rand(total) < cum) return e.getKey();
        }
        return null;
    }

    public ShulkerInstance getInstanceAt(Location location) {
        return ShulkerInstance.SHULKER_INSTANCE_LIST
                .values()
                .stream()
                .filter(shulker ->
                        shulker.getLocation().equals(location)
                )
                .findFirst()
                .orElse(null);
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Player player)) return;

        Item item = event.getItem();

        List<MetadataValue> metaValues = item.getMetadata("treexcastle_originalItem");
        if (!metaValues.isEmpty()) {
            ItemStack originalItem = (ItemStack) metaValues.get(0).value();
            event.setCancelled(true);
            item.remove();
            ItemStack cleanItem = originalItem.clone();
            ItemMeta meta = cleanItem.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().remove(CHANCE);
                meta.getPersistentDataContainer().remove(Keys.GUI_ITEM);
                cleanItem.setItemMeta(meta);
            }
            player.getInventory().addItem(cleanItem);
            player.updateInventory();
        }
    }
}
