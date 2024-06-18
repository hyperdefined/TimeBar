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
import net.advancedplugins.seasons.api.AdvancedSeasonsAPI;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class AdvancedSeasonsTask extends BukkitRunnable {

    private final WorldTimeTracker worldTimeTracker;
    private final AdvancedSeasonsAPI api = new AdvancedSeasonsAPI();
    private final World world;

    public AdvancedSeasonsTask(WorldTimeTracker worldTimeTracker) {
        this.worldTimeTracker = worldTimeTracker;
        this.world = worldTimeTracker.getMainWorld();
    }

    @Override
    public void run() {
        // get the current time
        int time = (int) world.getTime();
        float progress = (float) (time / 24000.0);

        // get the title
        String title = getTimeOfDay(time);
        // add the season
        if (title.contains("{SEASON}")) {
            String season = worldTimeTracker.timeBar.advancedSeasonsConfig.getString("seasons." + api.getSeason(world));
            if (season == null) {
                worldTimeTracker.timeBar.logger.warning("Unable to load custom season name for " + api.getSeason(world) + " from /plugins/TimeBar/advancedseasons.yml");
                season = api.getSeason(world);
            }
            title = title.replace("{SEASON}", season);
        }

        // loop through all bossbars and format the title
        for (Map.Entry<Player, BossBar> entry : worldTimeTracker.getBossBars().entrySet()) {
            Player player = entry.getKey();
            BossBar bossBar = entry.getValue();
            int temp = api.getTemperature(player);

            // add the temp. this is per player so we do it here
            if (title.contains("{TEMP}")) {
                title = title.replace("{TEMP}", temp + "°C");
            }

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
     * Parses the title, which formats any placeholders.
     *
     * @param time The current time string.
     * @return Formatted title.
     */
    private String formatTitle(String time) {
        String title = worldTimeTracker.timeBar.advancedSeasonsConfig.getString("timebar-title");
        if (title == null) {
            worldTimeTracker.timeBar.logger.severe("timebar-title is not set! Using default.");
            title = "{TIME} (Day {DAYCOUNT}) {SEASON} - {TEMP}";
        }

        if (title.contains("{TIME}")) {
            title = title.replace("{TIME}", time);
        }

        if (title.contains("{DAYCOUNT}")) {
            title = title.replace("{DAYCOUNT}", String.valueOf(world.getFullTime() / 24000));
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
            return formatTitle(worldTimeTracker.timeBar.config.getString("times.dawn"));
        }
        // morning
        if (time >= getTime("morning") && time < getTime("noon")) {
            return formatTitle(worldTimeTracker.timeBar.config.getString("times.morning"));
        }
        // noon
        if (time >= getTime("noon") && time < getTime("afternoon")) {
            return formatTitle(worldTimeTracker.timeBar.config.getString("times.noon"));
        }
        // afternoon
        if (time >= getTime("afternoon") && time < getTime("sunset")) {
            return formatTitle(worldTimeTracker.timeBar.config.getString("times.afternoon"));
        }
        // sunset
        if (time >= getTime("sunset") && time < getTime("night")) {
            return formatTitle(worldTimeTracker.timeBar.config.getString("times.sunset"));
        }
        // night
        if (time >= getTime("night") && time < getTime("midnight")) {
            return formatTitle(worldTimeTracker.timeBar.config.getString("times.night"));
        }
        // midnight
        if (time >= getTime("midnight") && time < getTime("dawn")) {
            return formatTitle(worldTimeTracker.timeBar.config.getString("times.midnight"));
        }
        return "INVALID";
    }
}
