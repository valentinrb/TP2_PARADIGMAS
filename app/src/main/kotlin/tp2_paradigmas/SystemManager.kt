package tp2_paradigmas

import kotlin.random.Random
import kotlin.text.toInt
import kotlin.text.toUInt
import kotlin.text.toUIntOrNull
import java.util.LinkedList
import java.util.Scanner

enum class Task {
    RECEPTION,
    SEND
}

public class SystemManager (
    private var network: NetworkManager,
    private var sites: MutableList<Page>
) {
    companion object {
        private var nextPageId: UInt = 1u

        @JvmStatic
        fun main(args: Array<String>) {
            val (numPageRouter, pageSize, cycles, bandWidth) = initSimulation()

            val systemManager = SystemManager(generateRandomNetwork(numPageRouter, bandWidth), generateRandomPages(numPageRouter, pageSize))            
            
            systemManager.printPages()
            systemManager.network.printNetwork()
            
            systemManager.initTransmission()
            systemManager.setPath()

            //systemManager.network.printPathList()

            var task = Task.SEND

            for (cicle in 1..cycles.toInt()) {
                println("--- Cicle $cicle - Task: $task ---")

                systemManager.network.getRouters().forEach { router ->
                    when (task) {
                        Task.SEND -> router.sendPackages(systemManager.network.getPathList())
                        Task.RECEPTION -> router.receivePackages()
                    }
                }

                task = when (task) {
                    Task.RECEPTION -> Task.SEND
                    Task.SEND -> Task.RECEPTION
                }
                
                if (cicle % 12 == 0) {
                    println("--- Generating more pages ---")

                    systemManager.sites.addAll(generateRandomPages(numPageRouter, pageSize))
                    systemManager.printPages()

                    systemManager.initTransmission()
                    systemManager.setPath()
                }

                if (cicle % 6 == 0) {
                    println("--- Recomputing optimal paths ---")
                    
                    systemManager.setPath()           
                }
                
                Thread.sleep(500)
            }
        }

        private fun generateRandomNetwork(routerCount: UInt, bandwidth: UInt): NetworkManager {
            val routers = (1u..routerCount.toUInt()).map { Router(it, mutableListOf(), LinkedList()) }
            val connections = mutableListOf<Connection>()
        
            for (i in routers.indices) {
                val sourceRouter = routers[i]
                val destinyRouter = if (i == routers.size - 1) routers[0] else routers[i + 1]
                val connection = Connection(sourceRouter, destinyRouter, LinkedList(), bandwidth)
        
                sourceRouter.setConnection(connection)
                destinyRouter.setConnection(connection)
                connections.add(connection)
            }
        
            for (router in routers) {
                val remainingRouters = routers.filter { it != router && it !in router.getConnections().map { it.getDestiny() } }
                if (remainingRouters.isNotEmpty()) {
                    val randomDestiny = remainingRouters.random()
                    val connection = Connection(router, randomDestiny, LinkedList(), bandwidth)
        
                    router.setConnection(connection)
                    randomDestiny.setConnection(connection)
                    connections.add(connection)
                }
            }
        
            return NetworkManager(routers, connections, mutableListOf())
        }
        
        private fun generateRandomPages(quantity: UInt, size: UInt): MutableList<Page> {
            val newPages = MutableList(quantity.toInt()) {
            val content = generateRandomContent(size)
            Page(nextPageId++, content.size.toUInt(), content)
        }

        return newPages
        }

        private fun generateRandomContent(size: UInt): ByteArray {
            val contentSize = Random.nextInt(1, size.toInt() + 1)
            return ByteArray(contentSize) { Random.nextInt(256).toByte() }
        }

        private fun initSimulation(): List<UInt> {
            println("Inicializando la simulacion de red. Por favor, ingrese los siguientes parametros:")

            println("--- Numero de routers y paginas ---")
            val numPageRouter = readLine()?.toUInt() ?: 0u

            println("--- Tamano de la pagina ---")
            val pageSize = readLine()?.toUInt() ?: 0u

            println("--- Ancho de banda ---")
            val bandWidth = readLine()?.toUInt() ?: 0u

            println("--- Numero de ciclos ---")
            val cycles = readLine()?.toUInt() ?: 0u

            return listOf(numPageRouter, pageSize.toUInt(), cycles.toUInt(), bandWidth.toUInt())
        }
    }

    private fun initTransmission() {
        network.getRouters().forEach { sourceRouter ->
            val randomPage = sites.random()
            randomPage.let {
                network.initTransmission(sourceRouter, it)
                sites.remove(it)
            }
        }
    }

    private fun setPath() {
        network.clearPathList()

        network.getRouters().forEach { sourceRouter ->
            network.getRouters().filter { it != sourceRouter }.forEach { destinyRouter ->
                val path = network.findOptimalPath(sourceRouter, destinyRouter)
    
                network.setPathList(path)
            }
        }
    }

    private fun getDestinyRouters(router: Router): MutableList<Router> {
        val destinyRouters = mutableListOf<Router>()
        val packagesToSend = router.getPackagesToSend()
    
        packagesToSend.forEach { packet ->
            val destRouter = packet.getDestiny()

            if (!destinyRouters.contains(destRouter))
                destinyRouters.add(destRouter)
        }

        return destinyRouters
    }

    private fun printPages() {
        sites.forEach { page -> 
            page.printPage()
        }
    }
}
