package porker.fasttravel.model;

import porker.fasttravel.FastTravelPlugin;
import porker.fasttravel.util.CameraUtil;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import io.papermc.lib.PaperLib; // Make sure PaperLib is on classpath
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FastTravelSession {

    private final FastTravelPlugin plugin;
    private final Player player;
    private final Location startLocation;
    private final Location destinationLocation;
    private final String destinationName;
    private final boolean destinationIsPlayer;
    private ArmorStand cameraStand;
    private boolean cancelled = false;
    private boolean started = false;
    private GameMode originalGameMode;

    private NPC npcStart;
    private NPC npcDestination;

    private static final ExecutorService ASYNC_EXECUTOR = Executors.newSingleThreadExecutor();

    public FastTravelSession(FastTravelPlugin plugin, Player player, Location destination, String destinationName, boolean destinationIsPlayer) {
        this.plugin = plugin;
        this.player = player;
        this.startLocation = player.getLocation().clone();
        this.destinationLocation = destination.clone();
        this.destinationName = destinationName;
        this.destinationIsPlayer = destinationIsPlayer;
    }

    public Player getPlayer() {
        return player;
    }

    public void start() {
        if (started) return;
        started = true;

        originalGameMode = player.getGameMode();

        if (destinationIsPlayer) {
            player.sendActionBar(ChatColor.YELLOW + "Fast traveling to " + destinationName);
        } else {
            player.sendActionBar(ChatColor.YELLOW + "Fast traveling to " + destinationName);
        }

        if (destinationIsPlayer) {
            Player target = Bukkit.getPlayerExact(destinationName);
            if (target != null && target.isOnline()) {
                target.sendMessage(ChatColor.YELLOW + player.getName() + " is fast traveling to you.");
            }
        }

        cameraStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        cameraStand.setVisible(false);
        cameraStand.setGravity(false);
        cameraStand.setMarker(true);
        cameraStand.setInvulnerable(true);
        cameraStand.setSilent(true);
        cameraStand.setCustomNameVisible(false);

        if (plugin.getServer().getPluginManager().isPluginEnabled("Citizens")) {
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            npcStart = registry.createNPC(EntityType.PLAYER, player.getName());
            npcStart.setName(player.getName());
            Location npcLoc = startLocation.clone();
            npcLoc.setPitch(0);
            npcLoc.setYaw(startLocation.getYaw());
            npcStart.spawn(npcLoc);
            removeNPCGravity(npcStart);
        }

        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(cameraStand);

        double interval = 8;
        int radius = 2;
        preGenerateRouteChunksAsync(startLocation, destinationLocation, interval, radius)
                .thenRunAsync(() -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Location initialThirdPerson = getThirdPersonView(startLocation, 3, 2, 20);
                        smoothMoveCamera(cameraStand.getLocation(), initialThirdPerson, 20, () -> {
                            Bukkit.getScheduler().runTaskLater(plugin, this::startCameraSequence, 20L);
                        });
                    });
                }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    private void startCameraSequence() {
        double distance = startLocation.distance(destinationLocation);

        double panDuration;
        if (distance <= 300) {
            panDuration = 5.0;
        } else if (distance <= 500) {
            panDuration = 8.0;
        } else if (distance <= 1000) {
            panDuration = 12.0;
        } else {
            panDuration = 16.0;
        }

        final long panTicks = (long) (panDuration * 20);

        int stepTime = 40;

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (cancelled || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                step++;
                switch(step) {
                    case 1:
                        playDeepNote(player);
                        break;
                    case 2:
                        playDeepNote(player);
                        CameraUtil.moveCameraStand(cameraStand, topDownView(startLocation, 10), 2);
                        break;
                    case 3:
                        playDeepNote(player);
                        CameraUtil.moveCameraStand(cameraStand, topDownView(startLocation, 25), 2);
                        break;
                    case 4:
                        playDeepNote(player);
                        CameraUtil.moveCameraStand(cameraStand, topDownView(startLocation, 45), 2);
                        break;
                    case 5:
                        this.cancel();
                        preparePan(panTicks);
                        break;
                }
            }
        }.runTaskTimer(plugin, 0, stepTime);
    }

    private void preparePan(long panTicks) {
        Location peak1 = cameraStand.getLocation();
        Location peak2 = topDownView(destinationLocation, 100);

        Bukkit.getScheduler().runTaskLater(plugin, () -> startPan(panTicks, peak1, peak2), 200L);
    }

    private void startPan(long panTicks, Location peak1, Location peak2) {
        new BukkitRunnable() {
            long elapsed = 0;
            @Override
            public void run() {
                if (cancelled || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                elapsed++;
                double t = (double)elapsed / (double)panTicks;
                if (t >= 1.0) {
                    this.cancel();
                    cameraStand.teleport(peak2);
                    startZoomIn();
                    return;
                }

                double eased = easeInOutQuad(t);

                double nx = peak1.getX() + (peak2.getX()-peak1.getX())*eased;
                double ny = peak1.getY() + (peak2.getY()-peak1.getY())*eased;
                double nz = peak1.getZ() + (peak2.getZ()-peak1.getZ())*eased;

                Location nloc = new Location(peak1.getWorld(), nx, ny, nz, peak1.getYaw(), peak1.getPitch());
                // Chunks should already be loaded by pre-generation
                cameraStand.teleport(nloc);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void startZoomIn() {
        if (npcStart != null && npcStart.isSpawned()) {
            npcStart.despawn();
            CitizensAPI.getNPCRegistry().deregister(npcStart);
            npcStart = null;
        }

        if (plugin.getServer().getPluginManager().isPluginEnabled("Citizens")) {
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            npcDestination = registry.createNPC(EntityType.PLAYER, player.getName());
            npcDestination.setName(player.getName());
            Location npcLoc = destinationLocation.clone();
            npcLoc.setPitch(0);
            npcLoc.setYaw(destinationLocation.getYaw());
            npcDestination.spawn(npcLoc);
            removeNPCGravity(npcDestination);
        }

        loadChunkAt(destinationLocation);

        int stepTime = 40;

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run(){
                if (cancelled || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                step++;
                switch(step) {
                    case 1:
                        playDeepNote(player);
                        CameraUtil.moveCameraStand(cameraStand, topDownView(destinationLocation, 45), 2);
                        break;
                    case 2:
                        playDeepNote(player);
                        CameraUtil.moveCameraStand(cameraStand, topDownView(destinationLocation, 25), 2);
                        break;
                    case 3:
                        playDeepNote(player);
                        CameraUtil.moveCameraStand(cameraStand, topDownView(destinationLocation, 10), 2);
                        break;
                    case 4:
                        playDeepNote(player);
                        CameraUtil.moveCameraStand(cameraStand, getThirdPersonView(destinationLocation, 3, 2, 20, 220), 2);
                        break;
                    case 5:
                        this.cancel();
                        smoothMoveCamera(cameraStand.getLocation(), destinationLocation, 20, () -> {
                            plugin.getFastTravelManager().endSession(player, true);
                        });
                        break;
                }
            }
        }.runTaskTimer(plugin, 0, stepTime);
    }

    public void playDeepNote(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    public void end(boolean teleportToDestination) {
        if (cameraStand != null && !cameraStand.isDead()) {
            cameraStand.remove();
        }

        if (npcStart != null) {
            if (npcStart.isSpawned()) npcStart.despawn();
            CitizensAPI.getNPCRegistry().deregister(npcStart);
            npcStart = null;
        }

        if (npcDestination != null) {
            if (npcDestination.isSpawned()) npcDestination.despawn();
            CitizensAPI.getNPCRegistry().deregister(npcDestination);
            npcDestination = null;
        }

        player.setSpectatorTarget(null);
        player.setGameMode(originalGameMode);

        if (!cancelled && teleportToDestination) {
            player.teleport(destinationLocation);
        } else {
            player.teleport(startLocation);
        }
    }

    public void forceEnd() {
        cancelled = true;
        end(false);
    }

    public void cancel() {
        cancelled = true;
    }

    private Location topDownView(Location base, double height) {
        Location loc = base.clone().add(0, height, 0);
        loc.setPitch(90f);
        loc.setYaw(0f);
        return loc;
    }

    private Location getThirdPersonView(Location base, double distanceBack, double height, float pitchAngle) {
        return getThirdPersonView(base, distanceBack, height, pitchAngle, 180);
    }

    private Location getThirdPersonView(Location base, double distanceBack, double height, float pitchAngle, float yawOffset) {
        Location loc = base.clone();
        float yaw = loc.getYaw();
        double rad = Math.toRadians(yaw);
        double dx = -Math.sin(rad) * distanceBack;
        double dz = Math.cos(rad) * distanceBack;
        loc.add(dx, height, dz);
        loc.setYaw(yaw + yawOffset);
        loc.setPitch(pitchAngle);
        return loc;
    }

    private void loadChunkAt(Location loc) {
        if (loc.getWorld() != null) {
            PaperLib.getChunkAtAsync(loc.getWorld(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4, true);
        }
    }

    /**
     *  PaperLib
     */
    private CompletableFuture<Void> preGenerateRouteChunksAsync(Location from, Location to, double interval, int radius) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        ASYNC_EXECUTOR.execute(() -> {
            Set<ChunkCoords> unique = new HashSet<>();
            if (from.getWorld() == null || to.getWorld() == null || !from.getWorld().equals(to.getWorld())) {
                if (from.getWorld() != null) {
                    int cx = from.getChunk().getX();
                    int cz = from.getChunk().getZ();
                    addChunksInRadius(cx, cz, radius, unique);
                }
                if (to.getWorld() != null) {
                    int cx = to.getChunk().getX();
                    int cz = to.getChunk().getZ();
                    addChunksInRadius(cx, cz, radius, unique);
                }
            } else {
                World world = from.getWorld();
                double distance = from.distance(to);
                Vector dir = to.toVector().subtract(from.toVector()).normalize();
                int steps = (int)(distance / interval) + 1;

                for (int i = 0; i <= steps; i++) {
                    double curDist = Math.min(i * interval, distance);
                    Vector pos = from.toVector().add(dir.clone().multiply(curDist));
                    Location sample = new Location(world, pos.getX(), pos.getY(), pos.getZ());
                    int cx = sample.getChunk().getX();
                    int cz = sample.getChunk().getZ();
                    addChunksInRadius(cx, cz, radius, unique);
                }
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                World w = player.getWorld();
                if (w == null) {
                    future.complete(null);
                    return;
                }

                List<CompletableFuture<Chunk>> loadFutures = new ArrayList<>();
                for (ChunkCoords cc : unique) {
                    loadFutures.add(PaperLib.getChunkAtAsync(w, cc.x, cc.z, true));
                }

                CompletableFuture.allOf(loadFutures.toArray(new CompletableFuture[0]))
                        .thenRun(() -> future.complete(null));
            });
        });

        return future;
    }

    private void addChunksInRadius(int cx, int cz, int radius, Set<ChunkCoords> set) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                set.add(new ChunkCoords(x, z));
            }
        }
    }

    private double easeInOutQuad(double t) {
        return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    private void smoothMoveCamera(Location start, Location end, int durationTicks, Runnable callback) {
        new BukkitRunnable() {
            int elapsed = 0;
            @Override
            public void run() {
                if (cancelled || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                elapsed++;
                double progress = (double)elapsed / (double)durationTicks;
                if (progress >= 1.0) {
                    this.cancel();
                    cameraStand.teleport(end);
                    if (callback != null) callback.run();
                    return;
                }

                double eased = easeInOutQuad(progress);
                double nx = start.getX() + (end.getX() - start.getX()) * eased;
                double ny = start.getY() + (end.getY() - start.getY()) * eased;
                double nz = start.getZ() + (end.getZ() - start.getZ()) * eased;
                float yaw = (float)(start.getYaw() + (end.getYaw() - start.getYaw()) * eased);
                float pitch = (float)(start.getPitch() + (end.getPitch() - start.getPitch()) * eased);

                Location nloc = new Location(start.getWorld(), nx, ny, nz, yaw, pitch);
                cameraStand.teleport(nloc);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void removeNPCGravity(NPC npc) {
        if (npc != null && npc.isSpawned() && npc.getEntity() instanceof LivingEntity le) {
            le.setGravity(false);
        }
    }

    private static class ChunkCoords {
        final int x, z;
        ChunkCoords(int x, int z) {
            this.x = x; this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ChunkCoords)) return false;
            ChunkCoords other = (ChunkCoords)o;
            return this.x == other.x && this.z == other.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x,z);
        }
    }
}