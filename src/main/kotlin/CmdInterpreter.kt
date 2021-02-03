package main

import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.concurrent.timerTask

class CmdInterpreter(private val playerDataManager: PlayerDataManager, private val plugin: Plugin) {

    fun command(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (sender is Player && !sender.isOp) {
            return false
        }
        when (args[0]) {
            "load" -> {
                playerDataManager.loadData()
            }

            "kickall" -> {
                // Set message
                var msg = "Ein Admin hat alle vom Server gekickt\nMuss wohl dringend sein ;)"
                Bukkit.savePlayers()
                // Kick all players
                val players = Bukkit.getOnlinePlayers()
                for (player in players) {
                    player.kickPlayer(msg)
                }
                playerDataManager.storeData()
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

            "op" -> {
                Settings.onlyOp = args.count() == 1 || args[1] != "false"
                if (Settings.onlyOp) {
                    for (player in Bukkit.getOnlinePlayers()) {
                        if (player.isOp) {
                            player.allowFlight = true
                        } else {
                            player.allowFlight = false
                            player.kickPlayer("Der Server ist vorübergehend nur für Admins zugänglich")
                        }
                    }
                }
            }

            "start" -> {
                // Set default world settings
                for (world in Bukkit.getWorlds()) {
                    world.setGameRuleValue("logAdminCommands", "false")
                    world.setGameRuleValue("showDeathMessages", "true")
                    world.setGameRuleValue("announceAdvancements", "false")
                    world.difficulty = Difficulty.HARD
                    world.fullTime = 0
                }

                // reset all players
                val players = Bukkit.getOnlinePlayers()
                for (player in players) {
                    // Prevent breaking Blocks
                    player.gameMode = GameMode.ADVENTURE
                    player.inventory.clear()
                    player.inventory.chestplate = ItemStack(Material.ELYTRA, 1)
                }

                val t = Timer()
                var i = 30
                t.schedule(timerTask {
                    // Should show remaining time?
                    if (i % 10 == 0 || (i < 20 && i % 5 == 0) || i < 5) {
                        Bukkit.broadcastMessage("${ChatColor.AQUA}Noch $i Sekunden!")
                        for (player in players) {
                            player.playSound(player.location, Sound.ENTITY_BLAZE_HURT, 0.25F, 1.0F)
                        }
                    }

                    for (player in players) {
                        Bukkit.getScheduler().callSyncMethod(plugin) {
                            player.teleport(
                                Location(
                                    player.location.world,
                                    0.0,
                                    200.0,
                                    0.0,
                                    player.location.yaw,
                                    player.location.pitch
                                )
                            )
                        }
                    }

                    // Timer ended
                    if (i <= 0) {
                        for (player in players) {
                            player.playSound(player.location, Sound.ENTITY_BLAZE_HURT, 0.75F, 1.0F)
                            Bukkit.getScheduler().callSyncMethod(plugin) {
                                player.closeInventory()
                                player.gameMode = GameMode.SURVIVAL
                                player.health = player.maxHealth
                                player.foodLevel = 20
                                playerDataManager.resetPlayerData(null)
                                player.allowFlight = false
                            }
                        }
                        Bukkit.broadcastMessage("${ChatColor.GREEN}Mögen die Spiele beginnen!")
                        t.cancel()
                    }

                    i--
                }, 1000, 1000)
                return true
            }
        }
        sender.sendMessage("?\nload: load data from disc\nkickall: kick all players from server\nstrike <player> <reason>: give a playe a strike\nstart: start the varo")
        return false
    }
}
