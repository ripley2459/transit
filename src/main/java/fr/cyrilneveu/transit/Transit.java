package fr.cyrilneveu.transit;

import fr.cyrilneveu.transit.command.TransitCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = TransitTags.MODID, version = TransitTags.VERSION, name = TransitTags.MODNAME, acceptedMinecraftVersions = "[1.12.2]")
public class Transit {
    public static final Logger LOGGER = LogManager.getLogger(TransitTags.MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new TransitCommand());
    }
}
