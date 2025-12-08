package space.atmo.vortex.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import space.atmo.vortex.VortexCore
import space.atmo.vortex.data.VxMapper

object VxCommands {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        // register the base command and keep its node for redirects
        val baseCommand = dispatcher.register(
            Commands.literal(VortexCore.ID) // /vortex
                .requires { it.hasPermission(2) }
                .executes(::displayUsage)
                .then(
                    Commands.literal("blocks")
                        .executes(::displayBlockRightClicks)
                )
                .then(
                    Commands.literal("items")
                        .executes(::displayItemRightClicks)
                )
                .then(
                    Commands.literal("placed")
                        .executes(::displayBlockPlace)
                )
        )

        // alias /vx -> /vortex
        val aliasCommand = Commands.literal("vx")
            .requires { it.hasPermission(2) }
            .redirect(baseCommand)

        dispatcher.register(aliasCommand)
    }

    private fun displayUsage(context: CommandContext<CommandSourceStack>): Int {
        context.source.sendSuccess({ Component.literal("Vortex command usage:") }, true)
        context.source.sendSuccess({ Component.literal("/vortex blocks - Display block right-click counts") }, true)
        context.source.sendSuccess({ Component.literal("/vortex items - Display item right-click counts") }, true)
        context.source.sendSuccess({ Component.literal("/vortex placed - Display block placement counts") }, true)
        return 1
    }

    private fun displayBlockRightClicks(context: CommandContext<CommandSourceStack>): Int {
        val blocksClicked = VxMapper.blockRightClickCount
        if (blocksClicked.getCounts().isEmpty()) {
            context.source.sendSuccess({ Component.literal("No block right-click data available.") }, true)
            return 1
        }
        context.source.sendSuccess({ Component.literal(VxMapper.blockRightClickCount.name) }, true)
        context.source.sendSuccess({ Component.literal(blocksClicked.getCounts().toString()) }, true)
        return 1
    }

    private fun displayItemRightClicks(context: CommandContext<CommandSourceStack>): Int {
        val itemsClicked = VxMapper.itemRightClickCount
        if (itemsClicked.getCounts().isEmpty()) {
            context.source.sendSuccess({ Component.literal("No item right-click data available.") }, true)
            return 1
        }
        context.source.sendSuccess({ Component.literal(itemsClicked.name) }, true)
        context.source.sendSuccess({ Component.literal(itemsClicked.getCounts().toString()) }, true)
        return 1
    }

    private fun displayBlockPlace(context: CommandContext<CommandSourceStack>): Int {
        val blocksPlaced = VxMapper.blockPlaceCount
        if (blocksPlaced.getCounts().isEmpty()) {
            context.source.sendSuccess({ Component.literal("No block placement data available.") }, true)
            return 1
        }
        context.source.sendSuccess({ Component.literal(blocksPlaced.name) }, true)
        context.source.sendSuccess({ Component.literal(blocksPlaced.getCounts().toString()) }, true)
        return 1
    }
}