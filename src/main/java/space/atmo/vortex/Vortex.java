package space.atmo.vortex;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.fml.common.Mod;

@Mod(Vortex.MOD_ID)
public class Vortex {
    public static final String MOD_ID = "vortex";

    public Vortex() {
        // Register this class to the NeoForge event bus for command registration.
        NeoForge.EVENT_BUS.register(this);
        // Register the ModUsageEventHandler to listen for various in-game events.
        NeoForge.EVENT_BUS.register(VortexEventHandler.class);
        // Register the DataExporter to handle saving data when the server stops.
        //NeoForge.EVENT_BUS.register(DataExporter.class);
    }

    /**
     * Registers the mod commands when the server starts.
     * This method is subscribed to the RegisterCommandsEvent, which is fired by NeoForge.
     *
     * @param event The RegisterCommandsEvent fired by NeoForge.
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        // Delegate command registration to the VortexCommands class.
        VortexCommands.register(event.getDispatcher());
    }
}
