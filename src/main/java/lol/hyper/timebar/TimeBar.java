package lol.hyper.timebar;

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

    public Events events;
    public CommandReload commandReload;

    @Override
    public void onEnable() {
        loadConfig();
        events =  new Events(this);
        commandReload = new CommandReload(this);

        this.getCommand("timebar").setExecutor(commandReload);

        Bukkit.getServer().getPluginManager().registerEvents(events, this);
    }

    private void startTimer() {
        timeBarTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            double time = Bukkit.getWorld("world").getTime();
            timeTracker.setProgress(time / 24000.0);
            if (time >= 23000) {
                timeTracker.setTitle("Dawn (Day " + Bukkit.getWorld("world").getFullTime() / 24000 + ")");
            }
            if (time >= 0 && time < 6000) {
                timeTracker.setTitle("Morning (Day " + Bukkit.getWorld("world").getFullTime() / 24000 + ")");
            }
            if (time >= 6000 && time < 9000) {
                timeTracker.setTitle("Noon (Day " + Bukkit.getWorld("world").getFullTime() / 24000 + ")");
            }
            if (time >= 9000 && time < 12000) {
                timeTracker.setTitle("Afternoon (Day " + Bukkit.getWorld("world").getFullTime() / 24000 + ")");
            }
            if (time >= 12000 && time < 14000) {
                timeTracker.setTitle("Sunset (Day " + Bukkit.getWorld("world").getFullTime() / 24000 + ")");
            }
            if (time >= 14000 && time < 18000) {
                timeTracker.setTitle("Night (Day " + Bukkit.getWorld("world").getFullTime() / 24000 + ")");
            }
            if (time >= 18000 && time < 23000) {
                timeTracker.setTitle("Midnight (Day " + Bukkit.getWorld("world").getFullTime() / 24000 + ")");
            }
        }, 0, 20);
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        int CONFIG_VERSION = 1;
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warning("You configuration is out of date! Some features may not work!");
        }
        startTimer();
    }
}
