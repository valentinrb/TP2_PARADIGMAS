package tp2_paradigmas

import kotlin.random.Random
import java.util.LinkedList

const val NUM_PAGE_ROUTER = 20
const val PAGE_SIZE = 2048u
const val CYCLES = 12
const val BAND_WIDTH = 1024u

enum class Task {
    RECEPTION,
    SEND_STORE
}

public class SystemManager (
    private var network: NetworkManager,
    private var sites: MutableList<Page>
) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val systemManager = SystemManager(generateRandomNetwork(NUM_PAGE_ROUTER), generateRandomPages(NUM_PAGE_ROUTER, PAGE_SIZE))            
            
            //systemManager.printPages()
            systemManager.network.printNetwork()
            
            systemManager.initTransmission()
            systemManager.setPath()

            //systemManager.network.printPathList()

            val cycles = CYCLES
            var task = Task.SEND_STORE

            for (cicle in 1..cycles) {
                println("--- Cicle $cicle - Task: $task ---")

                systemManager.network.getRouters().forEach { router ->
                    when (task) {
                        Task.SEND_STORE -> router.sendPackages(systemManager.network.getPathList())
                        Task.RECEPTION -> router.receivePackages()
                    }
                }

                task = when (task) {
                    Task.RECEPTION -> Task.SEND_STORE
                    Task.SEND_STORE -> Task.RECEPTION
                }
                
                if (cicle % 12 == 0) {
                    println("--- Generating more pages ---")

                    systemManager.sites.addAll(generateRandomPages(20, 2048u))
                    //systemManager.printPages()

                    systemManager.initTransmission()
                    systemManager.setPath()
                }

                if (cicle % 6 == 0) {
                    println("--- Recomputing optimal paths ---")
                    
                    systemManager.setPath()           
                    //systemManager.network.printPathList()
                }
                
                Thread.sleep(500)
            }
        }

        private fun generateRandomNetwork(routerCount: Int): NetworkManager {
            val routers = (1u..routerCount.toUInt()).map { Router(it, mutableListOf(), LinkedList()) }
            val connections = mutableListOf<Connection>()
        
            for (i in routers.indices) {
                val sourceRouter = routers[i]
                val destinyRouter = if (i == routers.size - 1) routers[0] else routers[i + 1]
                val bandwidth = BAND_WIDTH
                val connection = Connection(sourceRouter, destinyRouter, LinkedList(), bandwidth)
        
                sourceRouter.setConnection(connection)
                destinyRouter.setConnection(connection)
                connections.add(connection)
            }
        
            for (router in routers) {
                val remainingRouters = routers.filter { it != router && it !in router.getConnections().map { it.getDestiny() } }
                if (remainingRouters.isNotEmpty()) {
                    val randomDestiny = remainingRouters.random()
                    val bandwidth = BAND_WIDTH
                    val connection = Connection(router, randomDestiny, LinkedList(), bandwidth)
        
                    router.setConnection(connection)
                    randomDestiny.setConnection(connection)
                    connections.add(connection)
                }
            }
        
            return NetworkManager(routers, connections, mutableListOf())
        }
        
        private fun generateRandomPages(quantity: Int, size: UInt): MutableList<Page> {
            return MutableList(quantity) { id ->
                val content = generateRandomContent(size)
                Page((id + 1).toUInt(), content.size.toUInt(), content)
            }
        }

        private fun generateRandomContent(size: UInt): ByteArray {
            val contentSize = Random.nextInt(1, size.toInt() + 1)
            return ByteArray(contentSize) { Random.nextInt(256).toByte() }
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
    /* 
    private fun setPath() {
        //network.clearPathList()

        network.getRouters().forEach { sourceRouter ->
            val destinyRouters = getDestinyRouters(sourceRouter)

            destinyRouters.forEach { destinyRouter ->
                val path = network.findShortestPath(sourceRouter, destinyRouter)

                network.setPathList(path)
            }
        }
    }*/

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
