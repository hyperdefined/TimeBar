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

package lol.hyper.timebar.tracker;

import lol.hyper.timebar.TimeBar;
import lol.hyper.timebar.timers.AdvancedSeasonsTask;
import lol.hyper.timebar.timers.RealisticSeasonsTask;
import lol.hyper.timebar.timers.RegularTimeBarTask;
import lol.hyper.timebar.utils.NumberFormat;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldTimeTracker {

    public final TimeBar timeBar;
    private final World mainWorld;
    private final List<World> worldGroup;
    private final Map<Player, BossBar> bossBars = new HashMap<>();

    private BukkitTask timeBarTask;

    private String timeOfDay;
    private int dayCount;
    private float dayPercent;

    /**
     * Creates a tracker for a collection of worlds.
     *
     * @param timeBar         Plugin instance.
     * @param mainWorld       The world to track time for.
     * @param worldsToDisplay The worlds to display bossbars for.
     */
    public WorldTimeTracker(TimeBar timeBar, World mainWorld, List<World> worldsToDisplay) {
        this.timeBar = timeBar;
        this.mainWorld = mainWorld;
        this.worldGroup = worldsToDisplay;
    }

    public World getMainWorld() {
        return mainWorld;
    }

    public List<World> worldGroup() {
        return worldGroup;
    }

    public Map<Player, BossBar> getBossBars() {
        return bossBars;
    }

    /**
     * Add player to this tracker.
     *
     * @param player Player to add.
     */
    public void addPlayer(Player player) {
        BossBar bossBar = BossBar.bossBar(Component.text("Loading world time..."), 0, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        bossBars.put(player, bossBar);

        // honor worlds to display in
        // they are in a display world, show them timebar
        if (worldGroup.contains(player.getWorld())) {
            // if they have it enabled
            if (timeBar.enabledBossBar.contains(player)) {
                timeBar.getAdventure().player(player).showBossBar(bossBar);
            }
        }
    }

    /**
     * Remove player from this tracker.
     *
     * @param player Player to remove.
     */
    public void removePlayer(Player player) {
        BossBar bossBar = bossBars.get(player);
        timeBar.getAdventure().player(player).hideBossBar(bossBar);
        bossBars.remove(player);
    }

    /**
     * Start tracking time for main world defined in this tracker.
     * This also will update bossbars for any players on this tracker.
     */
    public void startTimer() {
        // start the tracker
        int updateFrequency = timeBar.config.getInt("bar-update-frequency");
        String allWorldNames = worldGroup.stream().map(World::getName).collect(Collectors.joining(", "));
        if (timeBar.realisticSeasons) {
            timeBarTask = new RealisticSeasonsTask(this).runTaskTimer(timeBar, 0, updateFrequency);
            timeBar.logger.info("Starting time tracker for '" + mainWorld.getName() + "'" + " (RealisticSeasons support)");
            timeBar.logger.info("Display worlds: [" + allWorldNames + "]");
        } else if (timeBar.advancedSeasons) {
            timeBarTask = new AdvancedSeasonsTask(this).runTaskTimer(timeBar, 0, updateFrequency);
            timeBar.logger.info("Starting time tracker for '" + mainWorld.getName() + "'" + " (AdvancedSeasons support)");
            timeBar.logger.info("Display worlds: [" + allWorldNames + "]");
        } else {
            timeBarTask = new RegularTimeBarTask(this).runTaskTimer(timeBar, 0, updateFrequency);
            timeBar.logger.info("Starting time tracker for '" + mainWorld.getName() + "'");
            timeBar.logger.info("Display worlds: [" + allWorldNames + "]");
        }
    }

    public void stopTimer() {
        // stop the tracker
        timeBarTask.cancel();
        timeBar.logger.info("Stopping current TimeBar task for '" + mainWorld.getName() + "'");
    }

    public boolean isRunning() {
        return !timeBarTask.isCancelled();
    }

    /**
     * Hide all bossbars for players on this tracker.
     */
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

    /**
     * Hide one player's bossbar.
     *
     * @param player Player to hide bossbar.
     */
    public void hidePlayer(Player player) {
        BossBar bossBar = bossBars.get(player);
        timeBar.getAdventure().player(player).hideBossBar(bossBar);
        timeBar.enabledBossBar.remove(player);
    }

    /**
     * Shows one player's bossbar.
     *
     * @param player Player to show bossbar.
     */
    public void showPlayer(Player player) {
        // honor worlds to display in
        // they are in a display world, show them timebar
        if (worldGroup.contains(player.getWorld())) {
            BossBar bossBar = bossBars.get(player);
            timeBar.getAdventure().player(player).showBossBar(bossBar);
        }
        timeBar.enabledBossBar.add(player);
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }

    public void setDayPercent(float dayPercent) {
        this.dayPercent = dayPercent;
    }

    public void setTimeOfDay(String timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public String getDayPercent() {
        return String.format("%.2f", dayPercent) + "%";
    }

    public String getDayCount() {
        return NumberFormat.formatInt(dayCount);
    }

    public String getTimeOfDay() {
        return timeOfDay;
    }
}
