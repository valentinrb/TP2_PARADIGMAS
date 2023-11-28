package tp2_paradigmas

public class Package (
    private val id: UInt,
    private val pageId: UInt,
    private val size: UInt,
    private val data: ByteArray,
    private val source: Router,
    private val destiny: Router,
    private val last: Boolean
) {
    fun getId() = id
    fun getPageId() = pageId
    fun getSize() = size
    fun getData() = data
    fun getSource() = source
    fun getDestiny() = destiny
    fun getLastFlag() = last

    fun printPackage() {
        println("Package ID: $id")
        println("Source Router: ${source.getIp()}")
        println("Destiny Router: ${destiny.getIp()}")
    }
}