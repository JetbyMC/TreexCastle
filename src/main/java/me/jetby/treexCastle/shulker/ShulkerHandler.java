package me.jetby.treexCastle.shulker;

import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionExecute;
import me.jetby.treexCastle.TreexCastle;
import me.jetby.treexCastle.configuration.MenusConfiguration;
import me.jetby.treexCastle.configuration.TypesConfiguration;
import me.jetby.treexCastle.gui.LootGui;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ShulkerHandler(@NotNull TreexCastle plugin) implements Listener {

    public ShulkerHandler(@NotNull TreexCastle plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getClickedBlock() == null) return;

        ShulkerInstance instance = plugin.getShulkerManager().getInstanceAt(e.getClickedBlock().getLocation());

        if (instance == null) {
            return;
        }
        ShulkerType shulker = instance.getType();

        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            e.setCancelled(true);

            if (shulker == null) return;

            if (shulker.lootDelivery() == TypesConfiguration.LootDelivery.GUI) {
                if (instance.isLooted()) {
                    new LootGui(player, MenusConfiguration.GUI_CONFIG_MAP.get(shulker.deliveryGui()), plugin, instance);
                }
            }

        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Location loc = e.getBlock().getLocation();
        ShulkerInstance instance = plugin.getShulkerManager().getInstanceAt(loc);
        if (instance == null) return;
        ShulkerType type = instance.getType();
        if (type == null) return;

        e.setCancelled(true);

        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            e.getBlock().setType(type.material());
        }

        instance.setDurability(instance.getDurability() - 1);
        if (type.onBreak() != null)
            ActionExecute.run(ActionContext.of(player, plugin)
                    .with(instance)
                    .replace("{blocks_left}", String.valueOf(instance.getDurability())), type.onBreak());

        if (instance.getDurability() <= 0) {
            if (instance.isLooted()) return;
            instance.setLooted(true);
            handleLoot(player, instance, e);

            return;
        }

        if (type.holo().enable())
            type.updateHologram(type.holo(), instance);
    }

    public void handleLoot(Player player, ShulkerInstance instance, BlockBreakEvent event) {
        ShulkerType shulker = instance.getType();

        switch (shulker.lootDelivery()) {
            case GUI -> handleTimer(player, instance, event, () -> {
                if (instance.getSharedLootInventory()!=null)
                    instance.getSharedLootInventory().close();
            });
            case FLYING -> handleFlying(player, instance, event);
            default -> handleDrop(player, instance, event);
        }
    }

    private void handleFlying(Player player, ShulkerInstance instance, BlockBreakEvent e) {
        ShulkerType shulker = instance.getType();

        List<ItemStack> loot = instance.getLoot();

        int totalItems = loot.size();
        int durationTicks = shulker.removeAfter() * 20;

        if (durationTicks <= 0) durationTicks = 1;

        double itemsPerTick = (double) totalItems / durationTicks;

        handleTimer(player, instance, e, null);

        new BukkitRunnable() {
            double progress = 0;
            int index = 0;

            @Override
            public void run() {
                progress += itemsPerTick;

                while (progress >= 1 && index < totalItems) {
                    progress -= 1;

                    ItemStack item = loot.get(index++);

                    ShulkerManager.dropFlyingItem(
                            shulker,
                            item,
                            shulker.holoLocation(shulker.holoRemove(), e.getBlock().getLocation())
                    );
                }

                if (index >= totalItems) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

    }

    private void handleDrop(Player player, ShulkerInstance instance, BlockBreakEvent e) {
        handleTimer(player, instance, e, () -> {
            ShulkerManager.dropLoot(instance.getLoot(), instance.getType(), instance.getLocation());
        });


    }

    private void handleTimer(Player player, ShulkerInstance instance, BlockBreakEvent e, Runnable onFinish) {
        ShulkerType shulker = instance.getType();

        final int[] timeLeft = {shulker.removeAfter()};
        instance.setRemoveAfter(timeLeft[0]);
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {

            if (timeLeft[0] <= 0) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (onFinish != null) {
                        onFinish.run();
                    }
                    if (shulker.onDespawn() != null)
                        ActionExecute.run(ActionContext.of(player, plugin).with(instance), shulker.onDespawn());
                    e.getBlock().setType(Material.AIR);
                    shulker.remove(plugin, instance);
                });
                task.cancel();
                return;
            }
            shulker.updateHologram(shulker.holoRemove(), instance);
            instance.setRemoveAfter(timeLeft[0]);
            timeLeft[0]--;

        }, 0, 20L);

    }

}
