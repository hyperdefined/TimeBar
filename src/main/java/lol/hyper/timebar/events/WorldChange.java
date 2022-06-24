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
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChange implements Listener {

    private final TimeBar timeBar;
    private final BukkitAudiences audiences;

    public WorldChange(TimeBar timeBar) {
        this.timeBar = timeBar;
        this.audiences = timeBar.getAdventure();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        // Check the worlds list in the config, if it's empty, then we can ignore this event
        if (timeBar.config.getStringList("worlds-to-show-in").isEmpty()) {
            return;
        }

        Player player = event.getPlayer();
        World newWorld = player.getWorld();

        // Check to see if the player is going to a world that the TimeBar is enabled in
        // If the player goes to a world on the list, show them the bar
        // If not, remove it
        if (timeBar.config.getStringList("worlds-to-show-in").contains(newWorld.getName())) {
            if (timeBar.enabledBossBar.contains(player)) {
                audiences.player(player).showBossBar(timeBar.timeTracker);
            } else {
                audiences.player(player).hideBossBar(timeBar.timeTracker);
            }
        } else {
            audiences.player(player).hideBossBar(timeBar.timeTracker);
        }
    }
}
