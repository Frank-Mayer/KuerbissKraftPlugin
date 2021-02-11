package main

import com.google.common.collect.Sets.newHashSet
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material

class EntryProtector {
    private val dangerousBlocks = newHashSet(
        Material.LAVA,
        Material.FIRE
    )
    private val enterableBlocks = newHashSet(
        Material.AIR,
        Material.CAVE_AIR,
        Material.VOID_AIR,
        Material.TORCH,
        Material.REDSTONE_TORCH,
        Material.REDSTONE_WALL_TORCH,
        Material.WALL_TORCH,
        Material.NETHER_PORTAL,
        Material.END_PORTAL,
        Material.WATER,
        Material.VINE,
        Material.BLACK_CARPET,
        Material.BLUE_CARPET,
        Material.BROWN_CARPET,
        Material.CYAN_CARPET,
        Material.GRAY_CARPET,
        Material.GREEN_CARPET,
        Material.LIGHT_BLUE_CARPET,
        Material.LIGHT_GRAY_CARPET,
        Material.LIME_CARPET,
        Material.MAGENTA_CARPET,
        Material.ORANGE_CARPET,
        Material.PINK_CARPET,
        Material.PURPLE_CARPET,
        Material.RED_CARPET,
        Material.WHITE_CARPET,
        Material.YELLOW_CARPET,
        Material.SNOW,
        Material.LADDER,
        Material.LARGE_FERN,
        Material.SEAGRASS,
        Material.TALL_SEAGRASS
    )

    fun protect(location: Location) {
        for (y in 0 until 3) {
                val loc = Location(location.world, location.x, location.y + y, location.z)
                if (!enterableBlocks.contains(loc.block.type)) {
                    Bukkit.broadcastMessage("Removed not enterable Block ${loc.block.type}")
                    loc.block.type = Material.AIR
                }
        }
        for (x in -1 until 2) {
            for (y in 0 until 3) {
                for (z in -1 until 2) {
                    val loc = Location(location.world, location.x + x, location.y + y, location.z + z)
                    if (dangerousBlocks.contains(loc.block.type)) {
                        Bukkit.broadcastMessage("Removed dangerous Block ${loc.block.type}")
                        loc.block.type = Material.AIR
                    }
                }
            }
        }
    }
}
