package porker.fasttravel.manager;

import porker.fasttravel.model.FastTravelSession;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FastTravelManager {

    private final Map<UUID, FastTravelSession> sessions = new HashMap<>();
    private final porker.fasttravel.FastTravelPlugin plugin;

    public FastTravelManager(porker.fasttravel.FastTravelPlugin plugin) {
        this.plugin = plugin;
    }

    public void startSession(FastTravelSession session) {
        sessions.put(session.getPlayer().getUniqueId(), session);
        session.start();
    }

    public FastTravelSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void endSession(Player player, boolean teleportToDestination) {
        FastTravelSession session = sessions.remove(player.getUniqueId());
        if (session != null) {
            session.end(teleportToDestination);
        }
    }

    public void cleanupAll() {
        for (FastTravelSession session : sessions.values()) {
            session.forceEnd();
        }
        sessions.clear();
    }
}