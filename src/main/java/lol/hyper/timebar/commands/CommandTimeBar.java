package lol.hyper.timebar.commands;

import lol.hyper.timebar.TimeBar;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;

public class CommandTimeBar implements TabExecutor {

    private final TimeBar timeBar;

    public CommandTimeBar(TimeBar timeBar) {
        this.timeBar = timeBar;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN + "RandomEnchant version " + timeBar.getDescription().getVersion() + ". Created by hyperdefined.");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.isOp() || sender.hasPermission("randomenchant.reload")) {
                    Bukkit.getScheduler().cancelTask(timeBar.timeBarTask);
                    timeBar.loadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid sub-command.");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.singletonList("reload");
    }
}
