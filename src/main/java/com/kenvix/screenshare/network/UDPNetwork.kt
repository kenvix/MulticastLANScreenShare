//--------------------------------------------------
// Interface UDPNetwork
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.network

import java.net.DatagramPacket

interface UDPNetwork {
    var maxPacketSize: Int
    var onReceive: ((packet: DatagramPacket) -> Unit)?

    suspend fun send(packet: DatagramPacket)
    suspend fun send(byteArray: ByteArray, length: Int = byteArray.size, offset: Int = 0)
    suspend fun read(length: Int = maxPacketSize): DatagramPacket
    suspend fun joinGroup()
    suspend fun leaveGroup()
}