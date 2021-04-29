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

import lol.hyper.timebar.TimeBar;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class CommandTimeBar implements TabExecutor {

    private final TimeBar timeBar;

    public CommandTimeBar(TimeBar timeBar) {
        this.timeBar = timeBar;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN + "TimeBar version " + timeBar.getDescription().getVersion() + ". Created by hyperdefined.");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.isOp() || sender.hasPermission("timebar.reload")) {
                    Bukkit.getScheduler().cancelTask(timeBar.timeBarTask);
                    timeBar.loadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                }
            } else if (args[0].equalsIgnoreCase("on")) {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(ChatColor.RED + "You must be a player for this command.");
                    return true;
                }
                Player player = (Player) sender;
                if (!timeBar.timeTracker.getPlayers().contains(player)) {
                    timeBar.timeTracker.addPlayer(player);
                    sender.sendMessage(ChatColor.GREEN + "TimeBar is now enabled.");
                } else {
                    sender.sendMessage(ChatColor.RED + "TimeBar is already enabled.");
                }
            } else if (args[0].equalsIgnoreCase("off")) {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(ChatColor.RED + "You must be a player for this command.");
                    return true;
                }
                Player player = (Player) sender;
                if (timeBar.timeTracker.getPlayers().contains(player)) {
                    timeBar.timeTracker.removePlayer(player);
                    sender.sendMessage(ChatColor.GREEN + "TimeBar is now disabled.");
                } else {
                    sender.sendMessage(ChatColor.RED + "TimeBar is already disabled.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid sub-command.");
            }
        }
        return true;
    }

    @Override
    public List < String > onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Arrays.asList("reload", "on", "off");
    }
}