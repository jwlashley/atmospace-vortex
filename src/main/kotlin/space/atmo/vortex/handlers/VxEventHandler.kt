package space.atmo.vortex.handlers

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.level.BlockEvent
import space.atmo.vortex.data.VxMapper

object VxEventHandler {


    @SubscribeEvent
    fun onBlockRightClick(blockRightClick: PlayerInteractEvent.RightClickBlock){
        if (blockRightClick.level.isClientSide) return;

        if (blockRightClick.hand != InteractionHand.MAIN_HAND) return

        val blockState: BlockState = blockRightClick.level.getBlockState(blockRightClick.pos)
        val blockRegistryName: ResourceLocation? = blockRightClick.level.registryAccess().registryOrThrow(Registries.BLOCK).getKey(blockState.block)

        if (blockRegistryName != null) {
            val modID : String = blockRegistryName.namespace

            if ("minecraft" != modID) {
                VxMapper.blockRightClickCount.incrementCount(modID)
            }
        }
    }

    @SubscribeEvent
    fun onItemRightClick(itemRightClick: PlayerInteractEvent.RightClickItem) {
        if (itemRightClick.level.isClientSide) return;
        if (itemRightClick.hand != InteractionHand.MAIN_HAND) return

        val itemStack: ItemStack = itemRightClick.itemStack;
        val itemRegistryName: ResourceLocation? = itemRightClick.level.registryAccess().registryOrThrow(Registries.ITEM).getKey(itemStack.item)

        if (itemRegistryName != null) {
            val modID: String = itemRegistryName.namespace

            if ("minecraft" != modID) {
                VxMapper.itemRightClickCount.incrementCount(modID)
            }
        }
    }

    @SubscribeEvent
    fun onBlockPlace(blockPlace: BlockEvent.EntityPlaceEvent){
        if (blockPlace.level.isClientSide) return;
        if (blockPlace.entity !is Player) return;

        val placedBlock = blockPlace.placedBlock
        val blockRegistryName = blockPlace.level.registryAccess().registryOrThrow(Registries.BLOCK).getKey(placedBlock.block)

        if (blockRegistryName != null){
            val modID = blockRegistryName.namespace

            if ("minecraft" != modID){
                VxMapper.blockPlaceCount.incrementCount(modID)
            }
        }
    }


}