package com.kenvix.screenshare.network

import com.kenvix.screenshare.network.UDPNetwork
import com.kenvix.screenshare.ui.GuiDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.libjpegturbo.turbojpeg.TJDecompressor
import sun.awt.image.codec.JPEGImageDecoderImpl
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.awt.image.RenderedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
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
): Closeable {
    @Volatile private var receiveBuffer: ByteBuffer? = null
    @Volatile private var bufferTime: Long = 0
    @Volatile private var bufferReceivedNum: Int = 0
    @Volatile private var bufferReceiveTotalNum: Int = 0

    var onImageBytesReceived: ((data: ByteArray, beginTime: Long, finishTime: Long) -> Unit)? = null
    private lateinit var receiveMonitorThread: Thread

    suspend fun sendToLoopback(image: RenderedImage) = withContext(Dispatchers.Default) {
        GuiDispatcher.update(image as BufferedImage)
    }

    suspend fun sendToNetwork(colorBytes: ByteArray) = withContext(Dispatchers.Default) {
        var i = 0
        val sizeChannel = Channel<Int>(8)
        val numToSend = ceil(colorBytes.size.toDouble() / packetSize.toDouble()).toInt()

        while (i < colorBytes.size) {
            sizeChannel.send(i)

            launch(Dispatchers.IO) {
                val offset = sizeChannel.receive()
                val buffer = ByteBuffer.allocate(8 + 4 * 4 + packetSize)
                val size = if (offset + packetSize > colorBytes.size) colorBytes.size - offset else packetSize

                buffer.asLongBuffer().put(System.currentTimeMillis())
                val intBuffer = buffer.asIntBuffer()
                intBuffer.put(2, offset)
                intBuffer.put(3, size)
                intBuffer.put(4, colorBytes.size)
                intBuffer.put(5, numToSend)

                buffer.position(8 + 4 * 4)
                if (offset + packetSize > colorBytes.size)
                    buffer.put(colorBytes, offset, colorBytes.size - offset)
                else
                    buffer.put(colorBytes, offset, packetSize)

                network.send(buffer.array())
            }

            i += packetSize
        }
    }

    fun stopImageRead() {
        receiveMonitorThread.interrupt()
        network.onReceive = null
    }

    fun beginImageRead(workScope: CoroutineScope, receiveTimeoutMills: Long, toleratePacketLossRate: Float = 0.9f) {
        receiveMonitorThread = Thread({
            val sleepTime = receiveTimeoutMills / 3
            while (!Thread.interrupted()) {
                if (System.currentTimeMillis() - bufferTime > receiveTimeoutMills && (bufferReceiveTotalNum.toFloat() / bufferReceivedNum) >= toleratePacketLossRate) {
                    onImageBytesReceived?.invoke(receiveBuffer!!.array(), bufferTime, System.currentTimeMillis())
                }

                Thread.sleep(sleepTime)
            }
        }, "Image Receive Timeout monitor")

        network.onReceive = { packet ->
            workScope.launch {
                try {
                    val input = ByteBuffer.wrap(packet.data).asReadOnlyBuffer()
                    val time = input.getLong(0)
                    val offset = input.getInt(8)
                    val dataSize = input.getInt(8 + 4)
                    val totalSize = input.getInt(8 + 4 + 4)
                    bufferReceiveTotalNum = input.getInt(8 + 4 + 4 + 4)
                    val data = ByteArray(dataSize)

                    input.position(8 + 4 + 4 + 4 + 4)
                    input.get(data, 0, dataSize)

                    if (receiveBuffer == null || (receiveBuffer!!.capacity() != totalSize && bufferTime < time)) {
                        receiveBuffer = ByteBuffer.allocate(totalSize)
                        bufferReceivedNum = 0
                        bufferTime = time
                    }

                    receiveBuffer!!.position(offset)
                    receiveBuffer!!.put(data, 0, dataSize)
                    bufferReceivedNum++

                    if (bufferReceiveTotalNum == bufferReceivedNum)
                        onImageBytesReceived?.invoke(receiveBuffer!!.array(), bufferTime, time)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        receiveMonitorThread.start()
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

    fun decompressImage(array: ByteArray, w: Int, h: Int): BufferedImage {
        return TJDecompressor(array).decompress(w, h, BufferedImage.TYPE_INT_RGB, 0)

//        ByteArrayInputStream(array).use {
//            return JPEGImageDecoderImpl(it).decodeAsBufferedImage()
//        }
    }

    fun convertIntArrayToByteArray(colorInts: IntArray): ByteArray {
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(colorInts.size * 4)
        val intBuffer: IntBuffer = byteBuffer.asIntBuffer()
        intBuffer.put(colorInts)

        return byteBuffer.array()
    }

    override fun close() {
        if (this::receiveMonitorThread.isInitialized) {
            if (receiveMonitorThread.isAlive)
                stopImageRead()
        }
    }
}