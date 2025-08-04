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

import lol.hyper.timebar.papi.PlaceholderUtil;
import lol.hyper.timebar.tracker.WorldTimeTracker;
import lol.hyper.timebar.utils.NumberFormat;
import net.advancedplugins.seasons.api.AdvancedSeasonsAPI;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Locale;
import java.util.Map;

public class AdvancedSeasonsTask extends BukkitRunnable {

    private final WorldTimeTracker worldTimeTracker;
    private final AdvancedSeasonsAPI api;
    private final World world;

    public AdvancedSeasonsTask(WorldTimeTracker worldTimeTracker) {
        this.worldTimeTracker = worldTimeTracker;
        this.world = worldTimeTracker.getMainWorld();
        this.api = new AdvancedSeasonsAPI();
    }

    @Override
    public void run() {
        // if there are no players, do not run
        if (worldTimeTracker.getBossBars().isEmpty()) {
            worldTimeTracker.stopTimer();
            return;
        }
        if (api == null) {
            worldTimeTracker.timeBar.logger.severe("Unable to hook into AdvancedSeasonsAPI!");
            worldTimeTracker.timeBar.logger.severe("Canceling AdvancedSeasonsTask, please try /timebar reload.");
            worldTimeTracker.timeBar.logger.severe("If you keep seeing this issue, please file an issue on GitHub!");
            worldTimeTracker.stopTimer();
            return;
        }

        // get the current time
        int time = (int) world.getTime();
        float progress = (float) (time / 24000.0);
        float timePercent = progress * 100;
        int dayCount = (int) (world.getFullTime() / 24000);
        String timeOfDay = getTimeOfDay(time);
        String currentSeason = api.getSeason(world);
        if (currentSeason == null) {
            worldTimeTracker.timeBar.logger.severe("Unable to read season information from world " + world.getName());
            worldTimeTracker.timeBar.logger.severe("This world is not setup with AdvancedSeasons, canceling...");
            this.cancel();
            return;
        }
        String seasonFromConfig = worldTimeTracker.timeBar.advancedSeasonsConfig.getString("seasons." + currentSeason);
        if (seasonFromConfig == null) {
            worldTimeTracker.timeBar.logger.warning("seasons." + currentSeason + " does not exist in advancedseasons.yml, using default season names...");
            seasonFromConfig = currentSeason;
        }

        // store these into the tracker
        worldTimeTracker.setDayPercent(timePercent);
        worldTimeTracker.setDayCount(dayCount);
        worldTimeTracker.setTimeOfDay(timeOfDay);

        // get the color for the season
        String colorConfig = worldTimeTracker.timeBar.advancedSeasonsConfig.getString("colors." + currentSeason.toUpperCase(Locale.ROOT));
        // load from config first
        BossBar.Color color = worldTimeTracker.timeBar.bossBarColor;
        ;
        if (colorConfig != null) {
            // see if the color is valid
            try {
                color = BossBar.Color.valueOf(colorConfig);
            } catch (IllegalArgumentException exception) {
                // if the color is not valid, fall back
                worldTimeTracker.timeBar.logger.warning(colorConfig + " is not a valid color for TimeBar.");
            }
        }

        // loop through all bossbars and format the title
        for (Map.Entry<Player, BossBar> entry : worldTimeTracker.getBossBars().entrySet()) {
            Player player = entry.getKey();
            BossBar bossBar = entry.getValue();
            int temp = api.getTemperature(player);
            Component title;

            // format the title
            String titleRaw = parseTitle(timeOfDay, dayCount, seasonFromConfig, temp, timePercent);

            // format if PAPI is detected
            if (worldTimeTracker.timeBar.papiSupport) {
                String formattedTitle = PlaceholderUtil.format(player, titleRaw);
                title = worldTimeTracker.timeBar.textUtils.format(formattedTitle);
            } else {
                title = worldTimeTracker.timeBar.textUtils.format(titleRaw);
            }
            bossBar.name(title);
            bossBar.progress(progress);
            bossBar.color(color);

            // if the player is holding a clock
            if (worldTimeTracker.timeBar.config.getBoolean("hold-clock-to-show")) {
                if (player.getInventory().getItemInMainHand().getType() == Material.CLOCK) {
                    worldTimeTracker.showPlayer(player);
                } else {
                    worldTimeTracker.hidePlayer(player);
                }
            }
        }
    }

    /**
     * Format the title.
     *
     * @param timeOfDay The time of day.
     * @param dayCount  The day count.
     * @param season    The current season.
     * @param temp      The player's temperature.
     * @param progress  The day progress.
     * @return The formatted title.
     */
    private String parseTitle(String timeOfDay, int dayCount, String season, int temp, float progress) {
        String title = worldTimeTracker.timeBar.advancedSeasonsConfig.getString("timebar-title");
        if (title == null) {
            worldTimeTracker.timeBar.logger.severe("timebar-title is not set! Using default.");
            title = "{TIME} (Day {DAYCOUNT}) {SEASON} - {TEMP}";
        }

        if (title.contains("{TIME}")) {
            title = title.replace("{TIME}", timeOfDay);
        }

        if (title.contains("{DAYCOUNT}")) {
            title = title.replace("{DAYCOUNT}", NumberFormat.formatInt(dayCount));
        }

        if (title.contains("{SEASON}")) {
            title = title.replace("{SEASON}", season);
        }

        if (title.contains("{TEMP}")) {
            title = title.replace("{TEMP}", temp + "°C");
        }

        if (title.contains("{DAYPERCENT}")) {
            title = title.replace("{DAYPERCENT}", String.format("%.2f", progress) + "%");
        }
        return title;
    }

    /**
     * Get config's time of day value.
     *
     * @param timeOfDay Time of day.
     * @return The time of day start tick.
     */
    private int getTime(String timeOfDay) {
        return worldTimeTracker.timeBar.config.getInt("times-of-day." + timeOfDay);
    }

    /**
     * Gets the "time of day" based on time.
     *
     * @return The time of day.
     */
    private String getTimeOfDay(int time) {
        // set the time of day depending on the time
        // dawn
        if (time >= getTime("dawn") || time < getTime("morning")) {
            return worldTimeTracker.timeBar.config.getString("times.dawn");
        }
        // morning
        if (time >= getTime("morning") && time < getTime("noon")) {
            return worldTimeTracker.timeBar.config.getString("times.morning");
        }
        // noon
        if (time >= getTime("noon") && time < getTime("afternoon")) {
            return worldTimeTracker.timeBar.config.getString("times.noon");
        }
        // afternoon
        if (time >= getTime("afternoon") && time < getTime("sunset")) {
            return worldTimeTracker.timeBar.config.getString("times.afternoon");
        }
        // sunset
        if (time >= getTime("sunset") && time < getTime("night")) {
            return worldTimeTracker.timeBar.config.getString("times.sunset");
        }
        // night
        if (time >= getTime("night") && time < getTime("midnight")) {
            return worldTimeTracker.timeBar.config.getString("times.night");
        }
        // midnight
        if (time >= getTime("midnight") && time < getTime("dawn")) {
            return worldTimeTracker.timeBar.config.getString("times.midnight");
        }
        return "INVALID";
    }
}
