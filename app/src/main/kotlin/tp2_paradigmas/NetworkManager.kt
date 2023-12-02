package tp2_paradigmas

import java.util.PriorityQueue

public class NetworkManager (
    private val routers: List<Router>,
    private var connections: List<Connection>,
    private var pathList: MutableList<Path>
) {
    fun initTransmission(sourceRouter: Router, page: Page) {
        var destRouter = routers.random()

        while (sourceRouter == destRouter)
            destRouter = routers.random()

        sourceRouter.createPackages(page, destRouter)
    }

    fun findOptimalPath(sourceRouter: Router, destinationRouter: Router): Path {
        val delays = HashMap<Router, UInt>()
        val visited = HashSet<Router>()
        val queue = PriorityQueue<Router>(compareBy { delays.getOrDefault(it, UInt.MAX_VALUE) })
        val previous = HashMap<Router, Router?>()
    
        delays[sourceRouter] = 1u
        queue.offer(sourceRouter)
    
        while (queue.isNotEmpty()) {
            val currentRouter = queue.poll()
    
            if (currentRouter == destinationRouter)
                return buildPath(destinationRouter, previous, sourceRouter)
    
            if (visited.contains(currentRouter)) continue
    
            visited.add(currentRouter)
    
            for (connection in currentRouter.getConnections()) {
                val neighbor = connection.getDestiny()
                val bandwidth = connection.getBandWidth()
                val bufferQueueSize = connection.getBufferQueueSize()
                val newDelay = delays.getOrDefault(currentRouter, UInt.MAX_VALUE) + calculateDelay(bandwidth, bufferQueueSize)
    
                if (newDelay < delays.getOrDefault(neighbor, UInt.MAX_VALUE)) {
                    delays[neighbor] = newDelay
                    previous[neighbor] = currentRouter
                    queue.offer(neighbor)
                }
            }
        }
    
        throw NoSuchElementException("No se encontró un camino entre $sourceRouter y $destinationRouter")
    }
    
    private fun calculateDelay(bandwidth: UInt, bufferQueueSize: Int): UInt {
        return bufferQueueSize.toUInt() / bandwidth
    }
    
    private fun buildPath(destination: Router, previous: HashMap<Router, Router?>, sourceRouter: Router): Path {
        val path = mutableListOf<Router>()
        var current: Router? = destination
    
        while (current != null) {
            path.add(current)
            current = previous[current]
        }
        
        return Path(sourceRouter.getIp(), destination.getIp(), path.reversed().toMutableList())
    }

    fun printNetwork() {
        routers.forEach { router ->
            val routerIp = router.getIp()
            val connectionsString = router.getConnections().joinToString(", ") { connection ->
                val sourceId = connection.getSource().getIp()
                val destinyId = connection.getDestiny().getIp()
                "$sourceId -> $destinyId"
            }
    
            println("Router [$routerIp] Connections: [$connectionsString]")
        }
    }

    fun printPathList() {
        pathList.forEach { path ->
            print("Path List: ")
    
            path.getPath().forEachIndexed { pathIndex, router ->
                print(router.getIp())
    
                if (pathIndex < path.getPath().size - 1) {
                    print(" -> ")
                }
            }
    
            println()
        }
    }

    fun clearPathList() = pathList.clear()

    fun setPathList(path: Path) { pathList.add(path) }
    fun getPathList() = pathList
    
    fun getRouters() = routers
    fun getConnections() = connections
}