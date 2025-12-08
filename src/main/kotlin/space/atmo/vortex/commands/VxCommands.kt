package space.atmo.vortex.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import space.atmo.vortex.VortexCore
import space.atmo.vortex.data.VxMapper
import space.atmo.vortex.screens.VxStatScreen

object VxCommands {

   private var DEBUG: Boolean = false

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
            var command = Commands.literal(VortexCore.ID) // Establishes base command of /vortex
                .requires {it.hasPermission(2)} // Requires OP level 2 or higher to execute.
                if (DEBUG) {
                    command = command.executes(::testDisplay)
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
                } else {
                    command = command.executes(::displayGui)
                }
            val baseCommand = dispatcher.register(command)

            val aliasCommand = Commands.literal("vx")
                .requires {it.hasPermission(2)}
                .redirect(baseCommand)
            dispatcher.register(aliasCommand)

        }


    private fun displayGui(context: CommandContext<CommandSourceStack>): Int {
        val minecraft = Minecraft.getInstance()
        if (minecraft != null) {
            val screen = VxStatScreen(Component.literal("Vortex Stats"))
            minecraft.setScreen(screen)
        } else {
            context.source.sendFailure(Component.literal("This command can only be used by a player."))
        }
        return 1
    }

    private fun testDisplay(context: CommandContext<CommandSourceStack>): Int {
        context.source.sendSuccess({ Component.literal("DEBUG Vortex reads you loud and clear!")}, true)
        return 1
    }

    private fun displayBlockRightClicks(context: CommandContext<CommandSourceStack>): Int{
        val blocksClicked = VxMapper.blockRightClickCount.getCounts()
        context.source.sendSuccess({ Component.literal(VxMapper.blockRightClickCount.name)}, true)
        context.source.sendSuccess({ Component.literal(blocksClicked.toString())}, true)
       return 1
    }

    private fun displayItemRightClicks(context: CommandContext<CommandSourceStack>): Int{
        val itemsClicked = VxMapper.itemRightClickCount
        context.source.sendSuccess({ Component.literal(itemsClicked.toString())}, true)
        return 1
    }
    private fun displayBlockPlace(context: CommandContext<CommandSourceStack>): Int{
        val blocksPlaced = VxMapper.blockPlaceCount
        context.source.sendSuccess({ Component.literal(blocksPlaced.toString())}, true)
        return 1
    }



}