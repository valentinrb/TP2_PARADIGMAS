package tp2_paradigmas

import java.util.Queue

public class Connection (
    private val source: Router,
    private val destiny: Router,
    private var buffer: Queue<Package>,
    private val bandWidth: UInt
) {
    private var currentBandwidthUsage: UInt = 0u

    fun addToBuffer(packet: Package) = buffer.offer(packet)
    fun rmvToBuffer(packet: Package) = buffer.remove(packet)

    fun getNextPackage() = buffer.first()
    fun isEmptyBuffer() = buffer.isEmpty()
    
    fun addBandwidthUsage(packageSize: UInt) { currentBandwidthUsage += packageSize }
    fun subBandwidthUsage(packageSize: UInt) { currentBandwidthUsage -= packageSize }
    fun getRemainingBandwidth() = bandWidth - currentBandwidthUsage

    fun getSource() = source
    fun getDestiny() = destiny
    fun getBandWidth() = bandWidth

    fun getBufferQueueSize() = buffer.size

    fun printBuffer() {
        print("Packages at buffer '${source.getIp()} -> ${destiny.getIp()}': ")
        buffer.forEach { packet ->
            println("${packet.getId()}")
        }
    }
}