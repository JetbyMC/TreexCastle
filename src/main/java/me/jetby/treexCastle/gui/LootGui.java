package me.jetby.treexCastle.gui;

import me.jetby.libb.Keys;
import me.jetby.libb.gui.parser.ParseUtil;
import me.jetby.libb.gui.parser.ParsedGui;
import me.jetby.libb.util.Randomizer;
import me.jetby.treexCastle.TreexCastle;
import me.jetby.treexCastle.shulker.ShulkerInstance;
import me.jetby.treexCastle.shulker.ShulkerManager;
import me.jetby.treexCastle.shulker.ShulkerType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class LootGui extends ParsedGui {

    private final Map<Integer, ItemStack> originalItems = new HashMap<>();

    public LootGui(@NotNull Player viewer, @NotNull FileConfiguration config, JavaPlugin plugin, ShulkerInstance instance) {
        super(viewer, config, plugin);

        lockEmptySlots(false);

        List<Integer> lootSlots = ParseUtil.parseSlots(config.getStringList("loot-slots"));

        if (instance.getSharedLootInventory() != null) {
            viewer.openInventory(instance.getSharedLootInventory());
            return;
        }

        ShulkerType shulker = instance.getType();

        for (ItemStack item : instance.getLoot()) {
            if (lootSlots.isEmpty()) break;
            int slot = Randomizer.rand(lootSlots);
            lootSlots.remove(Integer.valueOf(slot));

            if (shulker.isMask() && !shulker.maskMap().isEmpty()) {
                originalItems.put(slot, item);
                ItemStack fakeItem = ShulkerManager.applyMask(shulker, item);
                getInventory().setItem(slot, ShulkerManager.applyMask(shulker, fakeItem));
            } else {
                getInventory().setItem(slot, item);
            }
        }

        instance.setSharedLootInventory(getInventory());

        Consumer<InventoryClickEvent> onClick = onClick();
        onClick(event -> {
            if (onClick != null) onClick.accept(event);
            event.setCancelled(false);

            if (!(event.getClickedInventory() == instance.getSharedLootInventory())) return;
            ItemStack item = event.getCurrentItem();
            if (item == null) return;

            Player player = (Player) event.getWhoClicked();
            int clickedSlot = event.getSlot();

            if (originalItems.containsKey(clickedSlot)) {
                if (player.hasCooldown(item.getType())) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (originalItems.containsKey(clickedSlot)) {

                ItemStack original = getOriginalItem(clickedSlot);
                if (original == null) return;
                ItemMeta meta = original.getItemMeta();
                if (meta != null) {
                    meta.getPersistentDataContainer().remove(Keys.GUI_ITEM);
                    original.setItemMeta(meta);
                }
                event.getInventory().setItem(clickedSlot, original);
                originalItems.remove(clickedSlot);

                for (ItemStack fi : getFakeItems()) {
                    if (fi != null) {
                        player.setCooldown(fi.getType(), shulker.takeCooldown());
                    }
                }
            }
        });

        onClose(event -> {
            boolean empty = true;
            for (ItemStack i : instance.getSharedLootInventory().getContents()) {
                if (i != null) {
                    empty = false;
                    break;
                }
            }
            if (empty) instance.setSharedLootInventory(null);
        });

        open(viewer);
    }


    @Nullable
    private ItemStack getOriginalItem(int slot) {
        return originalItems.get(slot);
    }

    private List<ItemStack> getFakeItems() {
        List<ItemStack> fakes = new ArrayList<>();
        for (ItemStack item : getInventory().getContents()) {
            if (item != null && item.hasItemMeta() &&
                    item.getItemMeta().getPersistentDataContainer().has(TreexCastle.ITEM_KEY)) {
                fakes.add(item);
            }
        }
        return fakes;
    }

}