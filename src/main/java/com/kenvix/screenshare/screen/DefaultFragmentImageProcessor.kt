//--------------------------------------------------
// Class FragmentImageProcessor
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.screen

import com.kenvix.screenshare.network.UDPNetwork
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class DefaultFragmentImageProcessor(
    network: UDPNetwork
) : ScreenCapturer.Callback {

    override suspend fun onFragmentCaptured(image: BufferedImage, x: Int, y: Int) {

    }
}