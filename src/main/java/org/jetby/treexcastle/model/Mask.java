package org.jetby.treexcastle.model;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public record Mask(
        Material material,
        Component name,
        boolean enchanted
) {
}