//--------------------------------------------------
// Class DefaulReceivedImageProcessor
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.screen

import com.kenvix.screenshare.network.ImageNetwork
import com.kenvix.screenshare.network.UDPNetwork
import com.kenvix.screenshare.ui.GuiDispatcher
import java.io.Closeable

class DefaultReceivedImageProcessor(
    val network: UDPNetwork,
    val packetSize: Int = 1000
): Closeable {
    private val imageNetwork: ImageNetwork = ImageNetwork(network, packetSize)

    fun start(receiveTimeoutMills: Long, toleratePacketLossRate: Float = 0.9f) {
        imageNetwork.onImageBytesReceived = { data, beginTime, finishTime ->
            GuiDispatcher.update(imageNetwork.decompressImage(data))
        }

        imageNetwork.beginImageRead(receiveTimeoutMills, toleratePacketLossRate)
    }

    override fun close() {
        imageNetwork.stopImageRead()
    }
}