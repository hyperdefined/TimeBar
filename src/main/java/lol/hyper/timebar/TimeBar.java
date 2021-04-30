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

import lol.hyper.timebar.commands.CommandTimeBar;
import lol.hyper.timebar.events.Events;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class TimeBar extends JavaPlugin {

    public BossBar timeTracker = Bukkit.createBossBar("World Time", BarColor.BLUE, BarStyle.SOLID);

    public final File configFile = new File(this.getDataFolder(), "config.yml");
    public FileConfiguration config;
    public final Logger logger = this.getLogger();
    public int timeBarTask;
    public String worldName = "";

    public Events events;
    public CommandTimeBar commandReload;

    @Override
    public void onEnable() {
        loadConfig();
        events = new Events(this);
        commandReload = new CommandTimeBar(this);

        this.getCommand("timebar").setExecutor(commandReload);

        Bukkit.getServer().getPluginManager().registerEvents(events, this);

        Metrics metrics = new Metrics(this, 90179);

        new UpdateChecker(this, 91822).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                logger.info("You are running the latest version.");
            } else {
                logger.info("There is a new version available! Please download at https://www.spigotmc.org/resources/timebar.90179/");
            }
        });
    }

    private void startTimer() {
        timeBarTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
        double time = Bukkit.getWorld("world").getTime();
        timeTracker.setProgress(time / 24000.0);
        if (time >= 23000) {
            timeTracker.setTitle(parseString(config.getString("times.dawn")));
        }
        if (time >= 0 && time < 6000) {
            timeTracker.setTitle(parseString(config.getString("times.morning")));
        }
        if (time >= 6000 && time < 9000) {
            timeTracker.setTitle(parseString(config.getString("times.noon")));
        }
        if (time >= 9000 && time < 12000) {
            timeTracker.setTitle(parseString(config.getString("times.afternoon")));
        }
        if (time >= 12000 && time < 14000) {
            timeTracker.setTitle(parseString(config.getString("times.sunset")));
        }
        if (time >= 14000 && time < 18000) {
            timeTracker.setTitle(parseString(config.getString("times.night")));
        }
        if (time >= 18000 && time < 23000) {
            timeTracker.setTitle(parseString(config.getString("times.midnight")));
        }
        }, 0, 20);
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        int CONFIG_VERSION = 2;
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warning("You configuration is out of date! Some features may not work!");
        }

        worldName = config.getString("world-to-track-time");

        if (Bukkit.getWorld(worldName) == null) {
            logger.severe(worldName + " is not a valid world! Using default \"world\" instead.");
            worldName = "world";
        }

        String color = config.getString("titlebar-color");
        if (color.equalsIgnoreCase("blue")) {
            timeTracker.setColor(BarColor.BLUE);
        }
        if (color.equalsIgnoreCase("green")) {
            timeTracker.setColor(BarColor.GREEN);
        }
        if (color.equalsIgnoreCase("pink")) {
            timeTracker.setColor(BarColor.PINK);
        }
        if (color.equalsIgnoreCase("purple")) {
            timeTracker.setColor(BarColor.PURPLE);
        }
        if (color.equalsIgnoreCase("red")) {
            timeTracker.setColor(BarColor.RED);
        }
        if (color.equalsIgnoreCase("white")) {
            timeTracker.setColor(BarColor.WHITE);
        }
        if (color.equalsIgnoreCase("yellow")) {
            timeTracker.setColor(BarColor.YELLOW);
        }

        startTimer();
    }

    private String parseString(String time) {
        String title = config.getString("timebar-title");

        if (title.contains("{TIME}")) {
            title = title.replace("{TIME}", time);
        }

        if (title.contains("{DAYCOUNT}")) {
            title = title.replace("{DAYCOUNT}", String.valueOf(Bukkit.getWorld(worldName).getFullTime() / 24000));
        }
        return title;
    }
}