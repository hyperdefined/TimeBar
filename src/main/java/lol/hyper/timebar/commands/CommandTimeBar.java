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

package lol.hyper.timebar.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lol.hyper.timebar.TimeBar;
import lol.hyper.timebar.tracker.WorldTimeTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class CommandTimeBar implements BasicCommand {

    private final TimeBar timeBar;

    public CommandTimeBar(TimeBar timeBar) {
        this.timeBar = timeBar;
    }

    @Override
    public void execute(@NonNull CommandSourceStack source, String @NonNull [] args) {
        CommandSender sender = source.getSender();
        if (args.length == 0) {
            sender.sendMessage(Component.text("TimeBar version " + timeBar.getPluginMeta().getVersion() + ". Created by hyperdefined.", NamedTextColor.GREEN));
            return;
        }

        switch (args[0]) {
            case "reload" -> {
                if (sender.hasPermission("timebar.reload")) {
                    // this is a bit ugly, but it should be done in this order
                    // hide the bossbars, reload config, then start them again
                    for (WorldTimeTracker worldTimeTracker : timeBar.worldTimeTrackers) {
                        worldTimeTracker.hideBossBars();
                        worldTimeTracker.stopTimer();
                    }
                    timeBar.worldTimeTrackers.clear();
                    timeBar.loadConfig();
                    // add all players online back into trackers
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        timeBar.worldTimeTrackers.stream().filter(worldTimeTracker -> worldTimeTracker.worldGroup().contains(player.getWorld())).findFirst().ifPresent(tracker -> tracker.addPlayer(player));
                    }
                    for (WorldTimeTracker worldTimeTracker : timeBar.worldTimeTrackers) {
                        worldTimeTracker.startTimer();
                    }
                    sender.sendMessage(Component.text("Configuration reloaded!", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                }
                return;
            }
            case "on" -> {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(Component.text("You must be a player for this command.", NamedTextColor.RED));
                    return;
                }
                if (!sender.hasPermission("timebar.enable")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return;
                }
                if (timeBar.config.getBoolean("hold-clock-to-show")) {
                    sender.sendMessage(Component.text("You must be holding a clock to show/hide the TimeBar.", NamedTextColor.RED));
                    return;
                }
                Player player = (Player) sender;
                World world = player.getWorld();

                // show them the bossbar
                WorldTimeTracker tracker = timeBar.worldTimeTrackers.stream().filter(worldTimeTracker -> worldTimeTracker.worldGroup().contains(world)).findFirst().orElse(null);
                // showPlayer will add them to the list, but if their world doesn't have a tracker, add them manually
                if (tracker != null) {
                    tracker.showPlayer(player);
                } else {
                    timeBar.enabledBossBar.add(player);
                }
                player.sendMessage(Component.text("TimeBar is now enabled.", NamedTextColor.GREEN));
                return;
            }
            case "off" -> {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(Component.text("You must be a player for this command.", NamedTextColor.RED));
                    return;
                }
                if (!sender.hasPermission("timebar.disable")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return;
                }
                if (timeBar.config.getBoolean("hold-clock-to-show")) {
                    sender.sendMessage(Component.text("You must be holding a clock to show/hide the TimeBar.", NamedTextColor.RED));
                    return;
                }
                Player player = (Player) sender;
                World world = player.getWorld();

                // hide the bossbar
                WorldTimeTracker tracker = timeBar.worldTimeTrackers.stream().filter(worldTimeTracker -> worldTimeTracker.worldGroup().contains(world)).findFirst().orElse(null);
                // hidePlayer will remove them from the list, but if their world doesn't have a tracker, remove them manually
                if (tracker != null) {
                    tracker.hidePlayer(player);
                } else {
                    timeBar.enabledBossBar.remove(player);
                }
                player.sendMessage(Component.text("TimeBar is now disabled.", NamedTextColor.GREEN));
                return;
            }
            default -> sender.sendMessage(Component.text("Invalid sub-command. Valid options are: reload, on, off.", NamedTextColor.RED));
        }
        return;
    }

    @Override
    public @NonNull Collection<String> suggest(@NonNull CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            if (sender.hasPermission("timebar.reload")) {
                suggestions.add("reload");
            }
            if (sender.hasPermission("timebar.enable")) {
                suggestions.add("on");
            }
            if (sender.hasPermission("timebar.disable")) {
                suggestions.add("off");
            }
            return suggestions;
        }
        return Collections.emptyList();
    }
}
