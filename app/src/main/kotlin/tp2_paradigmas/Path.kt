package tp2_paradigmas

public class Path (
    private var srcRouterIp: UInt,
    private var dstRouterIp: UInt,
    private var path: MutableList<Router>
) {
    fun getSrcRouterIp() = srcRouterIp
    fun getDstRouterIp() = dstRouterIp
    fun getPath() = path

    fun printPath() {
        path.forEachIndexed { index, router ->
            print(router.getIp())

            if (index < path.size - 1) {
                print(" -> ")
            }
        }

        println()
    }
}