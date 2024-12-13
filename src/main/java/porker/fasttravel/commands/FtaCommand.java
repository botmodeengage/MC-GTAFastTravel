package porker.fasttravel.commands;

import porker.fasttravel.FastTravelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FtaCommand implements CommandExecutor {

    private final FastTravelPlugin plugin;

    public FtaCommand(FastTravelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            p.sendMessage(ChatColor.RED + "Usage: /fta <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            p.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (target.equals(p)) {
            p.sendMessage(ChatColor.RED + "You cannot fast travel to yourself.");
            return true;
        }

        target.sendMessage(ChatColor.YELLOW + p.getName() + " is requesting to fast travel to you. Type /ftaccept or /ftdeny.");
        p.sendMessage(ChatColor.GREEN + "Fast travel request sent to " + target.getName() + ".");
        RequestManager.setPendingRequest(target, p);

        return true;
    }
}