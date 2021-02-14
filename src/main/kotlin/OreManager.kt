package main

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import kotlin.math.roundToInt

class OreManager(private val plugin: Plugin) {

    private val common = 25
    private val uncommon = 50
    private val rare = 75
    private val veryRare = 120

    fun mine(block: Block, itemStack: ItemStack): Boolean {
        val location = block.location
        if (location.world != null) {
            val luck = itemStack.getEnchantmentLevel(Enchantment.LUCK)
            if (oreReplaceableBlock(block.type)) {
                var doSpawn = false
                var radius = 0
                lateinit var mat: Material

                // Redstone
                if (location.blockY <= 13 && (Math.random() * uncommon).roundToInt() <= 1 + luck) {
                    radius = (Math.random() * 3).roundToInt()
                    mat = Material.REDSTONE_ORE
                    doSpawn = true
                }
                // Diamond
                else if (location.blockY <= 13 && (Math.random() * rare).roundToInt() <= 1 + luck) {
                    radius = (Math.random() * 2).roundToInt()
                    mat = Material.DIAMOND_ORE
                    doSpawn = true
                }
                // Emerald
                else if (location.blockY <= 31 && (Math.random() * veryRare).roundToInt() <= 1 + luck) {
                    radius = (Math.random() * 2).roundToInt()
                    mat = Material.EMERALD_ORE
                    doSpawn = true
                }
                // Gold
                else if (block.biome == Biome.BADLANDS && location.blockY <= 80 && (Math.random() * common).roundToInt() <= 1 + luck) {
                    radius = (Math.random() * 4).roundToInt()
                    mat = Material.GOLD_ORE
                    doSpawn = true
                } else if (location.blockY <= 32 && (Math.random() * uncommon).roundToInt() <= 1 + luck) {
                    radius = (Math.random() * 3).roundToInt()
                    mat = Material.GOLD_ORE
                    doSpawn = true
                }
                // Lapis
                else if (location.blockY <= 32 && (Math.random() * rare).roundToInt() <= 1 + luck) {
                    radius = (Math.random() * 2).roundToInt()
                    mat = Material.LAPIS_ORE
                    doSpawn = true
                }

                if (doSpawn) {
                    val spawns = mutableSetOf<Location>()
                    for (x in location.blockX - radius..location.blockX + radius) {
                        for (y in location.blockY - radius..location.blockY + radius) {
                            for (z in location.blockZ - radius..location.blockZ + radius) {
                                val spawnLoc = Location(location.world, x.toDouble(), y.toDouble(), z.toDouble())
                                if (blockIsCovered(spawnLoc)) {
                                    spawns.add(spawnLoc)
                                }
                            }
                        }
                    }
                    if (spawns.count() > 0) {
                        Bukkit.getScheduler().callSyncMethod(plugin) {
                            for (spawnLoc in spawns) {
                                location.world!!.getBlockAt(spawnLoc).type = mat
                            }
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

    private fun blockIsCovered(location: Location): Boolean {
        return if (location.world != null) {
            val x = location.blockX
            val y = location.blockY
            val z = location.blockZ
            (location.world!!.getBlockAt(x + 1, y, z).type.isSolid &&
                    oreReplaceableBlock(location.world!!.getBlockAt(x - 1, y, z).type) &&
                    oreReplaceableBlock(location.world!!.getBlockAt(x, y + 1, z).type) &&
                    oreReplaceableBlock(location.world!!.getBlockAt(x, y - 1, z).type) &&
                    oreReplaceableBlock(location.world!!.getBlockAt(x, y, z + 1).type) &&
                    oreReplaceableBlock(location.world!!.getBlockAt(x, y, z - 1).type))
        } else {
            false
        }
    }

    private fun oreReplaceableBlock(mat: Material): Boolean {
        return (mat == Material.STONE || mat == Material.ANDESITE || mat == Material.GRANITE || mat == Material.DIORITE)
    }
}
