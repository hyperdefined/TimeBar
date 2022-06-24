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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinLeave implements Listener {

    private final TimeBar timeBar;
    private final BukkitAudiences audiences;

    public PlayerJoinLeave(TimeBar timeBar) {
        this.timeBar = timeBar;
        this.audiences = timeBar.getAdventure();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!timeBar.config.getStringList("worlds-to-show-in").contains(world.getName())) {
            audiences.player(player).hideBossBar(timeBar.timeTracker);
            timeBar.enabledBossBar.remove(player);
        } else {
            audiences.player(player).showBossBar(timeBar.timeTracker);
            timeBar.enabledBossBar.add(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        audiences.player(player).hideBossBar(timeBar.timeTracker);
        timeBar.enabledBossBar.remove(player);
    }
}
