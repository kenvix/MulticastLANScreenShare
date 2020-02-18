//--------------------------------------------------
// Class FragmentImageProcessor
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.screen

import com.kenvix.screenshare.network.UDPNetwork
import com.kenvix.screenshare.ui.ClientUI
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
    val packetSize: Int = 1000
) : ScreenCapturer.Callback {

    private val intNum = packetSize / 4

    @UseExperimental(ExperimentalUnsignedTypes::class)
    override suspend fun onFragmentCaptured(image: RenderedImage, x: Int, y: Int) = withContext(Dispatchers.Default) {
        val colorInts: IntArray = (image.data.dataBuffer as DataBufferInt).data

        sendToLoopback(image)
        sendToNetwork(compressImage(image, 0.5f))
    }

    private suspend fun sendToLoopback(image: RenderedImage) = withContext(Dispatchers.Default) {
        launch {
            ClientUI.getInstance().update(image as BufferedImage)
        }
    }

    private suspend fun sendToNetwork(colorBytes: ByteArray) = withContext(Dispatchers.Default) {
        var i = 0
        var order = 0
        val sizeChannel = Channel<Int>(8)
        val orderChannel = Channel<Int>(8)

        while (i < colorBytes.size) {
            sizeChannel.send(i)
            orderChannel.send(order)
            order++

            launch(Dispatchers.IO) {
                val offset = sizeChannel.receive()
                val buffer = ByteBuffer.allocate(8 + 4 + packetSize)
                buffer.asLongBuffer().put(System.currentTimeMillis())
                buffer.asIntBuffer().put(orderChannel.receive())

                if (offset + packetSize > colorBytes.size)
                    buffer.put(colorBytes, offset, colorBytes.size - offset)
                else
                    buffer.put(colorBytes, offset, packetSize)

                network.send(buffer.array())
            }

            i += packetSize
        }
    }

    private fun compressImage(image: RenderedImage, compressionQuality: Float = 0.7f): ByteArray {
        // The important part: Create in-memory stream
        ByteArrayOutputStream().use { compressed ->
            val outputStream = ImageIO.createImageOutputStream(compressed)

            // Obtain writer for JPEG format
            // NOTE: The rest of the code is just a cleaned up version of your code
            // Obtain writer for JPEG format
            val jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next()

            // Configure JPEG compression: 70% quality
            val jpgWriteParam = jpgWriter.defaultWriteParam
            jpgWriteParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
            jpgWriteParam.compressionQuality = compressionQuality
            jpgWriter.output = outputStream

            jpgWriter.write(null, IIOImage(image, null, null), jpgWriteParam)

            // Dispose the writer to free resources
            jpgWriter.dispose()

            // Get data for further processing...
            return compressed.toByteArray()
        }
    }

    private fun convertIntArrayToByteArray(colorInts: IntArray): ByteArray {
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(colorInts.size * 4)
        val intBuffer: IntBuffer = byteBuffer.asIntBuffer()
        intBuffer.put(colorInts)

        return byteBuffer.array()
    }
}