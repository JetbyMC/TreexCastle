package org.jetby.treexcastle.util;

import org.jetby.treexcastle.TreexCastle;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationHandler {
    public static String serialize(Location loc) {
        return String.format("%d;%d;%d;%s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());

    }

    public static Location deserialize(String str, TreexCastle plugin) {
        if (str == null || str.isEmpty() || str.equals("0;0;0;world")) {
            return null;
        }

        try {
            String[] parts = str.split(";");
            if (parts.length < 4) return null;

            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            World world = plugin.getServer().getWorld(parts[3]);

            return world != null ? new Location(world, x, y, z) : null;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
