package space.atmo.vortex.screens

import net.minecraft.network.chat.Component
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import space.atmo.vortex.data.VxMapper

class VxStatScreen(title: Component) : Screen(title) {
    override fun init() {
        super.init()
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(graphics, mouseX, mouseY, delta)

        val text = "No data available yet."

        if (VxMapper.blockRightClickCount.getCounts().isNotEmpty()) {
            val text = VxMapper.blockRightClickCount.name
            val blocksClicked = VxMapper.blockRightClickCount.getCounts().toString()
        }

        graphics.drawCenteredString(
            this.font,
            this.title,
            this.width / 2,
            7,
            0xFFFFFF
        )

        graphics.drawCenteredString(
            this.font,
            text,
            this.width / 2,
            14,
            0xFFFFFF
        )

        if (VxMapper.blockRightClickCount.getCounts().isNotEmpty()) {
            val text = VxMapper.blockRightClickCount.name
            val blocksClicked = VxMapper.blockRightClickCount.getCounts().toString()

            graphics.drawCenteredString(
                this.font,
                blocksClicked,
                this.width / 2,
                21,
                0xFFFFFF
            )
        }

    }

}