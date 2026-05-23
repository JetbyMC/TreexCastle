package org.jetby.treexcastle.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetby.treexcastle.TreexCastle;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class ConfigConfiguration {

    private final TreexCastle plugin;
    private final FileConfiguration configuration;

    private String license;
    private boolean bStats;
    private boolean updateChecker;
    private boolean update;
    private int time;
    private List<String> msg;

    private List<String> formattedTimeSeconds;
    private List<String> formattedTimeMinutes;
    private List<String> formattedTimeHours;
    private List<String> formattedTimeDays;
    private List<String> formattedTimeWeeks;
    private String formattedTimeFormat;

    public void load() {

        formattedTimeFormat = configuration.getString("formattedTime.format", "%weeks% %days% %hours% %minutes% %seconds%");

        List<String> formattedTimeSecondsDefault = new ArrayList<>(List.of("секунду", "секунды", "секунд"));
        formattedTimeSeconds = getOrDefaultList(configuration, "formattedTime.seconds", formattedTimeSecondsDefault);

        List<String> formattedTimeMinutesDefault = new ArrayList<>(List.of("минуту", "минуты", "минут"));
        formattedTimeMinutes = getOrDefaultList(configuration, "formattedTime.minutes", formattedTimeMinutesDefault);

        List<String> formattedTimeHoursDefault = new ArrayList<>(List.of("час", "часа", "часов"));
        formattedTimeHours = getOrDefaultList(configuration, "formattedTime.hours", formattedTimeHoursDefault);

        List<String> formattedTimeDaysDefault = new ArrayList<>(List.of("день", "дня", "дней"));
        formattedTimeDays = getOrDefaultList(configuration, "formattedTime.days", formattedTimeDaysDefault);

        List<String> formattedTimeWeeksDefault = new ArrayList<>(List.of("неделю", "недели", "недель"));
        formattedTimeWeeks = getOrDefaultList(configuration, "formattedTime.weeks", formattedTimeWeeksDefault);

        license = configuration.getString("license.key", "NONE");
        update = configuration.getBoolean("update.enable", false);
        time = configuration.getInt("update.time", 1800);
        msg = configuration.getStringList("update.msg");

        bStats = configuration.getBoolean("bStats", true);
        updateChecker = configuration.getBoolean("update-checker", true);
    }

    private List<String> getOrDefaultList(FileConfiguration config, String path, List<String> defaultValue) {
        List<String> list = config.getStringList(path);
        return config.getStringList(path).isEmpty() ? defaultValue : list;
    }

}
