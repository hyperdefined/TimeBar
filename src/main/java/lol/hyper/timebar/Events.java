package lol.hyper.timebar;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {

    private final TimeBar timeBar;

    public Events(TimeBar timeBar) {
        this.timeBar = timeBar;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        timeBar.timeTracker.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        timeBar.timeTracker.removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        // Check if we gotta remove the timebar when the player moves worlds
        if (timeBar.config.getBoolean("show-time-in-other-worlds")) {
            return;
        }

        World world = event.getFrom();

        // If the player travels overworld -> any other world, remove the bar
        // If the player travels any other world -> overworld, add the bar
        if (world.getName().equalsIgnoreCase(timeBar.config.getString("overworld"))) {
            timeBar.timeTracker.removePlayer(event.getPlayer());
        } else {
            timeBar.timeTracker.addPlayer(event.getPlayer());
        }
    }
}
