package tp2_paradigmas

import java.util.PriorityQueue

public class NetworkManager (
    private val routers: List<Router>,
    private var connections: List<Connection>
) {
    fun initTransmission(page: Page) {
        val sourceRouter = routers.random()
        var destRouter = routers.random()

        while (sourceRouter == destRouter)
            destRouter = routers.random()

        println("Routers 'src' 'dst': ${sourceRouter.getIp()} -> ${destRouter.getIp()}")

        val path = findShortestPath(sourceRouter, destRouter)
            ?: throw NullPointerException("El camino más corto no se encontró")

        path.forEachIndexed { index, router ->
            print(router.getIp())
        
            if (index < path.size - 1) {
                print(" -> ")
            } else {
                println()
            }
        }

        sourceRouter.sendPage(page, destRouter, path)
    }

    fun findShortestPath(sourceRouter: Router, destinationRouter: Router): List<Router>? {
        val distances = HashMap<Router, UInt>()
        val visited = HashSet<Router>()
        val queue = PriorityQueue<Router>(compareBy { distances.getOrDefault(it, UInt.MAX_VALUE) })
        val previous = HashMap<Router, Router?>()
    
        distances[sourceRouter] = 0u
        queue.offer(sourceRouter)
    
        while (queue.isNotEmpty()) {
            val currentRouter = queue.poll()
    
            if (currentRouter == destinationRouter)
                return buildPath(destinationRouter, previous)
    
            if (visited.contains(currentRouter)) continue
    
            visited.add(currentRouter)
    
            for (connection in currentRouter.getConnections()) {
                val neighbor = connection.getDestiny()
                val bandwidth = connection.getBandWidth()
                val newDistance = distances.getOrDefault(currentRouter, UInt.MAX_VALUE) + bandwidth
    
                if (newDistance < distances.getOrDefault(neighbor, UInt.MAX_VALUE)) {
                    distances[neighbor] = newDistance
                    previous[neighbor] = currentRouter
                    queue.offer(neighbor)
                }
            }
        }
    
        return null
    }
    
    private fun buildPath(
        destination: Router,
        previous: HashMap<Router, Router?>
    ): List<Router> {
        val path = mutableListOf<Router>()
        var current: Router? = destination
    
        while (current != null) {
            path.add(current)
            current = previous[current]
        }
    
        return path.reversed()
    }

    fun printNetwork() {
        routers.forEach { router ->
            val routerId = router.getIp()
            val connectionsString = router.getConnections().joinToString(", ") { connection ->
                val sourceId = connection.getSource().getIp()
                val destinyId = connection.getDestiny().getIp()
                "$sourceId -> $destinyId"
            }
    
            println("$routerId: [$connectionsString]")
        }
    }

    fun getRouters() = routers
}