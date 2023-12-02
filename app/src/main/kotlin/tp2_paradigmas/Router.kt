package tp2_paradigmas

import java.util.Arrays
import java.util.Queue

public class Router (
    private val ip: UInt,
    private var devices: MutableList<Connection>,
    private var internalBuffer: Queue<Package>
) {
    private var packagesToStore = mutableListOf<Package>()

    fun sendPackages(pathList: MutableList<Path>) {
        if (internalBuffer.isNotEmpty()) {
            val packet = internalBuffer.poll()
            val path = pathList.find { it.getSrcRouterIp() == packet.getSource().getIp() && it.getDstRouterIp() == packet.getDestiny().getIp() }
                ?: throw NoSuchElementException("No se encontró un camino para el paquete con origen IP: ${packet.getSource().getIp()} y destino IP: ${packet.getDestiny().getIp()}")        
            
            val nextRouter = determineNextHop(this, path)
            val connection = findConnection(this, nextRouter)
                ?: throw NullPointerException("La conexión no se encontró")
            
            val remainingBandwidth = connection.getRemainingBandwidth()
        
            if (packet.getSize() <= remainingBandwidth)
            {
                connection.addToBuffer(packet)
                connection.addBandwidthUsage(packet.getSize())
                println("Router [${ip}] - Packet sent ID[${packet.getId()}] from router [${packet.getSource().getIp()}] to router [${nextRouter.getIp()}] | with destiny router [${packet.getDestiny().getIp()}]")
            }
            else {
                println("Router [${ip}] - Connection [${connection.getSource().getIp()} -> ${connection.getDestiny().getIp()}] Buffer saturated - Remaining Bandwidth [${remainingBandwidth}].")
            }
        } else {
            println("Router [${ip}] - No packages to send.")
        }
    }

    fun receivePackages() {
        devices.filter { it.getDestiny() == this }.forEach { connection ->
            if (connection.isEmptyBuffer())
            {
                println("Router [${ip}] - Empty buffer.")
                return@forEach
            }
            
            val packet = connection.getNextPackage()

            if (packet.getDestiny().equals(this))
            {
                packagesToStore.add(packet)
                connection.rmvToBuffer(packet)
                connection.subBandwidthUsage(packet.getSize())

                println("Router [${ip}] - Packet stored ID[${packet.getId()}] from router [${packet.getSource().getIp()}] with destiny router [${packet.getDestiny().getIp()}]")
                
                if(packet.getLastFlag())
                    managePageRebuild(packet.getPageId())
            }
            else
            {
                println("Router [${ip}] - Packet buffered ID[${packet.getId()}] from router [${packet.getSource().getIp()}] with destiny router [${packet.getDestiny().getIp()}]")

                internalBuffer.offer(packet)
                connection.rmvToBuffer(packet)
                connection.subBandwidthUsage(packet.getSize())
            }
        }
    }

    fun createPackages(page: Page, destinationRouter: Router) {
        val packageSize: UInt = (devices.first().getBandWidth() / 4u)
        val numPackages = Math.ceil(page.getSize().toDouble() / packageSize.toDouble()).toUInt()
    
        for (i in 1..numPackages.toInt()) {
            val packageData = getPackageData(page.getContent(), i - 1, packageSize)
    
            val isLastPackage = i == numPackages.toInt()
            internalBuffer.offer(Package(i.toUInt(), page.getId(), packageData.size.toUInt(), packageData, this, destinationRouter, isLastPackage))
        }
    }

    private fun managePageRebuild(pageId: UInt)
    {
        val packagesToReconstruct = packagesToStore.filter { it.getPageId() == pageId }
        
        packagesToStore.removeAll(packagesToReconstruct)
        
        val page = rebuildPage(packagesToReconstruct)

        print("Router [${ip}] - Reconstruct ")

        page.printPage()
    }

    private fun rebuildPage(packages: List<Package>): Page {        
        val totalSize = packages.sumOf { it.getSize() }
        val reconstructedContent = ByteArray(totalSize.toInt())

        var currentIndex = 0u

        packages.forEach {pkg -> 
            val data = pkg.getData()
            val dataSize = pkg.getSize()
            
            System.arraycopy(data, 0, reconstructedContent, currentIndex.toInt(), dataSize.toInt())
            currentIndex += dataSize
        }

        return Page(packages.first().getPageId(), reconstructedContent.size.toUInt(), reconstructedContent)
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

    private fun determineNextHop(currentRouter: Router, path: Path): Router {
        val currentIndex = path.getPath().indexOf(currentRouter)
    
        if (currentIndex != -1 && currentIndex < path.getPath().size - 1)
            return path.getPath()[currentIndex + 1]
    
        return currentRouter
    }    

    fun printPackages() {
        internalBuffer.forEach { packet -> 
            println("Package ID: ${packet.getId()}")
            println("Source Router: ${packet.getSource().getIp()}")
            println("Destiny Router: ${packet.getDestiny().getIp()}")
        }
    }
     
    fun setConnection(newConnection: Connection) = devices.add(newConnection)

    fun getPackagesToSend() = internalBuffer

    fun getIp() = ip
    fun getConnections() = devices
}