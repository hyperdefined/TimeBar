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
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class RegularTimeBarTask extends BukkitRunnable {

    private final WorldTimeTracker worldTimeTracker;
    private final World world;

    public RegularTimeBarTask(WorldTimeTracker worldTimeTracker) {
        this.worldTimeTracker = worldTimeTracker;
        this.world = worldTimeTracker.getMainWorld();
    }

    @Override
    public void run() {
        // get the current time
        int time = (int) world.getTime();
        float progress = (float) (time / 24000.0);
        float timePercent = progress * 100;
        int dayCount = (int) (world.getFullTime() / 24000);
        String timeOfDay = getTimeOfDay(time);

        // store these into the tracker
        worldTimeTracker.setDayPercent(timePercent);
        worldTimeTracker.setDayCount(dayCount);
        worldTimeTracker.setTimeOfDay(timeOfDay);

        // set the title
        String titleRaw = parseTitle(timeOfDay, dayCount, timePercent);

        // loop through all bossbars and format the title
        for (Map.Entry<Player, BossBar> entry : worldTimeTracker.getBossBars().entrySet()) {
            Player player = entry.getKey();
            BossBar bossBar = entry.getValue();
            Component title;
            // format if PAPI is detected
            if (worldTimeTracker.timeBar.papiSupport) {
                String formattedTitle = PlaceholderUtil.format(player, titleRaw);
                title = worldTimeTracker.timeBar.textUtils.format(formattedTitle);
            } else {
                title = worldTimeTracker.timeBar.textUtils.format(titleRaw);
            }
            bossBar.name(title);
            bossBar.progress(progress);
            bossBar.color(worldTimeTracker.timeBar.bossBarColor);

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
     * @param progress  The day progress.
     * @return The formatted title.
     */
    private String parseTitle(String timeOfDay, int dayCount, float progress) {
        String title = worldTimeTracker.timeBar.config.getString("timebar-title");
        if (title == null) {
            worldTimeTracker.timeBar.logger.warn("timebar-title is not set! Using default.");
            title = "{TIME} (Day {DAYCOUNT})";
        }

        if (title.contains("{TIME}")) {
            title = title.replace("{TIME}", timeOfDay);
        }

        if (title.contains("{DAYCOUNT}")) {
            title = title.replace("{DAYCOUNT}", NumberFormat.formatInt(dayCount));
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
