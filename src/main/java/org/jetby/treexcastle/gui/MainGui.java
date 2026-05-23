package org.jetby.treexcastle.gui;

import org.jetby.libb.gui.AdvancedGui;
import org.jetby.libb.gui.item.ItemWrapper;
import org.jetby.treexcastle.TreexCastle;
import org.jetby.treexcastle.shulker.ShulkerType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class MainGui extends AdvancedGui {
    public static final NamespacedKey CHANCE = new NamespacedKey("treexcastle", "chance");

    public MainGui(TreexCastle plugin) {

        super(plugin.getFormattedMessage("gui.main.title"), 54);

        lockEmptySlots(true);

        int slot = 0;
        for (String type : plugin.getTypes().getShulkers().keySet()) {
            ShulkerType shulker = plugin.getTypes().getShulkers().get(type);
            int finalSlot = slot;

            setItem(type, ItemWrapper.builder(shulker.material())
                    .displayName(plugin.getFormattedMessage("gui.main.type_button.display_name", "{type}", type))
                    .lore(plugin.getFormattedMessageList("gui.main.type_button.lore")
                    )
                    .slots(finalSlot)
                    .onClick(event -> {
                        event.setCancelled(true);

                        if (event.getWhoClicked() instanceof Player) {
                            new InvGui(plugin, player, shulker).open(player);
                        }
                    })
                    .build());
            slot++;
        }
    }
}
