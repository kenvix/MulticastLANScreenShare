//--------------------------------------------------
// Class MulticastNetwork
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket

abstract class MulticastNetwork(
    val multicastAddress: InetAddress = InetAddress.getByName("230.114.5.14"),
    val multicastPort: Int = 1919
) : AutoCloseable {

    val multicastSocket: MulticastSocket = MulticastSocket(multicastPort)
    var maxPacketSize: Int = 1200

    suspend fun send(packet: DatagramPacket) = withContext(Dispatchers.IO) {
        multicastSocket.send(packet)
    }

    suspend fun send(byteArray: ByteArray, length: Int = byteArray.size, offset: Int = 0)
            = send(DatagramPacket(byteArray, offset, length, multicastAddress, multicastPort))

    suspend fun read(length: Int = maxPacketSize): DatagramPacket = withContext(Dispatchers.IO) {
        val packet = DatagramPacket(ByteArray(length), 0, length)
        multicastSocket.receive(packet)

        packet
    }

    suspend fun joinGroup() = withContext(Dispatchers.IO) {
        multicastSocket.joinGroup(multicastAddress)
    }

    suspend fun leaveGroup() = withContext(Dispatchers.IO) {
        multicastSocket.leaveGroup(multicastAddress)
    }

    override fun close() {
        multicastSocket.close()
    }

    override fun toString() = "${this::class.java.simpleName} @ $multicastAddress:$multicastPort"
}