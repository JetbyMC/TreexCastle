package me.jetby.treexCastle.gui;

import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.treexCastle.TreexCastle;
import me.jetby.treexCastle.configuration.ItemsConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.jetby.treexCastle.TreexCastle.r;
import static me.jetby.treexCastle.gui.MainGui.CHANCE;

public class ChanceGui extends AdvancedGui {
    private final ItemsConfiguration items;
    private final String type;
    private final String inv;
    private final Map<Integer, ItemStack> originalItems = new HashMap<>();


    public ChanceGui(Player player, String type, String inv, TreexCastle plugin) {
        super(plugin.getFormattedMessage("gui.chance.title"), 54);
        this.type = type;
        this.inv = inv;
        this.items = plugin.getItems();

        lockEmptySlots(true);

        List<ItemsConfiguration.ItemsData> itemMap = items.getData().get(type);
        for (ItemsConfiguration.ItemsData itemData : itemMap) {
            if (!itemData.inv().equals(inv)) continue;
            if (itemData.itemStack() == null) continue;

            ItemStack item = itemData.itemStack().clone();
            originalItems.put(itemData.slot(), item);

            final int[] chance = {item.getItemMeta().getPersistentDataContainer().getOrDefault(CHANCE, PersistentDataType.INTEGER, 100)};

            ItemWrapper wrapper = new ItemWrapper(item.getType());

            wrapper.amount(item.getAmount());
            wrapper.slots(itemData.slot());
            wrapper.displayName(plugin.getFormattedMessage("gui.chance.item.display_name", "{chance}", String.valueOf(chance[0])));
            wrapper.displayName(r("<#FB430A><bold>⭐ <white>Шанс: <gold>" + chance[0] + "%"));
            wrapper.lore(plugin.getFormattedMessageList("gui.chance.item.lore"));
            wrapper.onClick(event -> {
                event.setCancelled(true);
                ClickType click = event.getClick();

                if (click == ClickType.LEFT) chance[0] += 1;
                else if (click == ClickType.RIGHT) chance[0] -= 1;
                else if (click == ClickType.SHIFT_LEFT) chance[0] += 10;
                else if (click == ClickType.SHIFT_RIGHT) chance[0] -= 10;

                chance[0] = Math.max(0, Math.min(100, chance[0]));

                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(CHANCE, PersistentDataType.INTEGER, chance[0]);
                item.setItemMeta(meta);

                wrapper.displayName(plugin.getFormattedMessage("gui.chance.item.display_name", "{chance}", String.valueOf(chance[0])));
                updateItem("slot_" + itemData.slot());
            });

            setItem("slot_" + itemData.slot(), wrapper);
        }

        onClose(event -> {
            saveChanges();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                new InvGui(plugin, player, plugin.getTypes().getShulkers().get(type)).open(player);
            }, 1L);
        });
    }

    private void saveChanges() {
        for (Map.Entry<Integer, ItemStack> entry : originalItems.entrySet()) {
            int slot = entry.getKey();
            ItemStack item = entry.getValue();
            int chance = item.getItemMeta().getPersistentDataContainer().getOrDefault(CHANCE, PersistentDataType.INTEGER, 100);
            items.saveItem(type, inv, item, slot, chance);
        }
    }
}