package org.jetby.treexcastle;

import lombok.Getter;
import org.jetby.libb.action.ActionRegistry;
import org.jetby.libb.color.Serializer;
import org.jetby.libb.util.Logger;
import org.jetby.libb.util.VersionUtil;
import org.jetby.treexcastle.configuration.*;
import org.jetby.treexcastle.gui.MainGui;
import org.jetby.treexcastle.handler.WandHandler;
import org.jetby.treexcastle.hook.TreexCastlePlaceholderExpansion;
import org.jetby.treexcastle.shulker.ShulkerHandler;
import org.jetby.treexcastle.shulker.ShulkerInstance;
import org.jetby.treexcastle.shulker.ShulkerManager;
import org.jetby.treexcastle.util.FormatTime;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetby.libb.util.Metrics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public final class TreexCastle extends JavaPlugin {

    private ConfigConfiguration cfg;
    private ItemsConfiguration items;
    private TypesConfiguration types;
    private MenusConfiguration menus;
    private ShulkerManager shulkerManager;
    private LocationsConfiguration locations;

    private MainGui mainGui;
    private FormatTime formatTime;
    private TreexCastlePlaceholderExpansion castlePlaceholders;

    public static final NamespacedKey WAND_KEY = new NamespacedKey("treexcastle", "wand");
    public static final NamespacedKey ITEM_KEY = new NamespacedKey("treexcastle", "flying_item");

    public static TreexCastle INSTANCE;


    @Override
    public void onEnable() {
        INSTANCE = this;

        new Metrics(this, 24879);

        messages = getFileConfiguration("messages.yml");

        saveDefaultConfig();

        cfg = new ConfigConfiguration(this, getConfig());
        cfg.load();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            locations = new LocationsConfiguration(this, getFile("locations.yml"), getFileConfiguration("locations.yml"));
            locations.load();
        }, 1L);

        formatTime = new FormatTime(this);

        items = new ItemsConfiguration(getFile("items.yml"));
        items.load();

        types = new TypesConfiguration(this, new File(getDataFolder().getAbsolutePath(), "types"));
        types.load();

        shulkerManager = new ShulkerManager(this);
        shulkerManager.runTimer();

        menus = new MenusConfiguration(this);
        menus.loadGuis();
        mainGui = new MainGui(this);

        new ShulkerCommand(this).register();

        new ShulkerHandler(this);
        new WandHandler(this);

        if (cfg.isUpdateChecker()) {
            new VersionUtil(this, getDescription().getVersion(), "https://raw.githubusercontent.com/MrJetby/TreexCastle/refs/heads/master/VERSION", "treexcastle.update");
        }

        if (getServer().getPluginManager().getPlugin("DecentHolograms") == null) {
            Logger.error(this,"DecentHolograms was not found; the plugin cannot function without it!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            castlePlaceholders = new TreexCastlePlaceholderExpansion(this);
            castlePlaceholders.register();
            Logger.info(this, "Placeholders successfully registered.");
        } else {
            Logger.error(this,"PlaceholderAPI was not found, so placeholders will not be available!");
        }

        ActionRegistry.register("treexcastle", "explosion", (ctx, input) -> {
            ShulkerInstance instance = ctx.get(ShulkerInstance.class);
            String[] parts = input.rawText().split(" ");
            float power = 1;
            boolean fire = false;
            boolean breakBlocks = false;
            try {power = Float.parseFloat(parts[0]);} catch (Exception ignored) {}
            try {fire = Boolean.parseBoolean(parts[1]);} catch (Exception ignored) {}
            try {breakBlocks = Boolean.parseBoolean(parts[2]);} catch (Exception ignored) {}

            instance.getLocation().getWorld().createExplosion(instance.getLocation(), power, fire, breakBlocks);

        });

    }

    @Override
    public void onDisable() {
        ActionRegistry.unregisterAll("treexcastle");
        shulkerManager.removeAllClones();
        items.save();
        locations.save();
        if (castlePlaceholders != null) {
            castlePlaceholders.unregister();
        }
    }

    public static Component r(String string) {
        return Serializer.MINI_MESSAGE.deserialize("<!i>"+string);
    }

    private FileConfiguration messages;

    public Component getFormattedMessage(String path) {
        return r(messages.getString(path));
    }
    public List<Component> getFormattedMessageList(String path) {
        List<Component> components = new ArrayList<>();
        for (String str : messages.getStringList(path)) {
            components.add(r(str));
        }
        return components;
    }
    public Component getFormattedMessage(String path, String target, String replace) {
        return r(messages.getString(path, "").replace(target, replace));
    }

    public FileConfiguration getFileConfiguration(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) saveResource(fileName, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    public File getFile(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) saveResource(fileName, false);
        return file;
    }
}
