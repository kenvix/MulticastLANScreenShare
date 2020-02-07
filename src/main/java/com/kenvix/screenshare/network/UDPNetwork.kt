//--------------------------------------------------
// Interface UDPNetwork
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.MulticastSocket

interface UDPNetwork {
    var maxPacketSize: Int

    suspend fun send(packet: DatagramPacket)
    suspend fun send(byteArray: ByteArray, length: Int = byteArray.size, offset: Int = 0)
    suspend fun read(length: Int = maxPacketSize): DatagramPacket
    suspend fun joinGroup()
    suspend fun leaveGroup()
}