package porker.fasttravel.commands;

import porker.fasttravel.FastTravelPlugin;
import porker.fasttravel.model.FastTravelSession;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FtAcceptCommand implements CommandExecutor {

    private final FastTravelPlugin plugin;

    public FtAcceptCommand(FastTravelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player requester = RequestManager.getRequester(p);
        if (requester == null || !requester.isOnline()) {
            p.sendMessage(ChatColor.RED + "No active fast travel request to accept.");
            return true;
        }

        p.sendMessage(ChatColor.GREEN + "You accepted the fast travel request from " + requester.getName() + ".");
        requester.sendMessage(ChatColor.GREEN + p.getName() + " accepted your fast travel request.");

        Location dest = p.getLocation();
        FastTravelSession session = new FastTravelSession(plugin, requester, dest, p.getName(), true);
        plugin.getFastTravelManager().startSession(session);

        RequestManager.removeRequest(p);
        return true;
    }
}