package porker.fasttravel.commands;

import porker.fasttravel.FastTravelPlugin;
import porker.fasttravel.model.FastTravelSession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FtCommand implements CommandExecutor {

    private final FastTravelPlugin plugin;

    public FtCommand(FastTravelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(ChatColor.RED + "Usage: /ft <destinationPlayer | x y z | player1 player2 | PlayerD x y z>");
            return true;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                p.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            Location dest = target.getLocation();
            FastTravelSession session = new FastTravelSession(plugin, p, dest, target.getName(), true);
            plugin.getFastTravelManager().startSession(session);
            return true;
        }

        if (args.length == 3) {
            Double x = parseDouble(args[0]);
            Double y = parseDouble(args[1]);
            Double z = parseDouble(args[2]);
            if (x == null || y == null || z == null) {
                p.sendMessage(ChatColor.RED + "Invalid coordinates.");
                return true;
            }
            Location dest = new Location(p.getWorld(), x, y, z);
            String name = ((int)(double)x)+", "+((int)(double)y)+", "+((int)(double)z);
            FastTravelSession session = new FastTravelSession(plugin, p, dest, name, false);
            plugin.getFastTravelManager().startSession(session);
            return true;
        }

        if (args.length == 2) {
            Player source = Bukkit.getPlayerExact(args[0]);
            Player destP = Bukkit.getPlayerExact(args[1]);
            if (source == null || destP == null) {
                p.sendMessage(ChatColor.RED + "One of the players was not found.");
                return true;
            }

            if (!source.equals(p)) {
                p.sendMessage(ChatColor.RED + "You must be the source player.");
                return true;
            }

            Location dest = destP.getLocation();
            FastTravelSession session = new FastTravelSession(plugin, source, dest, destP.getName(), true);
            plugin.getFastTravelManager().startSession(session);
            return true;
        }

        if (args.length == 4) {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                p.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            Double x = parseDouble(args[1]);
            Double y = parseDouble(args[2]);
            Double z = parseDouble(args[3]);
            if (x == null || y == null || z == null) {
                p.sendMessage(ChatColor.RED + "Invalid coordinates.");
                return true;
            }
            Location coords = new Location(p.getWorld(), x, y, z);
            target.teleport(coords);
            p.sendMessage(ChatColor.GREEN + "Teleported " + target.getName() + " to " + ((int)(double)x)+", "+((int)(double)y)+", "+((int)(double)z));
            return true;
        }

        p.sendMessage(ChatColor.RED + "Invalid usage.");
        return true;
    }

    private Double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}