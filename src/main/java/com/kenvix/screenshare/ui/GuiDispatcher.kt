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
    val width
        get() = clientUi?.width ?: 0

    val height
        get() = clientUi?.height ?: 0

    fun show(width: Int, height: Int) = clientUi?.show(width, height)

    fun update(image: BufferedImage, shouldResize: Boolean = false) {
        if (clientUi?.isShowing == true)
            clientUi?.update(image, shouldResize)
    }

    fun setSize(width: Int, height: Int) {
        if (clientUi?.isShowing == true)
            clientUi?.setSize(width, height)
    }
}