//--------------------------------------------------
// Class FragmentImageProcessor
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.screen

import com.kenvix.screenshare.Main
import com.kenvix.screenshare.network.ImageNetwork
import com.kenvix.screenshare.network.UDPNetwork
import com.kenvix.screenshare.ui.GuiDispatcher
import com.kenvix.utils.lang.WeakRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.IntBuffer
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam


class DefaultFragmentImageProcessor(
    val network: UDPNetwork,
    val packetSize: Int = 1000,
    val quality: Int = 50
) : ScreenCapturer.Callback {

    private val imageNetwork = ImageNetwork(network, packetSize)

    @UseExperimental(ExperimentalUnsignedTypes::class)
    override suspend fun onFragmentCaptured(image: WeakRef<BufferedImage>, x: Int, y: Int) = withContext(Dispatchers.Default) {
        if (Main.isLoopbackEnabled)
            launch { imageNetwork.sendToLoopback(image) }

        launch { imageNetwork.sendToNetwork(imageNetwork.compressImageAwt(image, quality / 100f)) }
        Unit
    }
}