package space.atmo.vortex;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate; // Import for date
import java.time.format.DateTimeFormatter; // Import for date formatting
import java.util.Map;

/**
 * Handles exporting collected mod usage data to a CSV file when the server stops.
 * This ensures that the usage statistics are persisted even after the server is shut down.
 */
@EventBusSubscriber(modid = Vortex.MOD_ID) // Subscribes this class to the NeoForge event bus
public class DataExporter {

    // The base file name, the date will be prepended to it.
    private static final String BASE_FILE_NAME = "vortex_mod_usage_data";
    private static final String FILE_EXTENSION = ".csv";
    private static final String CONFIG_SUB_DIR = "vortex"; // Subdirectory within the server's config folder

    /**
     * Saves all collected mod usage data to a CSV file when the server stops.
     * The file is saved in the 'config/vortex/' directory relative to the server's root.
     * This method is subscribed to the ServerStoppedEvent, which is a NeoForge event.
     *
     * @param event The ServerStoppedEvent fired by NeoForge when the server is stopping.
     */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppingEvent event) {
        // Resolve the path to the specific config directory for the Vortex mod.
        // This will be <server_root>/config/vortex/
        Path configDir = event.getServer().getServerDirectory().resolve("config").resolve(CONFIG_SUB_DIR);

        // Create the directory if it doesn't exist to prevent file writing errors.
        if (!java.nio.file.Files.exists(configDir)) {
            try {
                java.nio.file.Files.createDirectories(configDir);
                System.out.println("Vortex: Created config directory: " + configDir.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Vortex: Failed to create config directory for mod usage data: " + e.getMessage());
                e.printStackTrace();
                return; // Exit if directory creation fails, as we can't save the file.
            }
        }

        // Generate the current date in mm-dd-yyyy format
        String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // Construct the full file name with the date
        String fileNameWithDate = BASE_FILE_NAME + "_" + dateString + FILE_EXTENSION;

        // Define the full path for the output CSV file: <server_root>/config/vortex/vortex_mod_usage_data_mm-dd-yyyy.csv
        Path outputFile = configDir.resolve(fileNameWithDate);

        // Write data to the CSV file using a FileWriter.
        // The try-with-resources statement ensures the writer is closed automatically.
        try (FileWriter writer = new FileWriter(outputFile.toFile())) {
            // Write the CSV Header row.
            writer.append("Category,ModID,Count\n");

            // Write data from each usage map, categorizing each entry.
            // Ensure VortexTracker is correctly imported and its static maps are accessible.
            writeMapToCSV(writer, VortexTracker.blockRightClickCounts, "BlockRightClick");
            writeMapToCSV(writer, VortexTracker.itemRightClickCounts, "ItemRightClick");
            writeMapToCSV(writer, VortexTracker.recipeCraftCounts, "CraftingOutput");
            writeMapToCSV(writer, VortexTracker.entityDamageCounts, "EntityDamage");

            System.out.println("Vortex: Mod usage data successfully saved to: " + outputFile.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Vortex: Error saving mod usage data to CSV: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error information.
        }
    }

    /**
     * Helper method to write the contents of a single map (representing a usage category)
     * to the CSV file. Each entry is written as a new row.
     *
     * @param writer   The FileWriter instance to write to.
     * @param map      The map containing mod usage data for a specific category.
     * @param category The name of the category (e.g., "BlockRightClick") to be included in the CSV.
     * @throws IOException If an I/O error occurs during writing.
     */
    private static void writeMapToCSV(FileWriter writer, Map<String, Integer> map, String category) throws IOException {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            writer.append(category) // Append the category name
                    .append(",")     // Append a comma as a delimiter
                    .append(entry.getKey()) // Append the mod ID
                    .append(",")     // Append a comma
                    .append(String.valueOf(entry.getValue())) // Append the usage count
                    .append("\n");  // Append a newline to move to the next row
        }
    }
}
