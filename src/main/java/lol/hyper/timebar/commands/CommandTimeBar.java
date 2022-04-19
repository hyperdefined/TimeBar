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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CommandTimeBar implements TabExecutor {

    private final TimeBar timeBar;
    private final MiniMessage miniMessage;

    public CommandTimeBar(TimeBar timeBar) {
        this.timeBar = timeBar;
        this.miniMessage = timeBar.miniMessage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            timeBar.getAdventure().sender(sender).sendMessage(miniMessage.deserialize("<green>TimeBar version" + timeBar.getDescription().getVersion() + ". Created by hyperdefined.</green>"));
            return true;
        }

        switch (args[0]) {
            case "reload": {
                if (sender.hasPermission("timebar.reload")) {
                    Bukkit.getScheduler().cancelTask(timeBar.timeBarTask);
                    timeBar.loadConfig();
                    timeBar.getAdventure().sender(sender).sendMessage(miniMessage.deserialize("<green>Configuration reloaded!</green>"));
                } else {
                    timeBar.getAdventure().sender(sender).sendMessage(miniMessage.deserialize("<red>You do not have permission for this command.</red>"));
                }
                return true;
            }
            case "on": {
                if (sender instanceof ConsoleCommandSender) {
                    timeBar.getAdventure().sender(sender).sendMessage(miniMessage.deserialize("<red>You must be a player for this command.</red>"));
                    return true;
                }
                Player player = (Player) sender;
                timeBar.getAdventure().player(player).showBossBar(timeBar.timeTracker);
                timeBar.getAdventure().player(player).sendMessage(miniMessage.deserialize("<green>TimeBar is now enabled.</green>"));
                timeBar.enabledBossBar.add(player);
                return true;
            }
            case "off": {
                if (sender instanceof ConsoleCommandSender) {
                    timeBar.getAdventure().sender(sender).sendMessage(miniMessage.deserialize("<red>You must be a player for this command.</red>"));
                    return true;
                }
                Player player = (Player) sender;
                timeBar.getAdventure().player(player).hideBossBar(timeBar.timeTracker);
                timeBar.getAdventure().player(player).sendMessage(miniMessage.deserialize("<green>TimeBar is now disabled.</green>"));
                timeBar.enabledBossBar.remove(player);
                return true;
            }
            default: {
                timeBar.getAdventure().sender(sender).sendMessage(miniMessage.deserialize("<red>Invalid sub-command. Valid options are: reload, on, off.</red>"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return Arrays.asList("reload", "on", "off");
    }
}
