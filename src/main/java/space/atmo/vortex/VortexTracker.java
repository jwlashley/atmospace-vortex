package space.atmo.vortex;

import java.util.*;

/**
 * Manages the in-memory storage for mod usage statistics.
 * Each map tracks a specific type of interaction (e.g., block right-clicks, item right-clicks).
 * This class holds the actual data collected during server runtime.
 */
public class VortexTracker {
    // Maps to store counts for different interaction types, keyed by mod ID.
    // These are static so they can be accessed directly by event handlers and command classes.
    public static final Map<String, Integer> blockRightClickCounts = new HashMap<>();
    public static final Map<String, Integer> itemRightClickCounts = new HashMap<>();
    public static final Map<String, Integer> recipeCraftCounts = new HashMap<>();
    public static final Map<String, Integer> entityDamageCounts = new HashMap<>();
    public static final Map<String, Integer> chunkGenerationCounts = new HashMap<>();
    public static final Map<String, Integer> commandUsageCounts = new HashMap<>();

    /**
     * Increments the count for a given mod ID in the specified map.
     * If the mod ID is not yet in the map, it's added with a count of 1.
     * This method is synchronized to ensure thread safety when multiple events occur simultaneously,
     * as events can be fired from different threads.
     *
     * @param map   The HashMap to update (e.g., blockRightClickCounts).
     * @param modId The ID of the mod whose usage is being tracked.
     */
    public static synchronized void incrementCount(Map<String, Integer> map, String modId) {
        map.put(modId, map.getOrDefault(modId, 0) + 1);
    }

    /**
     * Clears all collected usage data from all maps.
     * This is useful for debugging or resetting statistics via an in-game command.
     * This method is synchronized for thread safety.
     */
    public static synchronized void clearAllData() {
        blockRightClickCounts.clear();
        itemRightClickCounts.clear();
        recipeCraftCounts.clear();
        entityDamageCounts.clear();
        chunkGenerationCounts.clear();
        commandUsageCounts.clear();
        System.out.println("Vortex: All collected usage data has been cleared.");
    }

    // Gathering all mods that have recorded interactions.
    public static synchronized Set<String> getInteractedModIds(){
        Set<String> interactedMods = new HashSet<>();
        interactedMods.addAll(blockRightClickCounts.keySet());
        interactedMods.addAll(itemRightClickCounts.keySet());
        interactedMods.addAll(recipeCraftCounts.keySet());
        interactedMods.addAll(entityDamageCounts.keySet());
        interactedMods.addAll(chunkGenerationCounts.keySet());
        interactedMods.addAll(commandUsageCounts.keySet());
        return interactedMods;
    }


    /**
     * Compare a list of installed modIDs against our incremented modIDs to find mods that have not been used by players*.
     * *In a way that vortex tracks. - Some mods might not use interactions that we track and won't show up as interacted with.
     */
    public static synchronized Set<String> getUnusedModIds(Set<String> allinstalledModIds){
        if(allinstalledModIds == null){
            System.err.println("Vortex:allInstalledModIds cannot be null.");
            return Collections.emptySet();
        }
        Set<String> interactedModIds = getInteractedModIds();
        Set<String> unusedModIds = new HashSet<>(allinstalledModIds);
        unusedModIds.removeAll(interactedModIds);
        return unusedModIds;
    }
}

