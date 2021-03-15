package lol.hyper.timebar;

import lol.hyper.timebar.commands.CommandReload;
import lol.hyper.timebar.events.Events;
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
    public String timebarTitle = "";
    public String worldName = "";

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
                timeTracker.setTitle(parseString("Dawn"));
            }
            if (time >= 0 && time < 6000) {
                timeTracker.setTitle(parseString("Morning"));
            }
            if (time >= 6000 && time < 9000) {
                timeTracker.setTitle(parseString("Noon"));
            }
            if (time >= 9000 && time < 12000) {
                timeTracker.setTitle(parseString("Afternoon"));
            }
            if (time >= 12000 && time < 14000) {
                timeTracker.setTitle(parseString("Sunset"));
            }
            if (time >= 14000 && time < 18000) {
                timeTracker.setTitle(parseString("Night"));
            }
            if (time >= 18000 && time < 23000) {
                timeTracker.setTitle(parseString("Midnight"));
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

        timebarTitle = config.getString("timebar-title");
        worldName = config.getString("world-to-track-time");

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
        String title = "";

        if (timebarTitle.contains("{TIME}")) {
            title = timebarTitle.replace("{TIME}", time);
        }

        if (timebarTitle.contains("{DAYCOUNT}")) {
            title = timebarTitle.replace("{TIME}", String.valueOf(Bukkit.getWorld(worldName).getFullTime() / 24000));
        }
        return title;
    }
}
