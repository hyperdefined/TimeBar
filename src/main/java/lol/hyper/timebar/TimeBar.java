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

import lol.hyper.hyperlib.HyperLib;
import lol.hyper.hyperlib.bstats.bStats;
import lol.hyper.hyperlib.releases.modrinth.ModrinthPlugin;
import lol.hyper.hyperlib.releases.modrinth.ModrinthRelease;
import lol.hyper.hyperlib.utils.TextUtils;
import lol.hyper.timebar.commands.CommandTimeBar;
import lol.hyper.timebar.events.PlayerJoinLeave;
import lol.hyper.timebar.events.WorldChange;
import lol.hyper.timebar.papi.TimeBarExpansion;
import lol.hyper.timebar.tracker.WorldTimeTracker;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public final class TimeBar extends JavaPlugin {

    public final File configFile = new File(this.getDataFolder(), "config.yml");
    public final File realisticSeasonsConfigFile = new File(this.getDataFolder(), "realisticseasons.yml");
    public final File advancedSeasonsConfigFile = new File(this.getDataFolder(), "advancedseasons.yml");
    public final Logger logger = this.getLogger();
    public FileConfiguration config;
    public FileConfiguration realisticSeasonsConfig;
    public FileConfiguration advancedSeasonsConfig;
    public final Set<Player> enabledBossBar = new HashSet<>();
    public final List<WorldTimeTracker> worldTimeTrackers = new ArrayList<>();

    public PlayerJoinLeave playerJoinLeave;
    public WorldChange worldChange;
    public CommandTimeBar commandReload;
    public HyperLib hyperLib;
    public TextUtils textUtils;

    public boolean papiSupport = false;
    public boolean realisticSeasons = false;
    public boolean advancedSeasons = false;
    public BossBar.Color bossBarColor;

    @Override
    public void onEnable() {
        hyperLib = new HyperLib(this);
        hyperLib.setup();

        bStats bstats = new bStats(hyperLib, 10674);
        bstats.setup();

        textUtils = new TextUtils(hyperLib);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiSupport = true;
            logger.info("PlaceholderAPI is detected! Enabling support.");
            TimeBarExpansion expansion = new TimeBarExpansion(this);
            if (expansion.register()) {
                logger.info("Successfully registered placeholders!");
            } else {
                logger.warning("Unable to register placeholders!");
            }
        }
        if (Bukkit.getPluginManager().getPlugin("RealisticSeasons") != null) {
            realisticSeasons = true;
            logger.info("RealisticSeasons is detected! Enabling support.");
        }
        if (Bukkit.getPluginManager().getPlugin("AdvancedSeasons") != null) {
            advancedSeasons = true;
            logger.info("AdvancedSeasons is detected! Enabling support.");
        }

        loadConfig();
        playerJoinLeave = new PlayerJoinLeave(this);
        worldChange = new WorldChange(this);
        commandReload = new CommandTimeBar(this);

        this.getCommand("timebar").setExecutor(commandReload);

        Bukkit.getServer().getPluginManager().registerEvents(playerJoinLeave, this);
        Bukkit.getServer().getPluginManager().registerEvents(worldChange, this);

        Bukkit.getAsyncScheduler().runNow(this, scheduledTask -> {
            ModrinthPlugin modrinthPlugin = new ModrinthPlugin("3w9zYjq5");
            modrinthPlugin.get();

            ModrinthRelease release = modrinthPlugin.getReleaseByVersion(this.getPluginMeta().getVersion());
            if (release == null) {
                logger.warning("You are running a version not published.");
            } else {
                int buildsBehind = modrinthPlugin.buildsVersionsBehind(release);
                if (buildsBehind > 0) {
                    ModrinthRelease latest = modrinthPlugin.getLatestRelease();
                    if (latest != null) {
                        logger.info("You are " + buildsBehind + " versions behind. Please update!");
                        logger.info("The latest version is " + latest.getVersion());
                        logger.info(latest.getVersionPage());
                    }
                } else {
                    logger.info("Yay! You are running the latest version.");
                }
            }
        });

        for (WorldTimeTracker worldTimeTracker : worldTimeTrackers) {
            worldTimeTracker.startTimer();
        }
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        int CONFIG_VERSION = 5;
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warning("You configuration is out of date! Some features may not work!");
            // don't feel like adding a config updater
            if (config.getInt("config-version") == 3) {
                logger.warning("The configuration system has changed for this version. Please delete your configs and restart the server.");
            }
        }

        // make sure there are worlds on the list
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
                logger.warning(worldName + " is not a valid world, skipping. If it is a valid world, wait for your server to load then try '/timebar reload'");
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
                    logger.warning(worldName + " is not a valid world, skipping. If it is a valid world, wait for your server to load then try '/timebar reload'");
                }
            }

            WorldTimeTracker worldTimeTracker = new WorldTimeTracker(this, mainWorld, displayWorlds);
            worldTimeTrackers.add(worldTimeTracker);
        }

        String color = config.getString("titlebar-color");
        if (color == null) {
            // default to blue since I like it
            bossBarColor = BossBar.Color.BLUE;
        } else {
            // convert to uppercase and see if it's a real color
            color = color.toUpperCase(Locale.ROOT);
            try {
                bossBarColor = BossBar.Color.valueOf(color);
            } catch (IllegalArgumentException exception) {
                logger.warning(color + " is not a valid bossbar color. Defaulting to blue.");
                bossBarColor = BossBar.Color.BLUE;
            }
        }

        if (realisticSeasons) {
            if (!realisticSeasonsConfigFile.exists()) {
                this.saveResource("realisticseasons.yml", true);
            }
            realisticSeasonsConfig = YamlConfiguration.loadConfiguration(realisticSeasonsConfigFile);
            int REALISTIC_SEASONS_CONFIG_VERSION = 4;
            if (realisticSeasonsConfig.getInt("config-version") != REALISTIC_SEASONS_CONFIG_VERSION) {
                logger.warning("Your /plugins/TimeBar/realisticseasons.yml configuration is out of date! Some features may not work!");
            }
        }
        if (advancedSeasons) {
            if (!advancedSeasonsConfigFile.exists()) {
                this.saveResource("advancedseasons.yml", true);
            }
            advancedSeasonsConfig = YamlConfiguration.loadConfiguration(advancedSeasonsConfigFile);
            int ADVANCED_SEASONS_CONFIG_VERSION = 1;
            if (advancedSeasonsConfig.getInt("config-version") != ADVANCED_SEASONS_CONFIG_VERSION) {
                logger.warning("Your /plugins/TimeBar/advancedseasons.yml configuration is out of date! Some features may not work!");
            }
        }
    }

    public WorldTimeTracker getPlayerTracker(Player player) {
        return worldTimeTrackers.stream().filter(worldTimeTracker -> worldTimeTracker.worldGroup().contains(player.getWorld())).findFirst().orElse(null);
    }
}
