//--------------------------------------------------
// Interface BaseUI
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.ui

import java.awt.image.BufferedImage

interface BaseUI {
    var title: String
    val isShowing: Boolean
    val width: Int
    val height: Int

    fun show(width: Int, height: Int)
    fun update(image: BufferedImage, shouldResize: Boolean)
    fun setSize(width: Int, height: Int)
}