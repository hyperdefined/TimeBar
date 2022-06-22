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

public class RegularTimeBarTask extends BukkitRunnable  {

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

        double time = world.getTime();
        timeBar.timeTracker.progress((float) (time / 24000.0));
        Component title = Component.text("World Time");

        if (time >= 23000) {
            title = parseString(timeBar.config.getString("times.dawn"));
        }
        if (time >= 0 && time < 6000) {
            title = parseString(timeBar.config.getString("times.morning"));
        }
        if (time >= 6000 && time < 9000) {
            title = parseString(timeBar.config.getString("times.noon"));
        }
        if (time >= 9000 && time < 12000) {
            title = parseString(timeBar.config.getString("times.afternoon"));
        }
        if (time >= 12000 && time < 14000) {
            title = parseString(timeBar.config.getString("times.sunset"));
        }
        if (time >= 14000 && time < 18000) {
            title = parseString(timeBar.config.getString("times.night"));
        }
        if (time >= 18000 && time < 23000) {
            title = parseString(timeBar.config.getString("times.midnight"));
        }

        timeBar.timeTracker.name(title);
    }

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
            title = title.replace("{DAYCOUNT}", String.valueOf(Bukkit.getWorld(timeBar.worldName).getFullTime() / 24000));
        }
        return timeBar.miniMessage.deserialize(title);
    }
}
