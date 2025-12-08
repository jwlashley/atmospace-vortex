package space.atmo.vortex.data

object VxMapper {

    class Counter(val name: String) {
        private val counts = mutableMapOf<String, Int>()

        fun incrementCount(key: String) {
            val currentCount = counts.getOrDefault(key, 0)
            counts[key] = currentCount + 1
        }

        fun getCounts(): Map<String, Int> {
            return counts.toMap()
        }
    }

    var blockRightClickCount = Counter("Blocks Used")
    var blockPlaceCount = Counter("Blocks Placed")
    var itemRightClickCount = Counter("Items Used")
    var recipeCraftCount = Counter("Recipes Crafted")
    var entityDamageCount = Counter("Entities Damaged")
    var chunkGenerationCount = Counter("Chunks Generated")
    var commandUsageCount = Counter("Commands Used")

}