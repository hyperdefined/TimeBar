package lol.hyper.timebar.events;

import lol.hyper.timebar.TimeBar;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Events implements Listener {

    private final TimeBar timeBar;

    public Events(TimeBar timeBar) {
        this.timeBar = timeBar;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        World world = event.getPlayer().getWorld();
        if (!timeBar.config.getStringList("worlds-to-show-in").contains(world.getName())) {
            timeBar.timeTracker.removePlayer(event.getPlayer());
        } else {
            timeBar.timeTracker.addPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        timeBar.timeTracker.removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerTeleportEvent event) {
        // Check the worlds list in the config, if it's empty, then we can ignore this event
        if (timeBar.config.getStringList("worlds-to-show-in").isEmpty()) {
            return;
        }

        World worldTo = event.getTo().getWorld();

        // Check to see if the player is going to a world that the TimeBar is enabled in
        // If the player goes to a world on the list, show them the bar
        // If not, remove it
        if (timeBar.config.getStringList("worlds-to-show-in").contains(worldTo.getName())) {
            timeBar.timeTracker.addPlayer(event.getPlayer());
        } else {
            timeBar.timeTracker.removePlayer(event.getPlayer());
        }
    }
}