/*
 * This file is part of TimeBar.
 *
 * TimeBar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TimeBar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TimeBar.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.timebar.timers;

import lol.hyper.timebar.TimeBar;
import me.casperge.realisticseasons.calendar.Date;
import me.casperge.realisticseasons.season.Season;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RealisticSeasonsTask extends BukkitRunnable {

    private final TimeBar timeBar;

    public RealisticSeasonsTask(TimeBar timeBar) {
        this.timeBar = timeBar;
    }

    @Override
    public void run() {
        World world = Bukkit.getWorld(timeBar.worldName);
        if (world == null) {
            this.timeBar.logger.severe(timeBar.worldName + " is not a valid world!");
            this.cancel();
            return;
        }
        Season currentSeason = timeBar.seasonsAPI.getSeason(world);
        Date date = timeBar.seasonsAPI.getDate(world);
        String month = Month.of(date.getMonth()).toString();
        String hours = String.valueOf(timeBar.seasonsAPI.getHours(world));
        String minutes = String.valueOf(timeBar.seasonsAPI.getMinutes(world));
        int seconds = timeBar.seasonsAPI.getSeconds(world);
        if (hours.length() == 1) {
            hours = "0" + hours;
        }
        if (minutes.length() == 1) {
            minutes = "0" + minutes;
        }
        String timeString = hours + ":" + minutes;
        LocalTime currentWorldTime = LocalTime.parse(timeString);

        Component title = parseString(world, timeString, getTimeOfDay(month, currentWorldTime), currentSeason, date);
        timeBar.timeTracker.name(title);
        int currentSeconds = (Integer.parseInt(hours) * 3600) + (Integer.parseInt(minutes) * 60) + seconds;
        timeBar.timeTracker.progress((float) (currentSeconds / 86400.0));
    }

    /**
     * Gets the "time of day" based on time.
     *
     * @param month            The current month.
     * @param currentWorldTime The current time.
     * @return The time of day.
     */
    private String getTimeOfDay(String month, LocalTime currentWorldTime) {
        String monthLowerCase = month.toLowerCase(Locale.ROOT);
        ConfigurationSection seasonSection = timeBar.realisticSeasonsConfig.getConfigurationSection("month." + monthLowerCase);
        if (seasonSection == null) {
            timeBar.logger.severe("Section " + "month." + monthLowerCase + " does NOT EXIST!");
            return "INVALID";
        }

        //dawn
        String dawn = seasonSection.getString("dawn");
        if (dawn == null) {
            timeBar.logger.severe("month." + monthLowerCase + ".dawn is NOT SET!");
            return "INVALID";
        }
        LocalTime dawnTime = LocalTime.parse(dawn);

        //morning
        String morning = seasonSection.getString("morning");
        if (morning == null) {
            timeBar.logger.severe("month." + monthLowerCase + ".morning is NOT SET!");
            return "INVALID";
        }
        LocalTime morningTime = LocalTime.parse(morning);

        //noon
        String noon = seasonSection.getString("noon");
        if (noon == null) {
            timeBar.logger.severe("month." + monthLowerCase + ".noon is NOT SET!");
            return "INVALID";
        }
        LocalTime noonTime = LocalTime.parse(noon);

        //afternoon
        String afternoon = seasonSection.getString("afternoon");
        if (afternoon == null) {
            timeBar.logger.severe("month." + monthLowerCase + ".afternoon is NOT SET!");
            return "INVALID";
        }
        LocalTime afternoonTime = LocalTime.parse(afternoon);

        //sunset
        String sunset = seasonSection.getString("sunset");
        if (sunset == null) {
            timeBar.logger.severe("month." + monthLowerCase + ".sunset is NOT SET!");
            return "INVALID";
        }
        LocalTime sunsetTime = LocalTime.parse(sunset);

        //night
        String night = seasonSection.getString("night");
        if (night == null) {
            timeBar.logger.severe("month." + monthLowerCase + ".night is NOT SET!");
            return "INVALID";
        }
        LocalTime nightTime = LocalTime.parse(night);

        // night before dawn
        if (currentWorldTime.isBefore(dawnTime)) {
            return timeBar.realisticSeasonsConfig.getString("times.night");
        }
        // time is dawn
        if ((currentWorldTime.isAfter(dawnTime) || currentWorldTime.equals(dawnTime)) && currentWorldTime.isBefore(morningTime)) {
            return timeBar.realisticSeasonsConfig.getString("times.dawn");
        }
        // time is morning
        if ((currentWorldTime.isAfter(morningTime) || currentWorldTime.equals(morningTime)) && currentWorldTime.isBefore(noonTime)) {
            return timeBar.realisticSeasonsConfig.getString("times.morning");
        }
        // time is noon
        if ((currentWorldTime.isAfter(noonTime) || currentWorldTime.equals(noonTime)) && currentWorldTime.isBefore(afternoonTime)) {
            return timeBar.realisticSeasonsConfig.getString("times.noon");
        }
        // time is afternoon
        if ((currentWorldTime.isAfter(afternoonTime) || currentWorldTime.equals(afternoonTime)) && currentWorldTime.isBefore(sunsetTime)) {
            return timeBar.realisticSeasonsConfig.getString("times.afternoon");
        }
        // time is sunset
        if ((currentWorldTime.isAfter(sunsetTime) || currentWorldTime.equals(sunsetTime)) && currentWorldTime.isBefore(nightTime)) {
            return timeBar.realisticSeasonsConfig.getString("times.sunset");
        }
        // time is night
        if (currentWorldTime.isAfter(nightTime) || currentWorldTime.equals(nightTime)) {
            return timeBar.realisticSeasonsConfig.getString("times.night");
        }
        timeBar.logger.severe("Unable to find suitable time for " + currentWorldTime);
        return "INVALID";
    }

    /**
     * Parses the title, which formats any placeholders.
     *
     * @param world     The world.
     * @param time      The current time string.
     * @param timeOfDay The "time of day" word.
     * @param season    The current season.
     * @return Formatted title.
     */
    private Component parseString(World world, String time, String timeOfDay, Season season, Date date) {
        String title = timeBar.realisticSeasonsConfig.getString("timebar-title");
        if (title == null) {
            timeBar.logger.severe("timebar-title is not set! Using default.");
            title = "{TIME} - {TIME-WORD} ({DATE}) - {SEASON}";
        }
        String worldTime;
        if (timeBar.realisticSeasonsConfig.getBoolean("use-24h-format")) {
            worldTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("hh:mm a"));
        } else {
            worldTime = time;
        }

        if (title.contains("{TIME}")) {
            title = title.replace("{TIME}", worldTime);
        }

        if (title.contains("{TIME-WORD}")) {
            title = title.replace("{TIME-WORD}", timeOfDay);
        }

        if (title.contains("{DAYCOUNT}")) {
            title = title.replace("{DAYCOUNT}", String.valueOf(world.getFullTime() / 24000));
        }

        if (title.contains("{SEASON}")) {
            title = title.replace("{SEASON}", season.toString());
        }

        if (title.contains("{DATE}")) {
            title = title.replace("{DATE}", date.toString(true));
        }
        return timeBar.miniMessage.deserialize(title);
    }
}
