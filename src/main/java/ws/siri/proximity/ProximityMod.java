package ws.siri.proximity;

import ws.siri.proximity.backend.SubscriptionConnection;
import ws.siri.proximity.watcher.VolumeUpdater;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.tyrus.server.Server;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
        modid = ProximityMod.MODID,
        name = ProximityMod.MODNAME,
        version = ProximityMod.VERSION)
public class ProximityMod { // select ExampleMod and hit shift+F6 to rename it

    public static final String MODID = "discordproximity";      // the id of your mod, it should never change, it is used by forge and servers to identify your mods
    public static final String MODNAME = "Discord Proximity";// the name of your mod
    public static final String VERSION = "0.1.1";           // the current version of your mod

    public static final Logger logger = Logger.getLogger("DiscordProximity");

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.log(Level.INFO, "Starting web server at port 25560.");
        Server server = new Server("localhost", 25560, "/api", null, SubscriptionConnection.class);
        try {
            server.start();
            logger.log(Level.INFO, "WS server started.");
        } catch (Exception e) {
            logger.log(Level.INFO, "Failed to start web server " + e + ".");
        }
        MinecraftForge.EVENT_BUS.register(new VolumeUpdater());
    }
}
