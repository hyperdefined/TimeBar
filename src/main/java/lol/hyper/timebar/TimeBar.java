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

import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.timebar.commands.CommandTimeBar;
import lol.hyper.timebar.events.PlayerJoinLeave;
import lol.hyper.timebar.events.WorldChange;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public final class TimeBar extends JavaPlugin {

    public final File configFile = new File(this.getDataFolder(), "config.yml");
    public final Logger logger = this.getLogger();
    public BossBar timeTracker;
    public FileConfiguration config;
    public int timeBarTask;
    public String worldName = "";
    public final List<Player> enabledBossBar = new ArrayList<>();

    public final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitAudiences adventure;

    public PlayerJoinLeave playerJoinLeave;
    public WorldChange worldChange;
    public CommandTimeBar commandReload;

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);
        timeTracker = BossBar.bossBar(Component.text("World Time"), 0, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        loadConfig();
        playerJoinLeave = new PlayerJoinLeave(this);
        worldChange = new WorldChange(this);
        commandReload = new CommandTimeBar(this);

        this.getCommand("timebar").setExecutor(commandReload);

        Bukkit.getServer().getPluginManager().registerEvents(playerJoinLeave, this);
        Bukkit.getServer().getPluginManager().registerEvents(worldChange, this);

        new Metrics(this, 90179);

        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);
    }

    private void startTimer() {
        timeBarTask = Bukkit.getScheduler()
                .scheduleSyncRepeatingTask(
                        this,
                        () -> {
                            double time = Bukkit.getWorld("world").getTime();
                            timeTracker.progress((float) (time / 24000.0));
                            Component title = Component.text("World Time");
                            if (time >= 23000) {
                                title = parseString(config.getString("times.dawn"));
                            }
                            if (time >= 0 && time < 6000) {
                                title = parseString(config.getString("times.morning"));
                            }
                            if (time >= 6000 && time < 9000) {
                                title = parseString(config.getString("times.noon"));
                            }
                            if (time >= 9000 && time < 12000) {
                                title = parseString(config.getString("times.afternoon"));
                            }
                            if (time >= 12000 && time < 14000) {
                                title = parseString(config.getString("times.sunset"));
                            }
                            if (time >= 14000 && time < 18000) {
                                title = parseString(config.getString("times.night"));
                            }
                            if (time >= 18000 && time < 23000) {
                                title = parseString(config.getString("times.midnight"));
                            }

                            timeTracker.name(title);
                        },
                        0,
                        20);
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
        if (color == null) {
            // default to blue since I like it
            timeTracker.color(BossBar.Color.BLUE);
        } else {
            color = color.toUpperCase(Locale.ROOT);
            timeTracker.color(BossBar.Color.valueOf(color));
        }

        startTimer();
    }

    private Component parseString(String time) {
        String title = config.getString("timebar-title");

        if (title.contains("{TIME}")) {
            title = title.replace("{TIME}", time);
        }

        if (title.contains("{DAYCOUNT}")) {
            title = title.replace(
                    "{DAYCOUNT}", String.valueOf(Bukkit.getWorld(worldName).getFullTime() / 24000));
        }
        return miniMessage.deserialize(title);
    }

    public void checkForUpdates() {
        GitHubReleaseAPI api;
        try {
            api = new GitHubReleaseAPI("TimeBar", "hyperdefined");
        } catch (IOException e) {
            logger.warning("Unable to check updates!");
            e.printStackTrace();
            return;
        }
        GitHubRelease current = api.getReleaseByTag(this.getDescription().getVersion());
        GitHubRelease latest = api.getLatestVersion();
        if (current == null) {
            logger.warning("You are running a version that does not exist on GitHub. If you are in a dev environment, you can ignore this. Otherwise, this is a bug!");
            return;
        }
        int buildsBehind = api.getBuildsBehind(current);
        if (buildsBehind == 0) {
            logger.info("You are running the latest version.");
        } else {
            logger.warning("A new version is available (" + latest.getTagVersion() + ")! You are running version " + current.getTagVersion() + ". You are " + buildsBehind + " version(s) behind.");
        }
    }

    public BukkitAudiences getAdventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }
}
