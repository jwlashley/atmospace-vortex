// src/main/java/com/yourname/vortex/VortexEventHandler.java
package space.atmo.vortex;

import com.mojang.brigadier.ParseResults;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.minecraft.commands.CommandSourceStack;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles various in-game events to track mod usage.
 * All methods are static and subscribed to the main NeoForge event bus.
 * This class centralizes all event listening logic for the Vortex mod.
 */
@EventBusSubscriber(modid = Vortex.MOD_ID)
public class VortexEventHandler { // RENAMED: ModUsageEventHandler -> VortexEventHandler

    /**
     * Tracks when a player right-clicks on a block.
     * Filters out vanilla Minecraft blocks to focus on modded content.
     *
     * @param event The PlayerInteractEvent.RightClickBlock event.
     */
    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        // Ensure the event is happening on the server side to avoid double counting and client-only logic.
        if (event.getLevel().isClientSide) return;
        // Ensure the player is using their main hand to avoid duplicate events for off-hand.
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockState blockState = event.getLevel().getBlockState(event.getPos());
        ResourceLocation blockRegistryName = event.getLevel().registryAccess().registryOrThrow(Registries.BLOCK).getKey(blockState.getBlock());

        if (blockRegistryName != null) {
            String modId = blockRegistryName.getNamespace();
            // Filter out vanilla Minecraft blocks (modId "minecraft")
            if (!"minecraft".equals(modId)) {
                VortexTracker.incrementCount(VortexTracker.blockRightClickCounts, modId); // RENAMED: ModUsageTracker -> VortexTracker
            }
        }
    }

    /**
     * Tracks when a player right-clicks with an item.
     * Filters out vanilla Minecraft items.
     *
     * @param event The PlayerInteractEvent.RightClickItem event.
     */
    @SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        // Ensure the event is happening on the server side.
        if (event.getLevel().isClientSide) return;
        // Ensure the player is using their main hand.
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        ItemStack itemStack = event.getItemStack();
        ResourceLocation itemRegistryName = event.getLevel().registryAccess().registryOrThrow(Registries.ITEM).getKey(itemStack.getItem());

        if (itemRegistryName != null) {
            String modId = itemRegistryName.getNamespace();
            // Filter out vanilla Minecraft items (modId "minecraft")
            if (!"minecraft".equals(modId)) {
                VortexTracker.incrementCount(VortexTracker.itemRightClickCounts, modId); // RENAMED: ModUsageTracker -> VortexTracker
            }
        }
    }

    /**
     * Tracks when a player crafts an item.
     * The mod ID is determined by the output item of the crafted recipe.
     * Filters out vanilla Minecraft crafted items.
     *
     * @param event The PlayerEvent.ItemCraftedEvent event.
     */
    @SubscribeEvent
    public static void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        // Ensure the event is happening on the server side.
        if (event.getEntity().level().isClientSide) return;

        ItemStack craftedItem = event.getCrafting();
        ResourceLocation itemRegistryName = event.getEntity().level().registryAccess().registryOrThrow(Registries.ITEM).getKey(craftedItem.getItem());

        if (itemRegistryName != null) {
            String modId = itemRegistryName.getNamespace();
            // Filter out vanilla Minecraft crafted items (modId "minecraft")
            if (!"minecraft".equals(modId)) {
                VortexTracker.incrementCount(VortexTracker.recipeCraftCounts, modId); // RENAMED: ModUsageTracker -> VortexTracker
            }
        }
    }

    /**
     * Tracks when a player damages a living entity.
     * Filters out vanilla Minecraft entities.
     *
     * @param event The LivingDamageEvent event.
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // Ensure the event is happening on the server side.
        if (event.getEntity().level().isClientSide) return;
        // The getSource() method is on the specific subclasses (Pre/Post), not the base LivingDamageEvent.
        // We need to cast the event to access getSource().
        DamageSource damageSource = event.getSource();
        // Only track damage dealt by a server player.
        if (!(damageSource.getEntity() instanceof ServerPlayer)) return;


        LivingEntity damagedEntity = event.getEntity();
        EntityType<?> entityType = damagedEntity.getType();
        ResourceLocation entityRegistryName = event.getEntity().level().registryAccess().registryOrThrow(Registries.ENTITY_TYPE).getKey(entityType);

        if (entityRegistryName != null) {
            String modId = entityRegistryName.getNamespace();
            // Filter out vanilla Minecraft entities (modId "minecraft")
            if (!"minecraft".equals(modId)) {
                VortexTracker.incrementCount(VortexTracker.entityDamageCounts, modId); // RENAMED: ModUsageTracker -> VortexTracker
            }
        }
    }

    /**
     * Event handling for tracking chunk generation by mods with world generation.
     */
    private static final Set<ChunkPos> processedChunks = Collections.synchronizedSet(new HashSet<>());

    @SubscribeEvent
    public static void onChunkGeneration(ChunkEvent.Load event){
        if (event.getLevel().isClientSide()){
            return;
        }

        if (event.getChunk() instanceof LevelChunk chunk){

            ChunkPos chunkPos = chunk.getPos(); // Get the chunk's position

            if (processedChunks.contains(chunkPos)){ // Check if the chunk has been previously processed to avoid duplicate data.
                return;
            }

            Holder<Biome> biomeHolder = chunk.getNoiseBiome(chunk.getPos().getMinBlockX() >> 2, chunk.getMinBuildHeight() >> 2, chunk.getPos().getMinBlockZ() >> 2);

            biomeHolder.unwrapKey().ifPresent(biomeKey -> {
                ResourceLocation biomeID = biomeKey.location();
                String modID = biomeID.getNamespace();

            if (!"minecraft".equals(modID)) {
                VortexTracker.incrementCount(VortexTracker.chunkGenerationCounts, modID);
            }
            });

            processedChunks.add(chunkPos);

        }
    }


    @SubscribeEvent
    public static void onCommandExecution(CommandEvent event) {
        ParseResults<CommandSourceStack> parseResults = event.getParseResults();


        if (parseResults.getContext().getSource().getServer() == null) {
            return;
        }

        // attempt to get the mod id by listening to the command and getting the root ex: /vortex summary will grab vortex

        String modID = parseResults.getReader().getString().split(" ")[0].replace("/", "");

        if (modID != null && !"minecraft".equals(modID)) {
            VortexTracker.incrementCount(VortexTracker.commandUsageCounts, modID);


        }
    }
}
