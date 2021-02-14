package main

import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.max
import kotlin.math.min

class CmdInterpreter(private val playerDataManager: PlayerDataManager, private val plugin: Plugin) {
    fun command(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.count() >= 1) {
            if (sender is Player) {
                when (args[0]) {
                    "strikes" -> {
                        val data = playerDataManager.getPlayerData(Lib.getPlayerIdentifier(sender))
                        if (data != null) {
                            sender.sendMessage("${ChatColor.AQUA}Du hast ${ChatColor.YELLOW}${data.strikes}${ChatColor.AQUA} Strikes")
                        } else {
                            sender.sendMessage("${ChatColor.RED}Fehler, keine Daten gefunden")
                        }
                        return true
                    }

                    "time" -> {
                        val data = playerDataManager.getPlayerData(Lib.getPlayerIdentifier(sender))
                        if (data != null) {
                            sender.sendMessage("${ChatColor.AQUA}Deine heutige Online-Zeit: ${ChatColor.YELLOW}${data.dayPlayTime / 60}:${data.dayPlayTime % 60}")
                        } else {
                            sender.sendMessage("${ChatColor.RED}Fehler, keine Daten gefunden")
                        }
                        return true
                    }
                }
                if (!sender.isOp) {
                    sender.sendMessage("${ChatColor.AQUA}?\nstrikes: Sehe nach wie viele Strikes du hast")
                    return false
                }
            }
            if (sender !is Player || sender.isOp) {
                when (args[0]) {
                    "load" -> {
                        playerDataManager.loadData()
                    }

                    "kickall" -> {
                        playerDataManager.storeData()
                        Bukkit.savePlayers()
                        // Kick all players
                        val players = Bukkit.getOnlinePlayers()
                        Bukkit.getScheduler().callSyncMethod(plugin) {
                            for (player in players) {
                                player.kickPlayer("Ein Admin hat alle vom Server gekickt\nMuss wohl dringend sein ;)")
                            }
                        }
                        Timer().schedule(timerTask {
                            Bukkit.shutdown()
                        }, 3000)
                        return true
                    }

                    "strike" -> {
                        if (args.count() >= 3) {
                            val player = Lib.getPlayerByName(args[1])
                            if (player != null) {
                                playerDataManager.strikePlayer(player, args[2])
                                return true
                            }
                        }
                    }

                    "open" -> {
                        Settings.open = true
                        return true
                    }

                    "close" -> {
                        Settings.open = false
                        Bukkit.getScheduler().callSyncMethod(plugin) {
                            for (player in Bukkit.getOnlinePlayers()) {
                                if (!player.isOp) {
                                    player.kickPlayer("Der Server ist vorübergehend nur für Admins zugänglich")
                                }
                            }
                        }
                        return true
                    }

                    "reset" -> {
                        if (args.count() >= 2) {
                            playerDataManager.resetPlayerData(args[1])
                        } else {
                            playerDataManager.resetPlayerData(null)
                        }
                        return true
                    }

                    "portal" -> {
                        with(Bukkit.getWorld("world")) {
                            if (this != null) {
                                val spawnLoc = intArrayOf(
                                    spawnLocation.blockX,
                                    getHighestBlockYAt(spawnLocation.blockX, spawnLocation.blockZ),
                                    spawnLocation.blockZ
                                )
                                for (x in 0 until 4) {
                                    for (y in 0 until 5) {
                                        getBlockAt(spawnLoc[0] + x, spawnLoc[1] + y, spawnLoc[2]).type =
                                            Material.OBSIDIAN
                                    }
                                }
                                for (x in 1 until 3) {
                                    for (y in 1 until 4) {
                                        getBlockAt(spawnLoc[0] + x, spawnLoc[1] + y, spawnLoc[2]).type =
                                            Material.AIR
                                    }
                                }
                            } else {
                                Logger.error("Could not build portal, world is null")
                            }
                        }
                        return true
                    }

                    "start" -> {
                        Bukkit.broadcastMessage("${ChatColor.AQUA}Das Event hat begonnen! Der Server darf ab jetzt nicht mehr verlassen werden, bis deine Tageszeit aufgebraucht ist!")
                        Settings.quitNotAllowed = true

                        var spawnLoc: IntArray
                        with(Bukkit.getWorld("world")) {
                            if (this != null) {
                                spawnLoc = intArrayOf(
                                    spawnLocation.blockX,
                                    getHighestBlockYAt(spawnLocation.blockX, spawnLocation.blockZ),
                                    spawnLocation.blockZ
                                )
                            } else {
                                spawnLoc = intArrayOf(
                                    0,
                                    200,
                                    0
                                )
                                Logger.error("Could not set start location, world is null")
                            }
                        }

                        // Set default world settings
                        GameRule.ANNOUNCE_ADVANCEMENTS
                        for (world in Bukkit.getWorlds()) {
                            world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false)
                            world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true)
                            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
                            world.setGameRule(GameRule.DO_PATROL_SPAWNING, true)
                            world.setGameRule(GameRule.KEEP_INVENTORY, true)
                            world.setGameRule(GameRule.DO_LIMITED_CRAFTING, false)
                            world.difficulty = Difficulty.EASY
                            world.fullTime = 0
                        }

                        // reset all players
                        val players = Bukkit.getOnlinePlayers()
                        Bukkit.getScheduler().callSyncMethod(plugin) {
                            for (player in players) {
                                // Prevent breaking Blocks
                                player.gameMode = GameMode.ADVENTURE
                                player.inventory.clear()
                                player.inventory.chestplate = ItemStack(Material.ELYTRA, 1)
                                player.addPotionEffect(PotionEffect(PotionEffectType.LEVITATION, 20000, 1))
                                player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_FALLING, 20000, 2))
                            }
                        }

                        var i = 35

                        val t = Timer()
                        t.schedule(timerTask {
                            // Should show remaining time?
                            if (i % 10 == 0 || (i < 20 && i % 5 == 0) || i < 5) {
                                Bukkit.broadcastMessage("${ChatColor.AQUA}Noch $i Sekunden!")
                                for (player in players) {
                                    player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 0.25F, 1.0F)
                                }
                            }

                            for (player in players) {
                                Bukkit.getScheduler().callSyncMethod(plugin) {
                                    player.teleport(
                                        Location(
                                            player.location.world,
                                            spawnLoc[0].toDouble(),
                                            min(
                                                250.0,
                                                max(spawnLoc[1].toDouble() + 100.0, 175.0)
                                            ),
                                            spawnLoc[2].toDouble(),
                                            player.location.yaw,
                                            player.location.pitch
                                        )
                                    )
                                }
                            }

                            // Timer ended
                            if (i <= 0) {
                                Bukkit.getScheduler().callSyncMethod(plugin) {
                                    for (player in players) {
                                        player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F)
                                        player.playSound(player.location, Sound.EVENT_RAID_HORN, 1.0F, 1.0F)
                                        player.closeInventory()
                                        player.gameMode = GameMode.SURVIVAL
                                        player.level = 0
                                        player.exp = 0.0f
                                        player.health = 20.0
                                        player.foodLevel = 20
                                        playerDataManager.resetPlayerData(null)
                                        player.allowFlight = false
                                        player.removePotionEffect(PotionEffectType.LEVITATION)
                                        player.removePotionEffect(PotionEffectType.SLOW_FALLING)

                                        val iterator = Bukkit.getServer().advancementIterator()
                                        while (iterator.hasNext()) {
                                            val progress = player.getAdvancementProgress(iterator.next())
                                            for (criteria in progress.awardedCriteria) {
                                                progress.revokeCriteria(criteria!!)
                                            }
                                        }
                                    }
                                }
                                Bukkit.broadcastMessage("${ChatColor.GREEN}Mögen die Spiele beginnen!")

                                // Remove Elytra
                                val e = Timer()
                                i = 15
                                e.schedule(timerTask {
                                    if (i % 10 == 0 || (i < 20 && i % 5 == 0) || i < 5) {
                                        Bukkit.broadcastMessage("${ChatColor.RED}Elytra wird in $i Sekunden entfernt!")
                                    }

                                    if (i <= 0) {
                                        for (player in Bukkit.getOnlinePlayers()) {
                                            for (j in 0 until player.inventory.size) {
                                                val stack = player.inventory.getItem(j)
                                                if (stack != null && stack.type == Material.ELYTRA) {
                                                    player.inventory.setItem(j, null)
                                                }
                                            }
                                        }
                                        e.cancel()
                                    }
                                    i--
                                }, 1000, 1000)
                                t.cancel()
                            }

                            i--
                        }, 1000, 1000)
                        return true
                    }
                }
            }
        }
        return false
    }
}
