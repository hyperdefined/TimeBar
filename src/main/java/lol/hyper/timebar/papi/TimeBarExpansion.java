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

package lol.hyper.timebar.papi;

import lol.hyper.timebar.TimeBar;
import lol.hyper.timebar.tracker.WorldTimeTracker;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class TimeBarExpansion extends PlaceholderExpansion {

    private final TimeBar timeBar;

    public TimeBarExpansion(TimeBar timeBar) {
        this.timeBar = timeBar;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "timebar";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", timeBar.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return timeBar.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        // ignore null players
        if (player == null) {
            return null;
        }
        // find their world tracker
        World world = player.getWorld();
        WorldTimeTracker playerWorldTracker = timeBar.worldTimeTrackers.stream().filter(tracker -> tracker.getMainWorld() == world).findFirst().orElse(null);
        if (playerWorldTracker == null) {
            return null;
        }

        // params being the placeholder, like timebar_day
        switch (params.toLowerCase(Locale.ROOT)) {
            case "daycount" -> {
                return playerWorldTracker.getDayCount();
            }
            case "time_of_day" -> {
                return playerWorldTracker.getTimeOfDay();
            }
            case "day_percent" -> {
                return playerWorldTracker.getDayPercent();
            }
        }
        return null;
    }
}
