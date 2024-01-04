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

import lol.hyper.timebar.WorldTimeTracker;
import lol.hyper.timebar.papi.PlaceholderUtil;
import me.casperge.realisticseasons.api.SeasonsAPI;
import me.casperge.realisticseasons.calendar.Date;
import me.casperge.realisticseasons.season.Season;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class RealisticSeasonsTask extends BukkitRunnable {

    private final WorldTimeTracker worldTimeTracker;
    private final SeasonsAPI seasonsAPI;
    private final World world;

    private DateTimeFormatter dateFormatter;

    public RealisticSeasonsTask(WorldTimeTracker worldTimeTracker) {
        this.worldTimeTracker = worldTimeTracker;
        this.seasonsAPI = SeasonsAPI.getInstance();
        world = worldTimeTracker.getMainWorld();

        String dateFormat = worldTimeTracker.timeBar.realisticSeasonsConfig.getString("date-format");
        // if these config values are missing, use the default ones
        if (dateFormat == null) {
            dateFormat = "M/dd/yyyy";
            worldTimeTracker.timeBar.logger.warning("date-format is missing! Using default American English format.");
        }

        // test the date format
        try {
            dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
        } catch (NullPointerException | IllegalArgumentException exception) {
            worldTimeTracker.timeBar.logger.warning("date-format is NOT a valid format! Using default American English format.");
            exception.printStackTrace();
            dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        }
    }

    @Override
    public void run() {
        // get the current season & date of the world
        Season currentSeason = this.seasonsAPI.getSeason(world);
        Date currentDate = this.seasonsAPI.getDate(world);
        // this should be null if RealisticSeasons hasn't bet setup yet
        if (currentDate == null) {
            worldTimeTracker.timeBar.logger.severe("Cannot retrieve date from RealisticSeasons!");
            worldTimeTracker.timeBar.logger.severe("Most likely, you have not setup RealisticSeasons in the defined world: " + world.getName());
            worldTimeTracker.timeBar.logger.severe("Enter the world and type '/rs set <season>' to setup the world.");
            worldTimeTracker.timeBar.logger.severe("After you setup the season, you can run '/timebar reload'");
            this.cancel();
            return;
        }
        // get the current time
        String month = Month.of(currentDate.getMonth()).toString();
        String hours = String.valueOf(this.seasonsAPI.getHours(world));
        String minutes = String.valueOf(this.seasonsAPI.getMinutes(world));
        int seconds = this.seasonsAPI.getSeconds(world);

        // the RealisticSeasons API doesn't return single digit numbers
        // with zeros at the start, so we must correct it
        // Example: it returns 4, not 04
        if (hours.length() == 1) {
            hours = "0" + hours;
        }
        if (minutes.length() == 1) {
            minutes = "0" + minutes;
        }
        // format the time correctly
        String timeString = hours + ":" + minutes;
        LocalTime currentWorldTime = LocalTime.parse(timeString);

        // get the title to display for the bossbar
        String title = parseString(world, timeString, getTimeOfDay(month, currentWorldTime), currentSeason, currentDate);
        // set the progress
        int currentSeconds = (Integer.parseInt(hours) * 3600) + (Integer.parseInt(minutes) * 60) + seconds;
        float progress = (float) (currentSeconds / 86400.0);

        // loop through all bossbars and format the title
        for (Map.Entry<Player, BossBar> entry : worldTimeTracker.bossBars.entrySet()) {
            Player player = entry.getKey();
            BossBar bossBar = entry.getValue();
            // format if PAPI is detected
            if (worldTimeTracker.timeBar.papiSupport) {
                String formattedTitle = PlaceholderUtil.format(player, title);
                bossBar.name(worldTimeTracker.timeBar.miniMessage.deserialize(formattedTitle));
            } else {
                bossBar.name(worldTimeTracker.timeBar.miniMessage.deserialize(title));
            }
            bossBar.progress(progress);
            bossBar.color(worldTimeTracker.timeBar.bossBarColor);
        }
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
        ConfigurationSection monthSection = worldTimeTracker.timeBar.realisticSeasonsConfig.getConfigurationSection("month." + monthLowerCase);
        if (monthSection == null) {
            worldTimeTracker.timeBar.logger.severe("Section " + "month." + monthLowerCase + " does NOT EXIST!");
            return "INVALID";
        }

        //dawn
        String dawn = monthSection.getString("dawn");
        if (dawn == null) {
            worldTimeTracker.timeBar.logger.severe("month." + monthLowerCase + ".dawn is NOT SET!");
            return "INVALID";
        }
        LocalTime dawnTime = LocalTime.parse(dawn);

        //morning
        String morning = monthSection.getString("morning");
        if (morning == null) {
            worldTimeTracker.timeBar.logger.severe("month." + monthLowerCase + ".morning is NOT SET!");
            return "INVALID";
        }
        LocalTime morningTime = LocalTime.parse(morning);

        //noon
        String noon = monthSection.getString("noon");
        if (noon == null) {
            worldTimeTracker.timeBar.logger.severe("month." + monthLowerCase + ".noon is NOT SET!");
            return "INVALID";
        }
        LocalTime noonTime = LocalTime.parse(noon);

        //afternoon
        String afternoon = monthSection.getString("afternoon");
        if (afternoon == null) {
            worldTimeTracker.timeBar.logger.severe("month." + monthLowerCase + ".afternoon is NOT SET!");
            return "INVALID";
        }
        LocalTime afternoonTime = LocalTime.parse(afternoon);

        //sunset
        String sunset = monthSection.getString("sunset");
        if (sunset == null) {
            worldTimeTracker.timeBar.logger.severe("month." + monthLowerCase + ".sunset is NOT SET!");
            return "INVALID";
        }
        LocalTime sunsetTime = LocalTime.parse(sunset);

        //night
        String night = monthSection.getString("night");
        if (night == null) {
            worldTimeTracker.timeBar.logger.severe("month." + monthLowerCase + ".night is NOT SET!");
            return "INVALID";
        }
        LocalTime nightTime = LocalTime.parse(night);

        //midnight
        String midnight = monthSection.getString("midnight");
        if (midnight == null) {
            worldTimeTracker.timeBar.logger.severe("month." + monthLowerCase + ".midnight is NOT SET!");
            return "INVALID";
        }
        LocalTime midnightTime = LocalTime.parse(midnight);

        // time is midnight
        if ((currentWorldTime.isAfter(midnightTime) || currentWorldTime.equals(midnightTime)) && currentWorldTime.isBefore(dawnTime)) {
            return worldTimeTracker.timeBar.realisticSeasonsConfig.getString("times.midnight");
        }
        // time is dawn
        if ((currentWorldTime.isAfter(dawnTime) || currentWorldTime.equals(dawnTime)) && currentWorldTime.isBefore(morningTime)) {
            return worldTimeTracker.timeBar.realisticSeasonsConfig.getString("times.dawn");
        }
        // time is morning
        if ((currentWorldTime.isAfter(morningTime) || currentWorldTime.equals(morningTime)) && currentWorldTime.isBefore(noonTime)) {
            return worldTimeTracker.timeBar.realisticSeasonsConfig.getString("times.morning");
        }
        // time is noon
        if ((currentWorldTime.isAfter(noonTime) || currentWorldTime.equals(noonTime)) && currentWorldTime.isBefore(afternoonTime)) {
            return worldTimeTracker.timeBar.realisticSeasonsConfig.getString("times.noon");
        }
        // time is afternoon
        if ((currentWorldTime.isAfter(afternoonTime) || currentWorldTime.equals(afternoonTime)) && currentWorldTime.isBefore(sunsetTime)) {
            return worldTimeTracker.timeBar.realisticSeasonsConfig.getString("times.afternoon");
        }
        // time is sunset
        if ((currentWorldTime.isAfter(sunsetTime) || currentWorldTime.equals(sunsetTime)) && currentWorldTime.isBefore(nightTime)) {
            return worldTimeTracker.timeBar.realisticSeasonsConfig.getString("times.sunset");
        }
        // time is night
        if ((currentWorldTime.isAfter(nightTime) || currentWorldTime.equals(nightTime))) {
            return worldTimeTracker.timeBar.realisticSeasonsConfig.getString("times.night");
        }

        worldTimeTracker.timeBar.logger.severe("Unable to find suitable time for " + currentWorldTime);
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
    private String parseString(World world, String time, String timeOfDay, Season season, Date date) {
        String title = worldTimeTracker.timeBar.realisticSeasonsConfig.getString("timebar-title");
        if (title == null) {
            worldTimeTracker.timeBar.logger.severe("timebar-title is not set! Using default.");
            title = "{TIME} - {TIME-WORD} ({DATE}) - {SEASON}";
        }
        String worldTime;
        if (!worldTimeTracker.timeBar.realisticSeasonsConfig.getBoolean("use-24h-format")) {
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
        LocalDate convertedDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
        if (title.contains("{DATE}")) {
            String newDate = convertedDate.format(dateFormatter);
            title = title.replace("{DATE}", newDate);
        }
        if (title.contains("{DAY}")) {
            String writtenDay = seasonsAPI.getDayOfWeek(world);
            if (writtenDay.equals("DISABLED")) {
                title = title.replace("{DAY}", "DISABLED");
            } else {
                title = title.replace("{DAY}", writtenDay);
            }
        }
        if (title.contains("{MONTH}")) {
            String writtenMonth = seasonsAPI.getCurrentMonthName(world);
            String configMonth = worldTimeTracker.timeBar.realisticSeasonsConfig.getString("month." + writtenMonth.toLowerCase(Locale.ROOT) + ".name");
            if (writtenMonth.equals("DISABLED")) {
                title = title.replace("{MONTH}", "DISABLED");
            } else if (configMonth != null) {
                title = title.replace("{MONTH}", configMonth);
            } else {
                title = title.replace("{MONTH}", "INVALID");
            }
        }
        return title;
    }
}
