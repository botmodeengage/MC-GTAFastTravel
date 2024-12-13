package porker.fasttravel.commands;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RequestManager {
    private static final Map<Player, Player> pendingRequests = new HashMap<>();

    public static void setPendingRequest(Player target, Player requester) {
        pendingRequests.put(target, requester);
    }

    public static Player getRequester(Player target) {
        return pendingRequests.get(target);
    }

    public static void removeRequest(Player target) {
        pendingRequests.remove(target);
    }
}