package org.jetby.treexcastle;

import org.jetby.libb.command.AdvancedCommand;
import org.jetby.libb.command.annotations.PlayerOnly;
import org.jetby.libb.command.annotations.SubCommand;
import org.jetby.libb.platform.PlatformMeta;
import org.jetby.libb.platform.PlatformSender;
import org.jetby.treexcastle.shulker.ShulkerType;
import org.jetby.treexcastle.util.LocationHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.jetby.treexcastle.TreexCastle.WAND_KEY;

public class ShulkerCommand extends AdvancedCommand {

    private final TreexCastle plugin;

    public ShulkerCommand(@NotNull TreexCastle plugin) {
        super(plugin.getCommand("shulker"), plugin, false);
        this.plugin = plugin;
    }

    @SubCommand("reload")
    public void reload(CommandSender sender) {
        long start = System.currentTimeMillis();
        try {
            plugin.getCfg().load();

            plugin.getItems().save();
            plugin.getItems().load();

            plugin.getTypes().load();

            plugin.getMenus().loadGuis();

            plugin.getLocations().save();
            plugin.getLocations().load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long total = System.currentTimeMillis() - start;
        PlatformSender.sendMessage(sender, TreexCastle.r("<green>TreexCastle reloaded in " + total + " ms."));
    }

    @SubCommand("start")
    public void start(CommandSender sender) {
        plugin.getShulkerManager().spawnAllPossible();
        PlatformSender.sendMessage(sender, TreexCastle.r("<green>Successfully spawned all shulkers"));

    }

    @SubCommand("start")
    public void start(CommandSender sender, String shulkerName, String locationStr) {
        ShulkerType shulker = plugin.getTypes().getShulkers().get(shulkerName);
        if (shulker != null) {
            Location loc = LocationHandler.deserialize(locationStr, plugin);
            if (loc != null) {
                shulker.spawn(loc);
            } else {
                sender.sendMessage("Incorrect format, pls use 0;0;0;world");
            }
        }

    }

    @SubCommand("stop")
    public void stop(CommandSender sender) {
        plugin.getShulkerManager().removeAllClones();
        PlatformSender.sendMessage(sender, TreexCastle.r("<green>All shulkers removed"));
    }

    @SubCommand("menu")
    @PlayerOnly
    public void menu(Player player) {
        plugin.getMainGui().open(player);
    }

    @SubCommand("wand")
    @PlayerOnly
    public void wand(Player player) {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.getPersistentDataContainer().set(WAND_KEY, PersistentDataType.STRING, "wand");
        PlatformMeta.setDisplayName(itemMeta, TreexCastle.r("<red><b>TreexCastle"));
        PlatformMeta.setLore(itemMeta, List.of(
                TreexCastle.r("<red>Right click <gray>- <white>Create location"),
                TreexCastle.r("<red>Left click <gray>- <white>Delete location")));
        item.setItemMeta(itemMeta);
        player.getInventory().addItem(item);
    }
}
