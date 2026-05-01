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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldLoad implements Listener {

    private final TimeBar timeBar;

    public WorldLoad(TimeBar timeBar) {
        this.timeBar = timeBar;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        World loadedWorld = event.getWorld();
        String worldName = loadedWorld.getName();

        // world loaded is a main world, make the tracker
        if (timeBar.configuredWorlds.containsKey(worldName)) {
            // already have a timer for this world
            if (timeBar.worldTimeTrackers.containsKey(worldName)) {
                return;
            }
            timeBar.logger.info("Detected new world {} as a tracked world, adding...", worldName);
            makeTracker(loadedWorld, false);
            return;
        }

        // world is a display world
        // add it to the main world if loaded
        for (Map.Entry<String, List<String>> entry : timeBar.configuredWorlds.entrySet()) {
            String mainWorldName = entry.getKey();
            List<String> displayNames = entry.getValue();

            if (!displayNames.contains(worldName)) {
                continue;
            }

            timeBar.logger.info("Detected new world {} as a display world for {}, adding...", worldName, mainWorldName);

            WorldTimeTracker tracker = timeBar.worldTimeTrackers.get(mainWorldName);
            if (tracker != null) {
                tracker.addDisplay(loadedWorld);
            }
        }
    }

    public void makeTracker(World world, boolean start) {
        // this probably won't ever happen
        if (world == null) {
            return;
        }

        // get the display worlds
        // if they are loaded, add them
        List<World> displayWorlds = new ArrayList<>();
        for (String displayWorldName : timeBar.configuredWorlds.get(world.getName())) {
            World displayWorld = Bukkit.getWorld(displayWorldName);

            if (displayWorld != null) {
                displayWorlds.add(displayWorld);
            }
        }

        WorldTimeTracker tracker = new WorldTimeTracker(timeBar, world, displayWorlds);
        timeBar.worldTimeTrackers.put(world.getName(), tracker);
        if (start) {
            tracker.startTimer();
        }
    }
}
