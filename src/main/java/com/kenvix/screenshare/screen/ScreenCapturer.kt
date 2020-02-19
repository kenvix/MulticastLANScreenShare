//--------------------------------------------------
// Interface ScreenCapturer
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.screen

import java.awt.image.BufferedImage
import java.awt.image.RenderedImage

interface ScreenCapturer {
    var fragmentWidth: Int
    var fragmentHeight: Int
    val screenWidth: Int
    val screenHeight: Int

    var callback: Callback?
    var fps: Int
    var monitor: Int

    fun updateScreenProfile()

    interface Callback {
        suspend fun onFragmentCaptured(image: BufferedImage, x: Int, y: Int)
    }
}