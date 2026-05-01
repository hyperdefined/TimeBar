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

package lol.hyper.timebar.events;

import lol.hyper.timebar.TimeBar;
import lol.hyper.timebar.tracker.WorldTimeTracker;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChange implements Listener {

    private final TimeBar timeBar;

    public WorldChange(TimeBar timeBar) {
        this.timeBar = timeBar;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World newWorld = player.getWorld();
        World oldWorld = event.getFrom();

        // get tracker from old location
        WorldTimeTracker oldTracker = timeBar.getTracker(oldWorld);
        // get tracker for new location
        WorldTimeTracker newTracker = timeBar.getTracker(newWorld);

        if (oldTracker == newTracker) {
            return;
        }

        // if the old world had a tracker
        if (oldTracker != null) {
            // when the tracker has no one, stop it
            oldTracker.removePlayer(player);
            if (oldTracker.getBossBars().isEmpty()) {
                oldTracker.stopTimer();
            }
        }

        // if the new world has a tracker, add them
        if (newTracker != null) {
            newTracker.addPlayer(player);
            // if the tracker is not running, start it
            if (!newTracker.running()) {
                newTracker.startTimer();
            }

            return;
        }

        // if the world moving to does not have a tracker
        // and it's on the list, make a tracker
        if (timeBar.configuredWorlds.containsKey(player.getWorld().getName())) {
            timeBar.worldLoad.makeTracker(player.getWorld(), true);

            WorldTimeTracker tracker = timeBar.getPlayerTracker(player);
            if (tracker != null) {
                tracker.addPlayer(player);
            }
        }
    }
}
