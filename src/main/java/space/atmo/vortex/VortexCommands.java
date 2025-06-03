package space.atmo.vortex;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands; 
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registers and handles in-game commands for the Vortex mod.
 * Provides a single summary command for all collected data,
 * and a command to clear collected data.
 */
public class VortexCommands {

    // Define the ModInteractionDetails class here as a static nested class
    public static class ModInteractionDetails {
        public int totalInteractions = 0;
        public Map<String, Integer> interactionBreakdown = new HashMap<>();
    }

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
                        .then(Commands.literal("help") // Help command.
                                .executes(context -> displayHelpMessage(context.getSource()))
                        )
                        .then (Commands.literal("export")
                                .executes(context -> exportDataCommand(context.getSource()))
                        )
                        .then (Commands.literal("unused")
                                .executes(context -> getUnusedModsCommand(context.getSource()))
                        )
                        .then (Commands.literal("dataviewer")
                                .executes(context -> exportToDataViewer(context.getSource()))
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

    private static int displayHelpMessage(CommandSourceStack source){
        source.sendSuccess(() -> Component.literal("--- Vortex Mod Help ---"), false);
        source.sendSuccess(() -> Component.literal("Vortex helps server administrators understand and optimize their modded servers."), false);
        source.sendSuccess(() -> Component.literal("Available Commands:"), false);
        source.sendSuccess(() -> Component.literal("- /vx summary: Same as /vx"), false);
        source.sendSuccess(() -> Component.literal("- /vx clear: Resets all in-memory usage statistics."), false);
        source.sendSuccess(() -> Component.literal("- /vx export: Exports current tracking data to a csv file in your config directory."), false);
        source.sendSuccess(() -> Component.literal("- /vx unused: Lists mods with no tracked interactions."), false);
        source.sendSuccess(() -> Component.literal("- /vx help: Displays this help message."), false);
        return 1;
    }

    private static int exportDataCommand(CommandSourceStack source){
        DataExporter.exportCommand(source.getServer());
        source.sendSuccess(() -> Component.literal("Vortex data exported to content directory."), false);
        return 1;
    }


    private static int getUnusedModsCommand(CommandSourceStack source){

        Set<String> allInstalledModIds = ModList.get().getMods().stream()
                .map(mod -> mod.getModId()) //Working with IModInfo only once so not going to reference the method.
                .collect(Collectors.toSet());

        //Exclude Minecraft (vanilla), NeoForge (modloader) and Vortex ModIDs.
        allInstalledModIds.remove("minecraft");
        allInstalledModIds.remove(Vortex.MOD_ID);
        allInstalledModIds.remove("neoforge");


        Set<String> unusedMods = VortexTracker.getUnusedModIds(allInstalledModIds);

        if(unusedMods.isEmpty()){
            source.sendSuccess(() -> Component.literal("Vortex: No unused mods found."), false);
        } else{
            String modList = String.join(",",unusedMods);
            source.sendSuccess(() -> Component.literal("Vortex: Unused mods: " + modList), false);
        }
        return 1;
    }

    private static int exportToDataViewer(CommandSourceStack source){
        // Get the data
        Map<String, Map<String, Integer>> dataByCategory = new HashMap<>();
        dataByCategory.put("Block Right Clicks", VortexTracker.blockRightClickCounts);
        dataByCategory.put("Item Right Clicks", VortexTracker.itemRightClickCounts);
        dataByCategory.put("Recipe Crafts", VortexTracker.recipeCraftCounts);
        dataByCategory.put("Entity Damage", VortexTracker.entityDamageCounts);
        dataByCategory.put("Chunks Generated", VortexTracker.chunkGenerationCounts);
        dataByCategory.put("Command Interactions", VortexTracker.commandUsageCounts);

        // Creating JSON Structure
        Map<String, ModInteractionDetails> dataByMod = new HashMap<>();

        for (Map.Entry<String, Map<String, Integer>> categoryEntry : dataByCategory.entrySet()) {
            String categoryName = categoryEntry.getKey();
            Map<String, Integer> categoryData = categoryEntry.getValue();

            if (categoryData == null || categoryData.isEmpty()) {
                continue;
            }

            for (Map.Entry<String, Integer> modEntry : categoryData.entrySet()) {
                String modId = modEntry.getKey();
                int countInThisCategory = modEntry.getValue();

                ModInteractionDetails modDetails = dataByMod.computeIfAbsent(modId, k -> new ModInteractionDetails());

                modDetails.totalInteractions += countInThisCategory;

                modDetails.interactionBreakdown.put(categoryName, countInThisCategory);
            }
        }
            String jsonPayload = new Gson().toJson(dataByMod);

            // 5. Perform the Web Request (Asynchronously)
            // Ensure you replace YOUR_VERCEL_PROJECT_URL.vercel.app with your actual Vercel project URL
            String vercelApiUrl = "https://vortex-dataview.vercel.app/api/submit";
            // It's a good idea to make this URL configurable via VortexConfig later

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(vercelApiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            source.sendSuccess(() -> Component.literal("Vortex: Uploading data..."), false);

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        // Handle different HTTP status codes from Vercel
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            return response.body();
                        } else {
                            throw new RuntimeException("Failed to upload report. Server responded with " + response.statusCode() + ": " + response.body());
                        }
                    })
                    .thenAccept(responseBody -> {
                        try {
                            // Assuming the response from Vercel API is JSON like: {"id":"xyz123"}
                            Map<String, String> responseMap = new Gson().fromJson(responseBody, Map.class);
                            String reportId = responseMap.get("id");

                            if (reportId == null || reportId.isEmpty()) {
                                throw new RuntimeException("Vercel API did not return a report ID.");
                            }

                            // Construct the final URL for the user to view the report
                            String finalUrl = "https://vortex-dataview.vercel.app/?id=" + reportId;

                            Component linkComponent = Component.literal(finalUrl)
                                            .setStyle(Style.EMPTY
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl))
                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to view report")))
                                                    .withColor(ChatFormatting.GREEN)
                                                    .withUnderlined(true)
                                            );


                            source.sendSuccess(() -> Component.literal("Vortex Report Link: ").append(linkComponent), true); // 'true' to broadcast to OPs
                        } catch (Exception e) { // Catch parsing errors or missing ID
                            source.sendFailure(Component.literal("Vortex: Error processing response from Vercel: " + e.getMessage()));
                            System.err.println("Vortex: Error parsing Vercel response: " + responseBody + " | Exception: " + e);
                        }
                    })
                    .exceptionally(e -> {
                        source.sendFailure(Component.literal("Vortex: Failed to generate web report. Check server console. " + e.getMessage()));
                        System.err.println("Vortex: Failed to send data to Vercel: " + e);
                        return null;
                    });

            return 1; // Command executed

        }




    }
