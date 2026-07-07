package mods.flammpfeil.slashblade.bridge;

import mods.flammpfeil.slashblade.Reference;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = SlashBladeLegacyBridge.MOD_ID,
        name = SlashBladeLegacyBridge.MOD_NAME,
        version = Reference.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:" + Reference.MOD_ID
)
public class SlashBladeLegacyBridge {
    public static final String MOD_ID = "flammpfeil.slashblade";
    public static final String MOD_NAME = "SlashBlade Legacy Compatibility Bridge";

    private Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("Loaded {} for {}", MOD_NAME, Reference.MOD_ID);
    }
}
