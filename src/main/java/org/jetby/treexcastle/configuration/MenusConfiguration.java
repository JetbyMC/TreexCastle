package org.jetby.treexcastle.configuration;

import org.jetby.libb.util.Logger;
import org.jetby.treexcastle.TreexCastle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public record MenusConfiguration(TreexCastle plugin) {
    public static final Map<String, FileConfiguration> GUI_CONFIG_MAP = new HashMap<>();

    private void loadFilesRecursive(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadFilesRecursive(file);
                continue;
            }

            if (!file.getName().endsWith(".yml")) continue;

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String id = config.getString("id", file.getName().replace(".yml", ""));

            loadGui(id, file);
        }
    }

    public void loadGuis() {
        GUI_CONFIG_MAP.clear();
        File folder = new File(plugin.getDataFolder(), "menu");

        if (!folder.exists() && folder.mkdirs()) {
            String[] defaults = {
                    "loot_menu.yml"
            };

            for (String name : defaults) {
                File target = new File(folder, name);
                target.getParentFile().mkdirs();

                if (!target.exists()) {
                    plugin.saveResource("menu/" + name, false);
                }
            }
        }

        loadFilesRecursive(folder);

        Logger.info(plugin, "&a"+GUI_CONFIG_MAP.size() + " menus has been founded");
    }

    private void loadGui(String menuId, File file) {
        if (GUI_CONFIG_MAP.containsKey(menuId)) {
            Logger.error(plugin,"A duplicate of " + menuId + " was found");
            return;
        }
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            GUI_CONFIG_MAP.put(menuId, config);
        } catch (Exception e) {
            Logger.error(plugin,"Error trying to load menu: " + e.getMessage());
        }
    }
}
