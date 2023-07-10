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
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
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
        // if the player joins the server in a world that the bossbar
        // is not enabled in, don't show it
        BossBar playerBossBar = BossBar.bossBar(Component.text("World Time"), 0, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        timeBar.bossBarMap.put(player.getUniqueId(), playerBossBar);
        if (!timeBar.config.getStringList("worlds-to-show-in").contains(world.getName())) {
            audiences.player(player).hideBossBar(playerBossBar);
            timeBar.enabledBossBar.remove(player);
        } else {
            // show the bossbar if the player joins in a world we display it
            audiences.player(player).showBossBar(playerBossBar);
            timeBar.enabledBossBar.add(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BossBar playerBossBar = timeBar.bossBarMap.get(player.getUniqueId());
        audiences.player(player).hideBossBar(playerBossBar);
        timeBar.bossBarMap.remove(player.getUniqueId());
        timeBar.enabledBossBar.remove(player);
    }
}
