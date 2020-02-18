package com.kenvix.screenshare.network

import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket

class MulticastServer(
    multicastAddress: InetAddress = InetAddress.getByName("230.114.5.14"),
    multicastPort: Int = 1919
): MulticastNetwork(multicastAddress, multicastPort) {
    private var serverJob: CompletableJob? = null
    private var serverWorkScope: CoroutineScope? = null

    override var onReceive: ((packet: DatagramPacket) -> Unit)? = null

    fun listen() {
        if (onReceive == null)
            throw IllegalArgumentException("To start a server, onReceive should not be null")

        if (serverJob?.isActive == true)
            throw IllegalStateException("Already listening")

        serverJob = Job()
        serverWorkScope = CoroutineScope(Dispatchers.Default + serverJob!!)

        serverWorkScope!!.launch {
            multicastSocket.loopbackMode = false
            joinGroup()

            while (isActive) {
                onReceive?.invoke(read())
            }
        }
    }

    override fun close() {
        if (serverJob?.isActive == true)
            serverJob?.cancel()

        super.close()
    }
}