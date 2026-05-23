package org.jetby.treexcastle.configuration;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetby.libb.util.Logger;
import org.jetby.treexcastle.TreexCastle;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ItemsConfiguration {

    public record ItemsData(String inv, Integer slot, ItemStack itemStack, Integer chance) {
    }

    private final File file;
    private final FileConfiguration configuration;

    public ItemsConfiguration(File file) {
        this.file = file;
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    @Getter
    private final Map<String, List<ItemsData>> data = new HashMap<>();

    public void load() {
        data.clear();
        ConfigurationSection vulcans = configuration.getConfigurationSection("Items");

        if (vulcans == null) return;

        for (String type : vulcans.getKeys(false)) {
            ConfigurationSection invSection = vulcans.getConfigurationSection(type);
            if (invSection == null) continue;
            List<ItemsData> list = new ArrayList<>();
            for (String inv : invSection.getKeys(false)) {
                ConfigurationSection itemsSection = invSection.getConfigurationSection(inv);
                if (itemsSection == null) continue;
                for (String itemSlot : itemsSection.getKeys(false)) {
                    ConfigurationSection item = itemsSection.getConfigurationSection(itemSlot);
                    if (item == null) continue;
                    list.add(new ItemsData(inv,
                            Integer.parseInt(itemSlot),
                            item.getItemStack("item"),
                            item.getInt("chance")));
                }
            }

            data.put(type, list);
        }
    }

    public void removeItem(String type, String inv, Integer slot) {
        List<ItemsData> items = data.get(type);
        if (items == null) return;
        items.removeIf(itemsData ->
                inv.equalsIgnoreCase(itemsData.inv()) &&
                        Objects.equals(itemsData.slot(), slot)
        );
    }

    public void saveItem(String type, String inv, ItemStack itemStack, int slot, int chance) {
        List<ItemsData> items = data.computeIfAbsent(type, k -> new ArrayList<>());

        items.removeIf(itemsData ->
                inv.equals(itemsData.inv()) &&
                        itemsData.slot() == slot
        );

        if (itemStack != null) {
            items.add(new ItemsData(inv, slot, itemStack, chance));
        }
    }

    public void save() {

        for (String type : data.keySet()) {
            List<ItemsData> items = data.get(type);
            configuration.set("Items." + type, null);
            for (ItemsData itemData : items) {
                Integer slot = itemData.slot();
                ItemStack item = itemData.itemStack();
                Integer chance = itemData.chance();

                configuration.set("Items." + type + "." + itemData.inv() + "." + slot + ".item", item);
                configuration.set("Items." + type + "." + itemData.inv() + "." + slot + ".chance", chance);
            }
        }
        try {
            configuration.save(file);
        } catch (IOException e) {
            Logger.warn(TreexCastle.INSTANCE, "Failed to save file (items.yml)\n" + e);
        }
    }
}
