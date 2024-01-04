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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public final class TimeBar extends JavaPlugin {

    public final File configFile = new File(this.getDataFolder(), "config.yml");
    public final File realisticSeasonsConfigFile = new File(this.getDataFolder(), "realisticseasons.yml");
    public final Logger logger = this.getLogger();
    public FileConfiguration config;
    public FileConfiguration realisticSeasonsConfig;
    public final Set<Player> enabledBossBar = new HashSet<>();
    public final List<WorldTimeTracker> worldTimeTrackers = new ArrayList<>();

    public final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitAudiences adventure;

    public PlayerJoinLeave playerJoinLeave;
    public WorldChange worldChange;
    public CommandTimeBar commandReload;

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

        for (WorldTimeTracker worldTimeTracker : worldTimeTrackers) {
            worldTimeTracker.startTimer();
        }
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        int CONFIG_VERSION = 4;
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warning("You configuration is out of date! Some features may not work!");
            if (config.getInt("config-version") == 3) {
                logger.warning("The configuration system has changed for this version. Please delete your configs and restart the server.");
            }
        }

        ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
        if (worldsSection == null) {
            logger.severe("No worlds section found in config! Plugin is unable to function.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Set<String> worldKeys = worldsSection.getKeys(false);
        // loop through the worlds section and find the worlds under it
        for (String worldName : worldKeys) {
            World mainWorld = Bukkit.getWorld(worldName);
            // skip if the world is invalid
            if (mainWorld == null) {
                logger.warning(worldName + " is not a valid world, skipping...");
                continue;
            }
            // these are the worlds to display the timebar based on this world
            List<String> displayWorldNames = config.getStringList("worlds." + worldName);
            List<World> displayWorlds = new ArrayList<>();
            for (String name : displayWorldNames) {
                World world = Bukkit.getWorld(name);
                if (world != null) {
                    displayWorlds.add(world);
                } else {
                    logger.warning(worldName + " is not a valid world, skipping...");
                }
            }
            // make sure to add the world itself
            displayWorlds.add(mainWorld);

            WorldTimeTracker worldTimeTracker = new WorldTimeTracker(this, mainWorld, displayWorlds);
            worldTimeTrackers.add(worldTimeTracker);
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
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }
}
