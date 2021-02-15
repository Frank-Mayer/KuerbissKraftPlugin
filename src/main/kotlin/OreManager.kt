package main

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.roundToInt

class OreManager(private val plugin: Plugin) {
    private var loaded = false
    private val storeDir = "${Settings.storePath}OreClearedChunks.json"
    private var lastHash: Int = -1
    private val keepHappy = hashMapOf<String, Int>()

    private val updatedChunks = mutableSetOf<String>()

    private val common = 60
    private val uncommon = 85
    private val rare = 165
    private val veryRare = 235

    init {
        val file = File(storeDir)
        if (file.exists()) {
            loadData()
        } else {
            val fw = FileWriter(storeDir)
            fw.write("[]")
            fw.close()
        }
        startTimer()
    }

    private fun loadData() {
        val gson = Gson()
        val fr = FileReader(storeDir)
        val json = fr.readText()
        fr.close()
        val type: Type = object : TypeToken<Set<String>>() {}.type
        val impSet: Set<String> = gson.fromJson(json, type)
        for (el in impSet) {
            updatedChunks.add(el)
        }
        loaded = true
        lastHash = json.hashCode()
    }

    fun storeData() {
        if (!loaded) {
            loadData()
            return
        }
        val gson = Gson()
        val json = gson.toJson(updatedChunks)
        val hash = json.hashCode()
        if (hash != lastHash) {
            val fw = FileWriter(storeDir)
            fw.write(json)
            fw.close()
            lastHash = hash
            Logger.log("Ore data saved")
        } else {
            Logger.log("No changes in ore data")
        }
    }

    private fun startTimer() {
        val t = Timer()
        t.schedule(timerTask {
            for (player in Bukkit.getOnlinePlayers()) {
                for (x in -1..1) {
                    for (z in -1..1) {
                        val chunk = Location(
                            player.location.world,
                            player.location.x + x * 16,
                            player.location.y,
                            player.location.z + z * 16
                        ).chunk
                        val chunkId = "${chunk.x}|${chunk.z}"
                        if (!updatedChunks.contains(chunkId)) {
                            updatedChunks.add(chunkId)
                            updateChunk(chunk)
                        }
                    }
                }
            }
        }, 1000, 2500)
    }

    fun mine(block: Block, itemInHand: ItemStack, player: String): Boolean {
        val location = block.location
        if (location.world != null) {
            val luck = if (itemInHand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
                itemInHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)
            } else {
                0
            } + getKeepHappy(player)
            if (oreReplaceableBlock(block.type)) {
                var doSpawn = false
                var radius = 0
                lateinit var mat: Material

                when ((Math.random() * 5).roundToInt()) {
                    0 -> {
                        // Redstone
                        if (location.blockY <= 13 && (Math.random() * uncommon) <= 1 + luck) {
                            radius = (Math.random() * 1.45f).roundToInt()
                            mat = Material.REDSTONE_ORE
                            doSpawn = true
                            decrementKeepHappy(player, 7)
                        }
                    }

                    1 -> {
                        // Gold
                        if (block.biome == Biome.BADLANDS && location.blockY <= 80 && (Math.random() * common) <= 1 + luck) {
                            radius = (Math.random() * 2).roundToInt()
                            mat = Material.GOLD_ORE
                            doSpawn = true
                        } else if (location.blockY <= 32 && (Math.random() * uncommon) <= 1 + luck) {
                            radius = (Math.random() * 1).roundToInt()
                            mat = Material.GOLD_ORE
                            doSpawn = true
                            decrementKeepHappy(player, 10)
                        }
                    }

                    2 -> {
                        // Diamond
                        if (location.blockY <= 13 && (Math.random() * rare) <= 1 + luck) {
                            radius = (Math.random()).roundToInt()
                            mat = Material.DIAMOND_ORE
                            doSpawn = true
                            decrementKeepHappy(player, 20)
                        }
                    }

                    3 -> {
                        // Lapis
                        if (location.blockY <= 32 && (Math.random() * rare) <= 1 + luck) {
                            radius = 0
                            mat = Material.LAPIS_ORE
                            doSpawn = true
                            decrementKeepHappy(player, 40)
                        }
                    }

                    4 -> {
                        // Emerald
                        if (location.blockY <= 31 && (Math.random() * veryRare) <= 1 + luck) {
                            radius = 0
                            mat = Material.EMERALD_ORE
                            doSpawn = true
                            decrementKeepHappy(player, 40)
                        }
                    }
                }

                if (doSpawn) {
                    val spawns = mutableSetOf<Location>()
                    for (x in location.blockX - radius..location.blockX + radius) {
                        for (y in location.blockY - radius..location.blockY + radius) {
                            for (z in location.blockZ - radius..location.blockZ + radius) {
                                val spawnLoc = Location(location.world, x.toDouble(), y.toDouble(), z.toDouble())
                                if (Math.random() >= 0.6 && blockIsCovered(spawnLoc)) {
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
                } else {
                    incrementKeepHappy(player)
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

    private fun unwantedOre(mat: Material): Boolean {
        return (mat == Material.DIAMOND_ORE || mat == Material.GOLD_ORE || mat == Material.EMERALD_ORE || mat == Material.REDSTONE_ORE || mat == Material.LAPIS_ORE)
    }

    private fun updateChunk(chunk: Chunk) {

        val spawns = mutableSetOf<Location>()

        val max = try {
            val biome = chunk.getBlock(8, 50, 8).biome
            if (biome == Biome.BADLANDS || biome == Biome.BADLANDS_PLATEAU || biome == Biome.ERODED_BADLANDS || biome == Biome.WOODED_BADLANDS_PLATEAU) {
                35
            } else {
                81
            }
        } catch (e: Exception) {
            31
        }

        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0..max) {
                    val block = chunk.getBlock(x, y, z)
                    if (unwantedOre(block.type)) {
                        spawns.add(block.location)
                    }
                }
            }
        }

        val count = spawns.count()
        if (count > 0) {
            Bukkit.getScheduler().callSyncMethod(plugin) {
                for (spawnLoc in spawns) {
                    chunk.world.getBlockAt(spawnLoc).type = Material.STONE
                }
            }
            Logger.log("Removed $count unwanted ores")
        }
    }

    private fun getKeepHappy(player: String): Int {
        return if (keepHappy.containsKey(player)) {
            keepHappy[player]!!
        } else {
            0
        }
    }

    private fun decrementKeepHappy(player: String, amount: Int = 5) {
        if (keepHappy.containsKey(player)) {
            keepHappy[player] = (keepHappy[player]!! - amount).coerceAtLeast(0)
        } else {
            keepHappy[player] = 0
        }
    }

    private fun incrementKeepHappy(player: String) {
        if (keepHappy.containsKey(player)) {
            keepHappy[player] = (keepHappy[player]!! + 1).coerceAtMost(veryRare - 20)
        } else {
            keepHappy[player] = 1
        }
    }
}
