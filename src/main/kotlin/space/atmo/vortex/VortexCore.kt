package space.atmo.vortex


import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge.EVENT_BUS
import net.neoforged.neoforge.event.RegisterCommandsEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import space.atmo.vortex.commands.VxCommands
import space.atmo.vortex.handlers.VxEventHandler
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/**
 * Main mod class.
 */
@Mod(VortexCore.ID)
object VortexCore {
    const val ID = "vortex"

    // the logger for our mod
    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        EVENT_BUS.register(VxEventHandler);
        EVENT_BUS.addListener(this::onRegisterCommands)
        MOD_BUS.addListener(this::onCommonSetup)
    }



    @SubscribeEvent
    fun onCommonSetup(event: FMLCommonSetupEvent) {
        LOGGER.log(Level.INFO, "Hello! Vortex is installed and working. Give it a try with /vx!")
    }

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        VxCommands.register(event.dispatcher)
    }
}
