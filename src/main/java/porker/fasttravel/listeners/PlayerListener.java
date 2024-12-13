package porker.fasttravel.listeners;

import porker.fasttravel.FastTravelPlugin;
import porker.fasttravel.model.FastTravelSession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final FastTravelPlugin plugin;

    public PlayerListener(FastTravelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        FastTravelSession session = plugin.getFastTravelManager().getSession(event.getPlayer());
        if (session != null) {
            session.forceEnd();
            plugin.getFastTravelManager().endSession(event.getPlayer(), false);
        }
    }
}