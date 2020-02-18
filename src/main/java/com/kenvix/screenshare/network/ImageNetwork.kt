package com.kenvix.screenshare.network

import com.kenvix.screenshare.network.UDPNetwork
import com.kenvix.screenshare.ui.GuiDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.awt.image.RenderedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.*
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlin.math.ceil

class ImageNetwork(
    val network: UDPNetwork,
    val packetSize: Int = 1000
) {
    suspend fun sendToLoopback(image: RenderedImage) = withContext(Dispatchers.Default) {
        launch {
            GuiDispatcher.update(image as BufferedImage)
        }
    }

    suspend fun sendToNetwork(colorBytes: ByteArray) = withContext(Dispatchers.Default) {
        var i = 0
        val sizeChannel = Channel<Int>(8)
        val numToSend = ceil(colorBytes.size.toDouble() / packetSize.toDouble()).toInt()

        while (i < colorBytes.size) {
            sizeChannel.send(i)

            launch(Dispatchers.IO) {
                val offset = sizeChannel.receive()
                val buffer = ByteBuffer.allocate(8 + 4 + 4 + 4 + 4 + packetSize)
                val size = if (offset + packetSize > colorBytes.size) colorBytes.size - offset else packetSize

                buffer.asLongBuffer().put(System.currentTimeMillis())
                val intBuffer = buffer.asIntBuffer()
                intBuffer.put(offset)
                intBuffer.put(size)
                intBuffer.put(colorBytes.size)
                intBuffer.put(numToSend)

                if (offset + packetSize > colorBytes.size)
                    buffer.put(colorBytes, offset, colorBytes.size - offset)
                else
                    buffer.put(colorBytes, offset, packetSize)

                network.send(buffer.array())
            }

            i += packetSize
        }
    }

    private var receiveBuffer: ByteBuffer? = null
    private var bufferTime: Long = 0
    private var bufferReceivedNum: Int = 0
    var onImageReceived: ((data: ByteArray, beginTime: Long, finishTime: Long) -> Unit)? = null

    fun beginImageRead() {
        network.onReceive = { packet ->
            val input = ByteBuffer.wrap(packet.data).asReadOnlyBuffer()
            val time = input.getLong(0)
            val offset = input.getInt(8)
            val dataSize = input.getInt(8 + 4)
            val totalSize = input.getInt(8 + 4 + 4)
            val totalPacketNum = input.getInt(8 + 4 + 4 + 4)
            val data = ByteArray(dataSize)
            input.get(data, 8 + 4 + 4 + 4 + 4, dataSize)

            if (receiveBuffer == null || (receiveBuffer!!.capacity() != totalSize && bufferTime < time)) {
                receiveBuffer = ByteBuffer.allocate(totalSize)
                bufferReceivedNum = 0
            }

            receiveBuffer!!.put(data, offset, dataSize)
            bufferReceivedNum++

            if (totalPacketNum == bufferReceivedNum)
                onImageReceived?.invoke(receiveBuffer!!.array(), bufferTime, time)
        }
    }

    fun compressImage(image: RenderedImage, compressionQuality: Float = 0.7f): ByteArray {
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

    fun convertIntArrayToByteArray(colorInts: IntArray): ByteArray {
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(colorInts.size * 4)
        val intBuffer: IntBuffer = byteBuffer.asIntBuffer()
        intBuffer.put(colorInts)

        return byteBuffer.array()
    }
}