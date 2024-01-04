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

package lol.hyper.timebar;

import lol.hyper.timebar.timers.RealisticSeasonsTask;
import lol.hyper.timebar.timers.RegularTimeBarTask;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldTimeTracker {

    public final TimeBar timeBar;
    private final World mainWorld;
    private final List<World> worldsToDisplay;
    public final Map<Player, BossBar> bossBars = new HashMap<>();

    public BukkitTask timeBarTask;

    /**
     * Create a world time object. This tracks bossbars for players and world time.
     *
     * @param mainWorld       The world to track time for.
     * @param worldsToDisplay The worlds to display the bossbar for.
     */
    public WorldTimeTracker(TimeBar timeBar, World mainWorld, List<World> worldsToDisplay) {
        this.timeBar = timeBar;
        this.mainWorld = mainWorld;
        this.worldsToDisplay = worldsToDisplay;
    }

    public World getMainWorld() {
        return mainWorld;
    }

    public List<World> worldGroup() {
        return worldsToDisplay;
    }

    public void addPlayer(Player player) {
        BossBar bossBar = BossBar.bossBar(Component.text("Loading world time..."), 0, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        bossBars.put(player, bossBar);

        // honor worlds to display in
        // they are in a display world, show them timebar
        if (worldsToDisplay.contains(player.getWorld())) {
            // if they have it enabled
            if (timeBar.enabledBossBar.contains(player)) {
                timeBar.getAdventure().player(player).showBossBar(bossBar);
            }
        }
    }

    public void removePlayer(Player player) {
        BossBar bossBar = bossBars.get(player);
        timeBar.getAdventure().player(player).hideBossBar(bossBar);
        bossBars.remove(player);
    }

    public void startTimer() {
        if (timeBarTask != null) {
            timeBarTask.cancel();
            timeBar.logger.info("Stopping current TimeBar task for world " + mainWorld.getName());
        }
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("RealisticSeasons")) {
            timeBarTask = new RealisticSeasonsTask(this).runTaskTimer(timeBar, 0, timeBar.config.getInt("bar-update-frequency"));
            timeBar.logger.info("Starting time tracker for world: " + mainWorld.getName() + " (RealisticSeasons support)");
        } else {
            timeBarTask = new RegularTimeBarTask(this).runTaskTimer(timeBar, 0, timeBar.config.getInt("bar-update-frequency"));
            timeBar.logger.info("Starting time tracker for world: " + mainWorld.getName());
        }
    }

    public void hideBossBars() {
        for (Map.Entry<Player, BossBar> entry : bossBars.entrySet()) {
            Player player = entry.getKey();
            if (player == null) {
                continue;
            }
            BossBar bossBar = entry.getValue();
            timeBar.getAdventure().player(player).hideBossBar(bossBar);
        }
    }

    public void hidePlayer(Player player) {
        BossBar bossBar = bossBars.get(player);
        timeBar.getAdventure().player(player).hideBossBar(bossBar);
        timeBar.enabledBossBar.remove(player);
    }

    public void showPlayer(Player player) {
        // honor worlds to display in
        // they are in a display world, show them timebar
        if (worldsToDisplay.contains(player.getWorld())) {
            BossBar bossBar = bossBars.get(player);
            timeBar.getAdventure().player(player).showBossBar(bossBar);
        }
        timeBar.enabledBossBar.add(player);
    }
}
