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
        WorldTimeTracker oldTracker = timeBar.worldTimeTrackers.stream().filter(worldTimeTracker -> worldTimeTracker.worldGroup().contains(oldWorld)).findFirst().orElse(null);
        // get tracker for new location
        WorldTimeTracker newTracker = timeBar.worldTimeTrackers.stream().filter(worldTimeTracker -> worldTimeTracker.worldGroup().contains(newWorld)).findFirst().orElse(null);
        // if the old world had a tracker
        if (oldTracker != null) {
            // if they moved to a new tracker group
            if (!oldTracker.worldGroup().contains(newWorld)) {
                oldTracker.removePlayer(player);
                // if the new world has a tracker, add them
                if (newTracker != null) {
                    newTracker.addPlayer(player);
                    // perform the swap
                    if (!newTracker.running()) {
                        newTracker.startTimer();
                    }
                }
                // if we moved and the old world has no players, stop tracking
                if (oldTracker.getBossBars().isEmpty()) {
                    oldTracker.stopTimer();
                }
            }
        } else {
            // player is coming from a world that doesn't have a tracker
            // if the new world has a tracker, add them
            if (newTracker != null) {
                newTracker.addPlayer(player);
                // if the world they move to is not tracking, start it
                if (!newTracker.running()) {
                    newTracker.startTimer();
                }
            }
        }
    }
}
