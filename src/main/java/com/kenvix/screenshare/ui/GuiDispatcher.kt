package com.kenvix.screenshare.ui

import java.awt.Dimension
import java.awt.image.BufferedImage

object GuiDispatcher {
    private val clientUi: ClientUI? = ClientUI.getInstance()

    var title
        get() = clientUi?.frame?.title ?: ""
        set(value) {
            clientUi?.setTitle(value)
        }

    fun show(width: Int, height: Int) = clientUi?.show(width, height)

    fun update(image: BufferedImage) {
        if (clientUi?.frame?.isShowing == true)
            clientUi.update(image)
    }

    fun setSize(width: Int, height: Int) {
        if (clientUi?.frame?.isShowing == true)
            clientUi.setSize(width, height)
    }
}