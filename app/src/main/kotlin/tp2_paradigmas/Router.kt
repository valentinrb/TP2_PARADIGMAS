package tp2_paradigmas

import java.util.Arrays

public class Router (
    private val ip: UInt,
    private var devices: MutableList<Connection>
) {
    fun sendPage(page: Page, destinationRouter: Router, path: List<Router>)
    {
        val packages = createPackages(page, destinationRouter)
    
        sendPackage(packages, path)
    }

    fun sendPackage(packages: List<Package>, path: List<Router>) {
        val nextRouter = determineNextHop(this, path)
        val connection = findConnection(this, nextRouter)
            ?: throw NullPointerException("La conexión no se encontró")

        val remainingBandwidth = connection.getBandWidth()
        var totalWeightSent = 0u
    
        packages.forEach { packet ->
            if (totalWeightSent + packet.getSize() <= remainingBandwidth) {
                connection.addToQueue(packet)
                totalWeightSent += packet.getSize()
            } else {
                println("El ancho de banda se ha agotado. Se enviaron paquetes con un peso total de $totalWeightSent.")
                return
            }
        }
    }

    fun createPackages(page: Page, destinationRouter: Router): List<Package> {
        val packageSize: UInt = 256u
        val numPackages = Math.ceil(page.getSize().toDouble() / packageSize.toDouble()).toUInt()
    
        val packages = mutableListOf<Package>()
    
        for (i in 0 until numPackages.toInt()) {
            val packageData = getPackageData(page.getContent(), i, packageSize)

            packages.add(Package(i.toUInt(), packageData.size.toUInt(), packageData, this, destinationRouter))
        }
    
        return packages
    }

    fun reconstructPage(packages: List<Package>): ByteArray {        
        val totalSize = packages.sumOf { it.getSize() }

        val reconstructedContent = ByteArray(totalSize.toInt())

        var currentIndex = 0u

        packages.forEach {pkg -> 
            val data = pkg.getData()
            val dataSize = pkg.getSize()
            
            System.arraycopy(data, 0, reconstructedContent, currentIndex.toInt(), dataSize.toInt())
            currentIndex += dataSize
        }

        return reconstructedContent
    }

    private fun getPackageData(pageContent: ByteArray, packageIndex: Int, packageSize: UInt): ByteArray {
        val startIndex = (packageIndex * packageSize.toInt()).coerceAtMost(pageContent.size)
        val endIndex = ((packageIndex + 1) * packageSize.toInt()).coerceAtMost(pageContent.size)
        
        return pageContent.copyOfRange(startIndex, endIndex)
    }

    private fun findConnection(source: Router, destiny: Router): Connection? {
        return devices.find { connection ->
            connection.getSource() == source && connection.getDestiny() == destiny
        }
    }

    private fun determineNextHop(currentRouter: Router, path: List<Router>): Router {    
        val currentIndex = path.indexOf(currentRouter)
    
        if (currentIndex != -1 && currentIndex < path.size - 1) 
            return path[currentIndex + 1]
    
        return currentRouter
    }

    fun addConnection(connection: Connection) = devices.add(connection)

    fun getIp() = ip
    fun getConnections() = devices
}