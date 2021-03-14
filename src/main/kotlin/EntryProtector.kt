package main

import com.google.common.collect.Sets.newHashSet
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EntryProtector {
    private val dangerousBlocks = newHashSet(
        Material.LAVA,
        Material.FIRE,
    )

    fun login(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 16, true, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 32, true, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 200, 200, true, false, false))
    }

    fun protect(location: Location) {
        for (y in 0 until 3) {
            val loc = Location(location.world, location.x, location.y + y, location.z)
            if (loc.block.type.isSolid) {
                Logger.log("Removed not enterable Block ${loc.block.type}")
                loc.block.type = Material.AIR
            }
        }
        for (x in -2 until 3) {
            for (y in 0 until 3) {
                for (z in -2 until 3) {
                    val loc = Location(location.world, location.x + x, location.y + y, location.z + z)
                    if (dangerousBlocks.contains(loc.block.type)) {
                        Logger.log("Removed dangerous Block ${loc.block.type}")
                        loc.block.type = Material.AIR
                    }
                }
            }
        }
    }
}
