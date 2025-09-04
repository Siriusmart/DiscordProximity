package ws.siri.proximity;

import ws.siri.proximity.backend.SubscriptionConnection;
import ws.siri.proximity.commands.ExampleCommand;
import ws.siri.proximity.config.ConfigHandler;
import ws.siri.proximity.events.ExampleKeybindListener;
import ws.siri.proximity.watcher.VolumeUpdater;
import ws.siri.proximity.hud.ExampleHUD;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

// import org.eclipse.jetty.server.Handler;
// import org.eclipse.jetty.server.Server;
// import org.eclipse.jetty.servlet.ServletContextHandler;

import org.glassfish.tyrus.server.Server;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid = ProximityMod.MODID,
        name = ProximityMod.MODNAME,
        version = ProximityMod.VERSION)
public class ProximityMod { // select ExampleMod and hit shift+F6 to rename it

    public static final String MODID = "discordproximity";      // the id of your mod, it should never change, it is used by forge and servers to identify your mods
    public static final String MODNAME = "Discord Proximity";// the name of your mod
    public static final String VERSION = "1.0";           // the current version of your mod

    // this method is one entry point of you mod
    // it is called by forge when minecraft is starting
    // it is called before the other methods below
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // loads the config file from the disk
        ConfigHandler.loadConfig(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        Logger.getLogger("Proximity init").log(Level.SEVERE, "Starting web server at port 25560");
        // Server server = new Server(new InetSocketAddress("127.0.0.1", 25560));
        Server server = new Server("localhost", 25560, "/api", null, SubscriptionConnection.class);
        // Handler handler = new ServletContextHandler();
        // server.setHandler(handler);
        try {
            server.start();
        } catch (Exception e) {
            Logger.getLogger("Proximity init").log(Level.INFO, "Failed to start web server " + e);
        }
        // register your commands here
        ClientCommandHandler.instance.registerCommand(new ExampleCommand());

        // the ExampleKeybind has a method with the @SubscribeEvent annotation
        // for that code to run, that class needs to be registered on the MinecraftForge EVENT_BUS
        // register your other EventHandlers here
        MinecraftForge.EVENT_BUS.register(new ExampleKeybindListener());
        MinecraftForge.EVENT_BUS.register(new ExampleHUD());
        MinecraftForge.EVENT_BUS.register(new VolumeUpdater());

        if (Loader.isModLoaded("patcher")) {
            // this code will only run if the mod with id "patcher" is loaded
            // you can use it to load or not while modules of your mod that depends on other mods
        }

    }
}
