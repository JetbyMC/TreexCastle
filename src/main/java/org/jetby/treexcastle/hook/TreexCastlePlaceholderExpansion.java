package org.jetby.treexcastle.hook;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetby.treexcastle.TreexCastle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class TreexCastlePlaceholderExpansion extends PlaceholderExpansion {
    private final TreexCastle plugin;

    @Override
    public @NotNull String getIdentifier() {
        return "treexcastle";
    }

    @Override
    public @NotNull String getAuthor() {
        return "";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        if (identifier.equalsIgnoreCase("time_to_start")) {
            return String.valueOf(plugin.getShulkerManager().getTimeToStart());
        }
        if (identifier.equalsIgnoreCase("time_to_start_string")) {
            return (plugin.getFormatTime().stringFormat(plugin.getShulkerManager().getTimeToStart()));
        }
        if (identifier.equalsIgnoreCase("time_to_start_format")) {
            return (plugin.getFormatTime().stringFormat(plugin.getShulkerManager().getTimeToStart()));
        }
        return identifier;
    }

}
