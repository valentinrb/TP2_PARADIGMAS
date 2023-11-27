package tp2_paradigmas

import java.util.Arrays

public class Router (
    private val ip: UInt,
    private var devices: MutableList<Connection>,
    private var pathList: MutableList<Path>
) {
    private var packagesToSend = mutableListOf<Package>()
    private var packagesToStore = mutableListOf<Package>()

    fun sendPackages() {
        if (packagesToSend.isNotEmpty()) {
            val packet = packagesToSend.first()
            
            if (packet.getDestiny().equals(this))
                return
            
            packagesToSend.removeAt(0)

            val path = pathList.find { it.getSrcRouterIp() == packet.getSource().getIp() && it.getDstRouterIp() == packet.getDestiny().getIp() }
                ?: throw NoSuchElementException("No se encontró un camino para el paquete con origen IP: ${packet.getSource().getIp()} y destino IP: ${packet.getDestiny().getIp()}")        

            val nextRouter = determineNextHop(this, path)
            val connection = findConnection(this, nextRouter)
                ?: throw NullPointerException("La conexión no se encontró")
    
            val remainingBandwidth = connection.getRemainingBandwidth()
        
            if (packet.getSize() <= remainingBandwidth)
            {
                connection.addToBuffer(packet)
                connection.updateBandwidthUsage(packet.getSize())
            }

            println("Packet sent: ${packet.getId()} from: ${packet.getSource().getIp()} to: ${nextRouter.getIp()} | with destiny: ${packet.getDestiny().getIp()}")
        } else {
            println("No packages to send.")
        }

        return
    }

    fun receivePackages() {
        devices.forEach { connection ->
            if (connection.isEmptyBuffer())
            {
                println("Router [${ip}] - Empty buffer.")
                return@forEach
            }
            
            val packet = connection.getNextPackage()

            println("Router [${ip}] Connection [${connection.getSource().getIp()} -> ${connection.getDestiny().getIp()}]")
            packet.printPackage()

            if (packet.getDestiny().equals(this))
            {
                packagesToStore.add(packet)
                connection.rmvToBuffer(packet)

                println("Router [${ip}] - Packet received: ${packet.getId()} from: ${packet.getSource().getIp()} with destiny: ${packet.getDestiny().getIp()}")
                //if() Verificar que esten todos los paquetes para rearmar la página.
            }
            else
            {
                println("Router [${ip}] - No packages to store.")  
            }
        }
    }

    fun createPackages(page: Page, destinationRouter: Router) {
        val packageSize: UInt = 256u
        val numPackages = Math.ceil(page.getSize().toDouble() / packageSize.toDouble()).toUInt()
    
        for (i in 0 until numPackages.toInt()) {
            val packageData = getPackageData(page.getContent(), i, packageSize)

            packagesToSend.add(Package(i.toUInt(), packageData.size.toUInt(), packageData, this, destinationRouter))
        }
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

    private fun determineNextHop(currentRouter: Router, path: Path): Router {
        val currentIndex = path.getPath().indexOf(currentRouter)
    
        if (currentIndex != -1 && currentIndex < path.getPath().size - 1)
            return path.getPath()[currentIndex + 1]
    
        return currentRouter
    }    

    fun printPackages() {
        packagesToSend.forEach { packet -> 
            println("Package ID: ${packet.getId()}")
            println("Source Router: ${packet.getSource().getIp()}")
            println("Destiny Router: ${packet.getDestiny().getIp()}")
        }
    }
     
    fun printPathList() {
        pathList.forEach { path ->
            print("Path List for Router $ip: ")
    
            path.getPath().forEachIndexed { pathIndex, router ->
                print(router.getIp())
    
                if (pathIndex < path.getPath().size - 1) {
                    print(" -> ")
                }
            }
    
            println()
        }
    }

    fun setConnection(newConnection: Connection) = devices.add(newConnection)

    fun setPathList(path: Path) { pathList.add(path) }
    fun getPathList() = pathList

    fun getPackagesToSend() = packagesToSend

    fun getIp() = ip
    fun getConnections() = devices
}