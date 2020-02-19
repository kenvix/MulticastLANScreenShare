//--------------------------------------------------
// Class DefaulReceivedImageProcessor
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.screen

import com.kenvix.screenshare.network.ImageNetwork
import com.kenvix.screenshare.network.UDPNetwork
import com.kenvix.screenshare.ui.GuiDispatcher
import kotlinx.coroutines.*
import java.io.Closeable
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class DefaultReceivedImageProcessor(
    val network: UDPNetwork,
    val packetSize: Int = 1000
): Closeable {
    private val imageNetwork: ImageNetwork = ImageNetwork(network, packetSize)
    private val job: CompletableJob = Job()
    private val workScope: CoroutineScope = CoroutineScope(Dispatchers.Default + job)
    private val dateFormatter = SimpleDateFormat("HH:mm:ss.SSS")

    fun start(receiveTimeoutMills: Long, toleratePacketLossRate: Float = 0.9f) {
        imageNetwork.onImageBytesReceived = { data, beginTime, finishTime ->
            workScope.launch {
                try {
                    val begin = System.currentTimeMillis()
                    val img = imageNetwork.decompressImage(data, GuiDispatcher.width, GuiDispatcher.height)
                    val end = System.currentTimeMillis()
                    GuiDispatcher.update(img)
                    val end2 = System.currentTimeMillis()

                    GuiDispatcher.title = "Client | ${dateFormatter.format(Date(end))} | Loss 0% | Network Ping ${finishTime - beginTime}ms | Decode ${end - begin}ms | Swing Render ${end2 - end}ms | Total latency ${System.currentTimeMillis() - finishTime}ms"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        imageNetwork.beginImageRead(workScope, receiveTimeoutMills, toleratePacketLossRate)
    }

    override fun close() {
        imageNetwork.stopImageRead()
    }
}