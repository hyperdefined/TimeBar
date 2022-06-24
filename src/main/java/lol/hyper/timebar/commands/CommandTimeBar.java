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
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    private final BukkitAudiences audiences;

    public CommandTimeBar(TimeBar timeBar) {
        this.timeBar = timeBar;
        this.audiences = timeBar.getAdventure();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            audiences.sender(sender).sendMessage(Component.text("TimeBar version " + timeBar.getDescription().getVersion() + ". Created by hyperdefined.").color(NamedTextColor.GREEN));
            return true;
        }

        switch (args[0]) {
            case "reload": {
                if (sender.hasPermission("timebar.reload")) {
                    timeBar.loadConfig();
                    timeBar.startTimer();
                    audiences.sender(sender).sendMessage(Component.text("Configuration reloaded!").color(NamedTextColor.GREEN));
                } else {
                    audiences.sender(sender).sendMessage(Component.text("You do not have permission for this command.").color(NamedTextColor.RED));
                }
                return true;
            }
            case "on": {
                if (sender instanceof ConsoleCommandSender) {
                    audiences.sender(sender).sendMessage(Component.text("You must be a player for this command.").color(NamedTextColor.RED));
                    return true;
                }
                Player player = (Player) sender;
                audiences.player(player).showBossBar(timeBar.timeTracker);
                audiences.player(player).sendMessage(Component.text("TimeBar is now enabled.").color(NamedTextColor.GREEN));
                timeBar.enabledBossBar.add(player);
                return true;
            }
            case "off": {
                if (sender instanceof ConsoleCommandSender) {
                    audiences.sender(sender).sendMessage(Component.text("You must be a player for this command.").color(NamedTextColor.RED));
                    return true;
                }
                Player player = (Player) sender;
                audiences.player(player).hideBossBar(timeBar.timeTracker);
                audiences.player(player).sendMessage(Component.text("TimeBar is now disabled.").color(NamedTextColor.GREEN));
                timeBar.enabledBossBar.remove(player);
                return true;
            }
            default: {
                audiences.sender(sender).sendMessage(Component.text("Invalid sub-command. Valid options are: reload, on, off.").color(NamedTextColor.RED));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return Arrays.asList("reload", "on", "off");
    }
}
