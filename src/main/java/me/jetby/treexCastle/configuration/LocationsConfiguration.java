package me.jetby.treexCastle.configuration;

import lombok.RequiredArgsConstructor;
import me.jetby.libb.util.Randomizer;
import me.jetby.treexCastle.TreexCastle;
import me.jetby.treexCastle.util.LocationHandler;
import me.jetby.treexCastle.util.Logger;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class LocationsConfiguration {

    private final TreexCastle plugin;
    private final File file;
    private final FileConfiguration configuration;

    private final Set<Location> locations = new HashSet<>();
    private final Set<Location> occupied = new HashSet<>();

    public List<Location> getLocations() {
        return locations.stream().toList();
    }

    public void load() {
        locations.clear();
        List<Location> locations = configuration.getStringList("locations").stream()
                .map(str -> LocationHandler.deserialize(str, plugin))
                .toList();

        this.locations.addAll(locations);
    }

    public void save() {
        try {
            configuration.set("locations", null);
            configuration.set("locations", locations.stream().map(LocationHandler::serialize).toList());
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public Location acquireRandomAvailableLocation() {
        List<Location> available = new ArrayList<>();
        for (Location loc : locations) {
            if (!occupied.contains(loc)) available.add(loc);
        }

        if (available.isEmpty()) return null;

        Location randomLoc = available.get(Randomizer.rand(available.size()));
        if (randomLoc != null) {
            occupied.add(randomLoc);
        }
        return randomLoc;
    }

    public void acquire(Location location) {
        if (location == null) return;

        if (!locations.contains(location)) return;
        if (occupied.contains(location)) return;

        occupied.add(location);
    }

    public void reset(Location location) {
        if (location == null) return;
        occupied.remove(location);
    }

    public void addLocation(Location location) {
        if (location == null) return;
        boolean added = locations.add(location);
        if (added) save();
    }

    public boolean removeLocation(Location location) {
        if (location == null) return false;
        boolean removed = locations.remove(location);
        if (removed) {
            occupied.remove(location);
            save();
        }
        return removed;
    }

    public boolean isOccupied(Location location) {
        if (location == null) return false;
        return occupied.contains(location);
    }

}
