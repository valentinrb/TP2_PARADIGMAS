package tp2_paradigmas

import kotlin.random.Random
import java.util.LinkedList

enum class Task {
    RECEPTION,
    SEND_STORE
}

public class SystemManager (
    private var network: NetworkManager,
    private var sites: List<Page>
) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val systemManager = SystemManager(generateRandomNetwork(5), generateRandomPages(5, 1024u))            
            systemManager.network.printNetwork()

            systemManager.initTransmission()
            systemManager.setPath()

            systemManager.network.getRouters().forEach { router ->
                router.printPathList()
            }

            val cycles = 2
            var task = Task.SEND_STORE

            for (cicle in 1..cycles) {
                println("Ciclo $cicle - Tarea: $task")   

                systemManager.network.getRouters().forEach { router ->
                    when (task) {
                        Task.SEND_STORE -> router.sendPackages()
                        Task.RECEPTION -> router.receivePackages()
                    }
                }

                task = when (task) {
                    Task.RECEPTION -> Task.SEND_STORE
                    Task.SEND_STORE -> Task.RECEPTION
                }
                
                if (cicle % 5 == 0) {
                    println("Administrador toma el control para recomputar caminos óptimos")
                }
                
                Thread.sleep(1000)
            }
        }

        private fun generateRandomNetwork(routerCount: Int): NetworkManager {
            val routers = (1..routerCount).map { Router((it % 256).toUInt(), mutableListOf(), mutableListOf()) }
            val establishedConnections = mutableSetOf<Pair<Router, Router>>()

            routers.forEach { router ->
                val connectedRouters = routers.filter { it != router }
                val numConnections = (1..5 - 1).random()
        
                repeat(numConnections) {
                    val randomDestiny = connectedRouters.random()
                    val connectionPair = Pair(router, randomDestiny)
                    val inverseConnectionPair = Pair(randomDestiny, router)
        
                    if (connectionPair !in establishedConnections && inverseConnectionPair !in establishedConnections) {
                        val bandwidth = 256u
        
                        val connection = Connection(router, randomDestiny, LinkedList(), bandwidth)
                        router.setConnection(connection)
                        
                        val reverseConnection = Connection(router, randomDestiny, LinkedList(), bandwidth)
                        randomDestiny.setConnection(reverseConnection)
        
                        establishedConnections.add(connectionPair)
                        establishedConnections.add(inverseConnectionPair)
                    }
                }
            }
    
            val connections = routers.flatMap { it.getConnections() }.distinct().toMutableList()

            return NetworkManager(routers, connections)
        }
        
        private fun generateRandomPages(quantity: Int, size: UInt): List<Page> {
            return List(quantity) { id ->
                val content = generateRandomContent(size)
                Page(id.toUInt(), content.size.toUInt(), content)
            }
        }

        private fun generateRandomContent(size: UInt): ByteArray {
            val contentSize = Random.nextInt(1, size.toInt() + 1)
            return ByteArray(contentSize) { Random.nextInt(256).toByte() }
        }
    }

    private fun initTransmission() {
        this.network.getRouters().forEach { sourceRouter ->
            this.network.initTransmission(sourceRouter, this.sites.random())
        }
    }

    private fun setPath() {
        this.network.getRouters().forEach { sourceRouter ->
            val destinyRouters = getDestinyRouters(sourceRouter)

            destinyRouters.forEach { destinyRouter ->
                sourceRouter.setPathList(this.network.findShortestPath(sourceRouter, destinyRouter))
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
}
