package space.atmo.vortex;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands; 
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registers and handles in-game commands for the Vortex mod.
 * Provides a single summary command for all collected data,
 * and a command to clear collected data.
 */
public class VortexCommands {

    /**
     * Registers all commands for the Vortex mod with the Minecraft command dispatcher.
     * This method defines the command structure and the actions to be performed when commands are executed.
     * Uses Brigader, which is standard for command registration in NeoForge.
     *
     * @param dispatcher The CommandDispatcher instance.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> modUsageCommand = dispatcher.register(
                Commands.literal(Vortex.MOD_ID) // Base command: /vortex
                        .requires(source -> source.hasPermission(2)) // Requires Op level 2 or higher to run these commands
                        .executes(context -> displayAllSummaries(context.getSource())) // Default execution for /vortex
                        .then(Commands.literal("summary") // Explicit /vortex summary command
                                .executes(context -> displayAllSummaries(context.getSource()))
                        )
                        .then(Commands.literal("clear") // Command to clear all collected data in memory
                                .executes(context -> {
                                    VortexTracker.clearAllData(); // Call the clear method from ModUsageTracker
                                    context.getSource().sendSuccess(() -> Component.literal("Vortex: All collected usage data has been cleared."), true);
                                    return 1;
                                })
                        )
        );
        // Register a shorter alias for convenience: /vx
        dispatcher.register(Commands.literal("vx")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .redirect(modUsageCommand));
    }

    /**
     * Displays the top 10 most used mods for a given data category.
     * The results are sorted in descending order of usage count.
     *
     * @param source The command source (e.g., player, console).
     * @param data   The map containing mod usage data (e.g., blockRightClickCounts).
     * @param type   A descriptive string for the data category (e.g., "Block Right-Click").
     * @return 1 if successful, 0 if data is empty.
     */
    private static int displayMostUsed(CommandSourceStack source, Map<String, Integer> data, String type) {
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No " + type + " data collected yet."), false);
            return 0;
        }

        String topMods = data.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // Sort in descending order of count
                .limit(10) // Display top 10 entries
                .map(entry -> entry.getKey() + ": " + entry.getValue()) // Format each entry as "ModID: Count"
                .collect(Collectors.joining("\n")); // Join entries with newlines for multi-line output

        source.sendSuccess(() -> Component.literal("--- Vortex: Most Used " + type + " ---\n" + topMods), false);
        return 1;
    }

    /**
     * Displays the top 10 least used mods (that have at least 1 usage) for a given data category.
     * The results are filtered to include only mods with actual usage and sorted in ascending order.
     *
     * @param source The command source.
     * @param data   The map containing mod usage data.
     * @param type   A descriptive string for the data category.
     * @return 1 if successful, 0 if data is empty or no used mods found after filtering.
     */
    private static int displayLeastUsed(CommandSourceStack source, Map<String, Integer> data, String type) {
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No " + type + " data collected yet."), false);
            return 0;
        }

        // Filter to only include mods that have been used at least once, then sort ascending.
        String leastMods = data.entrySet().stream()
                .filter(entry -> entry.getValue() > 0) // Only show mods with actual usage (count > 0)
                .sorted(Map.Entry.comparingByValue()) // Sort in ascending order of count
                .limit(10) // Display bottom 10 entries (of those that were actually used)
                .map(entry -> entry.getKey() + ": " + entry.getValue()) // Format each entry
                .collect(Collectors.joining("\n")); // Join entries with newlines

        if (leastMods.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Vortex: All " + type + " mods have significant usage, or no usage at all (after filtering for > 0 usage)."), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("--- Vortex: Least Used " + type + " (with some usage) ---\n" + leastMods), false);
        return 1;
    }

    /**
     * Displays a comprehensive summary of mod usage across all tracking categories,
     * including both most used and least used for each.
     *
     * @param source The command source.
     * @return 1 if successful.
     */
    private static int displayAllSummaries(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("--- Vortex: Comprehensive Mod Usage Summary ---"), false);

        source.sendSuccess(() -> Component.literal("\n"), false); // Add a newline for separation
        displayMostUsed(source, VortexTracker.blockRightClickCounts, "Block Right-Click");
        displayLeastUsed(source, VortexTracker.blockRightClickCounts, "Block Right-Click");

        source.sendSuccess(() -> Component.literal("\n"), false);
        displayMostUsed(source, VortexTracker.itemRightClickCounts, "Item Right-Click");
        displayLeastUsed(source, VortexTracker.itemRightClickCounts, "Item Right-Click");

        source.sendSuccess(() -> Component.literal("\n"), false);
        displayMostUsed(source, VortexTracker.recipeCraftCounts, "Crafting Output");
        displayLeastUsed(source, VortexTracker.recipeCraftCounts, "Crafting Output");

        source.sendSuccess(() -> Component.literal("\n"), false);
        displayMostUsed(source, VortexTracker.entityDamageCounts, "Entity Damage");
        displayLeastUsed(source, VortexTracker.entityDamageCounts, "Entity Damage");

        return 1;
    }
}
