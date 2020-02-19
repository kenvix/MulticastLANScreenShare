package com.kenvix.screenshare.ui

import com.kenvix.screenshare.Main
import java.awt.image.BufferedImage

object GuiDispatcher {
    var clientUi: BaseUI? = Main.clientUI
    var title
        get() = clientUi?.title ?: ""
        set(value) {
            clientUi?.title = value
        }

    fun show(width: Int, height: Int) = clientUi?.show(width, height)

    fun update(image: BufferedImage) {
        if (clientUi?.isShowing == true)
            clientUi?.update(image)
    }

    fun setSize(width: Int, height: Int) {
        if (clientUi?.isShowing == true)
            clientUi?.setSize(width, height)
    }
}