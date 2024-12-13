package porker.fasttravel;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import porker.fasttravel.commands.*;
import porker.fasttravel.listeners.PlayerListener;
import porker.fasttravel.manager.FastTravelManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FastTravelPlugin extends JavaPlugin {

    private static FastTravelPlugin instance;
    private FastTravelManager ftManager;
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        instance = this;

        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        protocolManager = ProtocolLibrary.getProtocolManager();
        ftManager = new FastTravelManager(this);

        getCommand("ft").setExecutor(new FtCommand(this));
        getCommand("fta").setExecutor(new FtaCommand(this));
        getCommand("ftc").setExecutor(new FtcCommand(this));
        getCommand("ftaccept").setExecutor(new FtAcceptCommand(this));
        getCommand("ftdeny").setExecutor(new FtDenyCommand());


        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        ftManager.cleanupAll();
    }

    public static FastTravelPlugin getInstance() {
        return instance;
    }

    public FastTravelManager getFastTravelManager() {
        return ftManager;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

}