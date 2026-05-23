package org.jetby.treexcastle.handler;

import org.jetby.treexcastle.TreexCastle;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import static org.jetby.treexcastle.TreexCastle.WAND_KEY;


public record WandHandler(TreexCastle plugin) implements Listener {

    public WandHandler(@NotNull TreexCastle plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (block == null) return;

        Location location = block.getLocation();

        if (itemInHand.hasItemMeta() && itemInHand.getItemMeta().getPersistentDataContainer().has(WAND_KEY, PersistentDataType.STRING)) {
            e.setCancelled(true);
            if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                boolean removed = plugin.getLocations().removeLocation(location);
                if (removed) {
                    player.sendTitle("§dTreexCastle", "§cЛокация убрана");
                } else {
                    player.sendTitle("§dTreexCastle", "§7Локация не найдена");
                }
            } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (!plugin.getLocations().getLocations().contains(location)) {
                    plugin.getLocations().addLocation(location);
                    player.sendTitle("§dTreexCastle", "§aЛокация добавлена");

                    String type = plugin.getShulkerManager().getRandomType();
                    if (type == null) {
                        player.sendTitle("§dTreexCastle", "§cТип шалкера не выбран (проверьте spawnChance)");
                        return;
                    }

                    plugin.getTypes().getShulkers().get(type).spawn(location);
                }

            }
        }

    }

}