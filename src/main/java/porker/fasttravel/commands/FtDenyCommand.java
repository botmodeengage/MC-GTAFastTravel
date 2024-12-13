package porker.fasttravel.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FtDenyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player requester = RequestManager.getRequester(p);
        if (requester == null || !requester.isOnline()) {
            p.sendMessage(ChatColor.RED + "No active fast travel request to deny.");
            return true;
        }

        p.sendMessage(ChatColor.YELLOW + "You denied the fast travel request from " + requester.getName() + ".");
        requester.sendMessage(ChatColor.RED + p.getName() + " denied your fast travel request.");

        RequestManager.removeRequest(p);
        return true;
    }
}