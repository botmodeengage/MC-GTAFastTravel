package porker.fasttravel.util;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class CameraUtil {

    public static void moveCameraStand(ArmorStand stand, Location loc, int durationSeconds) {
        stand.teleport(loc);
    }
}