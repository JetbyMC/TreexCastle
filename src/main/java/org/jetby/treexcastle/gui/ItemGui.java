package org.jetby.treexcastle.gui;

import org.jetby.libb.gui.AdvancedGui;
import org.jetby.libb.gui.item.ItemWrapper;
import org.jetby.treexcastle.TreexCastle;
import org.jetby.treexcastle.configuration.ItemsConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static org.jetby.treexcastle.gui.MainGui.CHANCE;

public class ItemGui extends AdvancedGui {

    private final ItemsConfiguration items;

    public ItemGui(Player player, String inv, String type, TreexCastle plugin) {
        super(plugin.getFormattedMessage("gui.items.title"), 54);
        this.items = plugin.getItems();


        lockEmptySlots(false);
        onDrag(inventoryDragEvent -> {
            inventoryDragEvent.setCancelled(false);
        });

        List<ItemsConfiguration.ItemsData> map = items.getData().get(type);
        for (ItemsConfiguration.ItemsData itemData : map) {
            if (!itemData.inv().equals(inv)) continue;
            if (itemData.itemStack() == null) continue;
            ItemWrapper wrapper = new ItemWrapper(itemData.itemStack());
            wrapper.slots(itemData.slot());
            wrapper.onClick(event -> {
                event.setCancelled(false);
            });
            setItem(itemData.slot().toString() + "-" + itemData.inv(),
                    wrapper);
        }

        onClose(event -> {
            saveInv(event.getInventory(), type, inv);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                new InvGui(plugin, player, plugin.getTypes().getShulkers().get(type)).open(player);
            }, 1L);
        });
    }

    private void saveInv(Inventory inventory, String type, String inv) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null) {
                items.removeItem(type, inv, slot);
                continue;
            }
            int chance = item.getItemMeta().getPersistentDataContainer()
                    .getOrDefault(CHANCE, PersistentDataType.INTEGER, 100);
            items.saveItem(type, inv, item, slot, chance);
        }
    }
}