package tp2_paradigmas

import kotlin.random.Random
import java.util.LinkedList

public class SystemManager (
    private var network: NetworkManager,
    private var sites: List<Page>
) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val systemManager = SystemManager(generateRandomNetwork(5), generateRandomPages(5, 1024u))
            
            systemManager.network.printNetwork()

            systemManager.network.initTransmission(systemManager.sites.random())
        }

        fun generateRandomNetwork(routerCount: Int): NetworkManager {
            val routers = (1..routerCount).map { Router((it % 256).toUInt(), mutableListOf()) }
            val establishedConnections = mutableSetOf<Pair<Router, Router>>()

            routers.forEach { router ->
                val connectedRouters = routers.filter { it != router }
                val numConnections = (1..5 - 1).random()
        
                repeat(numConnections) {
                    val randomDestiny = connectedRouters.random()
                    val connectionPair = Pair(router, randomDestiny)
                    val inverseConnectionPair = Pair(randomDestiny, router)
        
                    if (connectionPair !in establishedConnections && inverseConnectionPair !in establishedConnections) {
                        val bandwidth = (1u..10u).random()
        
                        val connection = Connection(router, randomDestiny, LinkedList(), bandwidth)
                        router.addConnection(connection)
        
                        val reverseConnection = Connection(randomDestiny, router, LinkedList(), bandwidth)
                        randomDestiny.addConnection(reverseConnection)
        
                        establishedConnections.add(connectionPair)
                        establishedConnections.add(inverseConnectionPair)
                    }
                }
            }
    
            val connections = routers.flatMap { it.getConnections() }.distinct().toMutableList()

            return NetworkManager(routers, connections)
        }
        
        fun generateRandomPages(quantity: Int, size: UInt): List<Page> {
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
}
