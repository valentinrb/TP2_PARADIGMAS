package tp2_paradigmas

public class Page (
    private val id: UInt,
    private val size: UInt,
    private val content: ByteArray
) {    
    fun getId() = id
    fun getSize() = size
    fun getContent() = content

    fun printPage() {
        println("Page [${id}] Size [${size}]")
    }
}