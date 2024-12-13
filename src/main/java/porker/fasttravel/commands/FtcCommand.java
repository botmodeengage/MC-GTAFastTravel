package porker.fasttravel.commands;

import porker.fasttravel.FastTravelPlugin;
import porker.fasttravel.model.FastTravelSession;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FtcCommand implements CommandExecutor {

    private final FastTravelPlugin plugin;

    public FtcCommand(FastTravelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        FastTravelSession session = plugin.getFastTravelManager().getSession(p);
        if (session == null) {
            p.sendMessage(ChatColor.RED + "You are not currently fast traveling.");
            return true;
        }

        plugin.getFastTravelManager().endSession(p, false);
        p.sendMessage(ChatColor.GREEN + "Fast travel cancelled.");
        return true;
    }
}