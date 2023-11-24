package tp2_paradigmas

import java.util.Queue

public class Connection (
    private val source: Router,
    private val destiny: Router,
    private var buffer: Queue<Package>,
    private val bandWidth: UInt
) {
    fun addToQueue(packageData: Package) = buffer.offer(packageData)

    fun getSource() = source
    fun getDestiny() = destiny
    fun getBandWidth() = bandWidth
}