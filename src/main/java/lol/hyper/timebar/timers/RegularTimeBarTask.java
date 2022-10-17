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
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class RegularTimeBarTask extends BukkitRunnable {

    private final TimeBar timeBar;
    private final World world;

    public RegularTimeBarTask(TimeBar timeBar) {
        this.timeBar = timeBar;
        this.world = Bukkit.getWorld(timeBar.worldName);
    }

    @Override
    public void run() {
        if (world == null) {
            this.timeBar.logger.severe(timeBar.worldName + " is not a valid world!");
            this.cancel();
            return;
        }
        // get the current time
        int time = (int) world.getTime();
        timeBar.timeTracker.progress((float) (time / 24000.0));
        Component title = Component.text("World Time");

        // set the time of day depending on the time
        // dawn
        if (time >= getTime("dawn") || time < getTime("morning")) {
            title = parseString(timeBar.config.getString("times.dawn"));
        }
        // morning
        if (time >= getTime("morning") && time < getTime("noon")) {
            title = parseString(timeBar.config.getString("times.morning"));
        }
        // noon
        if (time >= getTime("noon") && time < getTime("afternoon")) {
            title = parseString(timeBar.config.getString("times.noon"));
        }
        // afternoon
        if (time >= getTime("afternoon") && time < getTime("sunset")) {
            title = parseString(timeBar.config.getString("times.afternoon"));
        }
        // sunset
        if (time >= getTime("sunset") && time < getTime("night")) {
            title = parseString(timeBar.config.getString("times.sunset"));
        }
        // night
        if (time >= getTime("night") && time < getTime("midnight")) {
            title = parseString(timeBar.config.getString("times.night"));
        }
        // midnight
        if (time >= getTime("midnight") && time < getTime("dawn")) {
            title = parseString(timeBar.config.getString("times.midnight"));
        }

        timeBar.timeTracker.name(title);
    }

    /**
     * Parses the title, which formats any placeholders.
     *
     * @param time The current time string.
     * @return Formatted title.
     */
    private Component parseString(String time) {
        String title = timeBar.config.getString("timebar-title");
        if (title == null) {
            timeBar.logger.severe("timebar-title is not set! Using default.");
            title = "{TIME} (Day {DAYCOUNT})";
        }

        if (title.contains("{TIME}")) {
            title = title.replace("{TIME}", time);
        }

        if (title.contains("{DAYCOUNT}")) {
            title = title.replace("{DAYCOUNT}", String.valueOf(world.getFullTime() / 24000));
        }
        return timeBar.miniMessage.deserialize(title);
    }

    /**
     * Get config's time of day value.
     *
     * @param timeOfDay Time of day.
     * @return The time of day start tick.
     */
    private int getTime(String timeOfDay) {
        return timeBar.config.getInt("times-of-day." + timeOfDay);
    }
}
