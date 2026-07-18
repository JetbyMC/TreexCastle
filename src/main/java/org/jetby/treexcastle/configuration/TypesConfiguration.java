package org.jetby.treexcastle.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetby.libb.action.ActionUtil;
import org.jetby.libb.action.record.ActionBlock;
import org.jetby.libb.gui.parser.ParseUtil;
import org.jetby.treexcastle.TreexCastle;
import org.jetby.treexcastle.model.FlyingDropParticle;
import org.jetby.treexcastle.model.Holo;
import org.jetby.treexcastle.model.Mask;
import org.jetby.treexcastle.shulker.ShulkerType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
public class TypesConfiguration {

    private final TreexCastle plugin;
    private final File file;

    @Getter
    private final Map<String, ShulkerType> shulkers = new HashMap<>();

    public void load() {

        if (!file.exists()) {
            if (file.mkdirs()) {
                File defaultFile = new File(file, "default.yml");
                if (!defaultFile.exists()) {
                    plugin.saveResource("types/default.yml", false);
                }
                FileConfiguration config = YamlConfiguration.loadConfiguration(defaultFile);
                loadType(config, config.getString("id", defaultFile.getName().replace(".yml", "")));
            }
            return;
        }

        File[] files = file.listFiles();
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            loadType(config, config.getString("id"));
        }
    }

    public void loadType(FileConfiguration config, String id) {

        ConfigurationSection section = config.getConfigurationSection("settings");

        Material material = Material.valueOf(section.getString("type", "STONE"));
        int durability = section.getInt("durability", 50);
        LootDelivery lootDelivery = LootDelivery.valueOf(section.getString("loot_delivery", "DROP").toUpperCase());
        String deliveryGui = section.getString("delivery_gui", "default_loot_menu");
        String lootAmount = section.getString("lootAmount", "1");
        int spawnChance = section.getInt("spawnChance", 50);
        int removeAfter = section.getInt("remove-after", 0);

        ConfigurationSection maskSection = config.getConfigurationSection("mask");
        boolean isMask = maskSection.getBoolean("enable", false);
        int takeCooldown = maskSection.getInt("take-cooldown", 0);

        Map<String, Mask> maskMap = loadMask(maskSection.getConfigurationSection("items"));

        FlyingDropParticle dropParticle = loadDropParticle(config.getConfigurationSection("flying-drop-particle"));

        ActionBlock onSpawn = ActionUtil.getActionBlock(config, "on_spawn");
        ActionBlock onBreak = ActionUtil.getActionBlock(config, "on_break");
        ActionBlock onDespawn = ActionUtil.getActionBlock(config, "on_despawn");

        Holo holo = new Holo(
                section.getBoolean("hologram.enable", false),
                section.getDouble("hologram.x", 0.5),
                section.getDouble("hologram.y", 2.0),
                section.getDouble("hologram.z", 0.5),
                section.getStringList("hologram.lines"
                ));

        Holo holoRemove = new Holo(
                section.getBoolean("hologram-remove.enable", false),
                section.getDouble("hologram-remove.x", 0.5),
                section.getDouble("hologram-remove.y", 2.0),
                section.getDouble("hologram-remove.z", 0.5),
                section.getStringList("hologram-remove.lines"
                ));


        shulkers.put(id, new ShulkerType(
                id,
                material,
                durability,
                lootDelivery,
                deliveryGui,
                lootAmount,
                removeAfter,
                isMask,
                takeCooldown,
                maskMap,
                dropParticle,
                spawnChance,
                holo,
                holoRemove,
                onSpawn,
                onBreak,
                onDespawn
        ));
    }

    private Map<String, Mask> loadMask(ConfigurationSection section) {
        Map<String, Mask> masks = new HashMap<>();

        if (section != null) {

            for (String maskId : section.getKeys(false)) {

                ConfigurationSection mask = section.getConfigurationSection(maskId);
                if (mask != null) {
                    Material material = Material.valueOf(mask.getString("material", "STONE"));
                    boolean enchanted = mask.getBoolean("enchanted", false);
                    Component name = TreexCastle.r(mask.getString("name", "Default item"));
                    masks.put(maskId, new Mask(material, name, enchanted));
                }
            }

        }
        return masks;
    }

    private FlyingDropParticle loadDropParticle(ConfigurationSection dropParticleSection) {
        Sound sound = Sound.ENTITY_ITEM_PICKUP;
        float soundVolume = 1;
        float soundPitch = 1;
        Particle particle = Particle.FLAME;
        int paritcleAmount = 1;
        double offsetX = 0;
        double offsetY = 0;
        double offsetZ = 0;
        double particleMinY = 5.0;
        double particleMaxY = 10.0;
        double minSpeed = 0.5;
        double maxSpeed = 1.0;
        int pickupDelay = 0;
        boolean visualFire = false;


        if (dropParticleSection != null) {
            sound = Sound.valueOf(dropParticleSection.getString("sound", "ENTITY_ITEM_PICKUP").toUpperCase());
            soundVolume = (float) dropParticleSection.getDouble("volume", 1);
            soundPitch = (float) dropParticleSection.getDouble("pitch", 1);
            paritcleAmount = dropParticleSection.getInt("amount", 1);
            offsetX = dropParticleSection.getDouble("offset-x", 0);
            offsetY = dropParticleSection.getDouble("offset-y", 0);
            offsetZ = dropParticleSection.getDouble("offset-z", 0);
            particleMinY = dropParticleSection.getDouble("min-y", 5.0);
            particleMaxY = dropParticleSection.getDouble("max-y", 10.0);
            minSpeed = dropParticleSection.getDouble("min-speed", 0.5);
            maxSpeed = dropParticleSection.getDouble("max-speed", 1.0);
            particle = Particle.valueOf(dropParticleSection.getString("particle", "FLAME"));
            pickupDelay = dropParticleSection.getInt("pickup-delay", 0);
            visualFire = dropParticleSection.getBoolean("visual-fire", false);

        }
        return new FlyingDropParticle(sound, soundVolume, soundPitch, visualFire, particle, paritcleAmount, offsetX, offsetY, offsetZ, particleMinY, particleMaxY, minSpeed, maxSpeed, pickupDelay);
    }

    public enum LootDelivery {
        DROP, GUI, FLYING
    }
}
