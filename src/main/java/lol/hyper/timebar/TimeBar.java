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
import lol.hyper.timebar.timers.RealisticSeasonsTask;
import lol.hyper.timebar.timers.RegularTimeBarTask;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public final class TimeBar extends JavaPlugin {

    public final File configFile = new File(this.getDataFolder(), "config.yml");
    public final File realisticSeasonsConfigFile = new File(this.getDataFolder(), "realisticseasons.yml");
    public final Logger logger = this.getLogger();
    public final Map<UUID, BossBar> bossBarMap = new HashMap<>();
    public FileConfiguration config;
    public FileConfiguration realisticSeasonsConfig;
    public String worldName;
    public final List<Player> enabledBossBar = new ArrayList<>();

    public final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitAudiences adventure;

    public PlayerJoinLeave playerJoinLeave;
    public WorldChange worldChange;
    public CommandTimeBar commandReload;
    public BukkitTask timeBarTask;

    public boolean papiSupport = false;
    public BossBar.Color bossBarColor;

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);

        loadConfig();
        playerJoinLeave = new PlayerJoinLeave(this);
        worldChange = new WorldChange(this);
        commandReload = new CommandTimeBar(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiSupport = true;
            logger.info("PlaceholderAPI is detected! Enabling support.");
        }

        this.getCommand("timebar").setExecutor(commandReload);

        Bukkit.getServer().getPluginManager().registerEvents(playerJoinLeave, this);
        Bukkit.getServer().getPluginManager().registerEvents(worldChange, this);

        new Metrics(this, 10674);

        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);

        startTimer();
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        int CONFIG_VERSION = 3;
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warning("You configuration is out of date! Some features may not work!");
        }

        worldName = config.getString("world-to-track-time");
        if (worldName == null) {
            logger.severe("world-to-track-time is not set! Defaulting to world.");
            worldName = "world";
        }

        if (Bukkit.getWorld(worldName) == null) {
            logger.severe(worldName + " is not a valid world! Using default \"world\" instead.");
            worldName = "world";
        }

        String color = config.getString("titlebar-color");
        if (color == null) {
            // default to blue since I like it
            bossBarColor = BossBar.Color.BLUE;
        } else {
            color = color.toUpperCase(Locale.ROOT);
            bossBarColor = BossBar.Color.valueOf(color);
        }

        if (this.getServer().getPluginManager().isPluginEnabled("RealisticSeasons")) {
        	logger.info("RealisticSeasons is detected! Enabling support.");
            if (!realisticSeasonsConfigFile.exists()) {
                this.saveResource("realisticseasons.yml", true);
            }
            realisticSeasonsConfig = YamlConfiguration.loadConfiguration(realisticSeasonsConfigFile);
            int SEASONS_CONFIG_VERSION = 3;
            if (realisticSeasonsConfig.getInt("config-version") != SEASONS_CONFIG_VERSION) {
                logger.warning("You seasons configuration is out of date! Some features may not work!");
            }
        }
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

    public void startTimer() {
        if (timeBarTask != null) {
                timeBarTask.cancel();
                logger.info("Stopping current TimeBar task...");
        }
        if (this.getServer().getPluginManager().isPluginEnabled("RealisticSeasons")) {
            timeBarTask = new RealisticSeasonsTask(this).runTaskTimer(this, 0, config.getInt("bar-update-frequency"));
            logger.info("Starting new TimeBar task with RealisticSeasons support...");
        } else {
            timeBarTask = new RegularTimeBarTask(this).runTaskTimer(this, 0, config.getInt("bar-update-frequency"));
            logger.info("Starting new TimeBar task...");
        }
    }
}
