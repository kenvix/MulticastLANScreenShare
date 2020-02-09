//--------------------------------------------------
// Class ScreenVideoPacket
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.network

import java.nio.ByteBuffer


data class UDPPacket(
    val type: Byte,
    val length: Short,
    val data: ByteArray
) {

    fun toByteArray(): ByteArray {
        return ByteBuffer.allocate(data.size + 2 + 1).put(type).putShort(length).put(data).array()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UDPPacket

        if (type != other.type) return false
        if (length != other.length) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.toInt()
        result = 171 * result + length.toInt()
        result = 171 * result + data.contentHashCode()
        return result
    }
}