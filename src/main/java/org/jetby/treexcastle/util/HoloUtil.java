package org.jetby.treexcastle.util;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@UtilityClass
public class HoloUtil {

    public void create(@NotNull List<String> lines,
                       @NotNull Location location,
                       @NotNull String name) {

        Hologram hologram = DHAPI.getHologram(name);
        if (hologram == null) {
            DHAPI.createHologram(name, location, lines);
        }
    }

    public void update(@NotNull List<String> lines,
                       @NotNull Location location,
                       @NotNull String name) {

        Hologram hologram = DHAPI.getHologram(name);
        if (hologram != null) {
            if (!hologram.getLocation().equals(location)) {
                hologram.setLocation(location);
                hologram.realignLines();
            }
            DHAPI.setHologramLines(hologram, lines);
        }
    }

    public void remove(@NotNull String name) {

        Hologram hologram = DHAPI.getHologram(name);
        if (hologram != null)
            DHAPI.removeHologram(name);
    }
}
