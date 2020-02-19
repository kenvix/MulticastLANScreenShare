//--------------------------------------------------
// Class MulticastNetwork
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.*

abstract class MulticastNetwork(
    val multicastAddress: InetAddress = InetAddress.getByName("230.114.5.14"),
    val multicastPort: Int = 1919,
    val networkInterface: NetworkInterface? = null
) : AutoCloseable, UDPNetwork {

    val multicastSocketAddress = InetSocketAddress(multicastAddress, multicastPort)
    val multicastSocket: MulticastSocket
    override var maxPacketSize: Int = 1200

    init {
        multicastSocket = MulticastSocket(multicastPort)

        if (networkInterface != null)
            multicastSocket.networkInterface = networkInterface
    }

    override suspend fun send(packet: DatagramPacket) = withContext(Dispatchers.IO) {
        multicastSocket.send(packet)
    }

    override suspend fun send(byteArray: ByteArray, length: Int, offset: Int)
        = send(DatagramPacket(byteArray, offset, length, multicastAddress, multicastPort))

    override suspend fun read(length: Int): DatagramPacket = withContext(Dispatchers.IO) {
        val packet = DatagramPacket(ByteArray(length), 0, length)
        multicastSocket.receive(packet)

        packet
    }

    override suspend fun joinGroup() = withContext(Dispatchers.IO) {
        multicastSocket.joinGroup(multicastAddress)
    }

    override suspend fun leaveGroup() = withContext(Dispatchers.IO) {
        multicastSocket.leaveGroup(multicastAddress)
    }

    override fun close() {
        //multicastSocket.leaveGroup(multicastAddress)
        multicastSocket.close()
    }

    override fun toString() = "${this::class.java.simpleName} @ $multicastAddress:$multicastPort"
}