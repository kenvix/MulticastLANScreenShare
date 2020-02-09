//--------------------------------------------------
// Class FragmentImageProcessor
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.screen

import com.kenvix.screenshare.network.UDPNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.awt.image.DataBufferInt
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.zip.GZIPOutputStream


class DefaultFragmentImageProcessor(
    val network: UDPNetwork,
    val packetSize: Int = 600,
    val useGzip: Boolean = true
) : ScreenCapturer.Callback {

    private val intNum = packetSize / 4

    @Suppress("DeferredResultUnused")
    @UseExperimental(ExperimentalUnsignedTypes::class)
    override suspend fun onFragmentCaptured(image: RenderedImage, x: Int, y: Int) = withContext(Dispatchers.Default) {
        val colorInts: IntArray = (image.data.dataBuffer as DataBufferInt).data

        val byteBuffer: ByteBuffer = ByteBuffer.allocate(colorInts.size * 4)
        val intBuffer: IntBuffer = byteBuffer.asIntBuffer()
        intBuffer.put(colorInts)

        val colorBytes: ByteArray = byteBuffer.array()
        var i = 0

        while (i < colorBytes.size) {
            withContext(Dispatchers.IO) {
                if (useGzip) {
                    val result = ByteArrayOutputStream().use { colorResultStream ->
                        val gzipStream = GZIPOutputStream(colorResultStream)

                        if (i + packetSize > colorBytes.size)
                            gzipStream.write(colorBytes, i, colorBytes.size - i)
                        else
                            gzipStream.write(colorBytes, i, packetSize)

                        gzipStream.close()
                        colorResultStream.toByteArray()
                    }

                    network.send(result)
                } else {
                    if (i + packetSize > colorBytes.size)
                        network.send(colorBytes, i, colorBytes.size - i)
                    else
                        network.send(colorBytes, i, packetSize)
                }
            }

            i += packetSize
        }
    }
}