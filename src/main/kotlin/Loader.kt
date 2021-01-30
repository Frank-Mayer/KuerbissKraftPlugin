package main

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.concurrent.timerTask

class Loader : JavaPlugin(), Listener, CommandExecutor {

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getScheduler().runTaskTimer(this, runTimer(), 20 * 3, 20 * 10)
    }

    @EventHandler
    fun joinEvent(event: PlayerJoinEvent) {
        event.player.sendMessage("Welcome!")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player && sender.isOp) {
            when (command.name) {
                "varo" -> {
                    if (args.isEmpty()) {
                        sender.sendMessage("?\nstart: start the varo")
                    } else {
                        when (args[0]) {
                            "start" -> {
                                // Set default gamerules
                                sender.performCommand("gamerule logAdminCommands false")
                                sender.performCommand("gamerule showDeathMessages true")
                                sender.performCommand("gamerule announceAdvancements false")

                                // reset all players
                                var players = Bukkit.getOnlinePlayers()
                                for (player in players) {
                                    // Prevent breaking Blocks
                                    player.gameMode = GameMode.ADVENTURE
                                    player.health = player.maxHealth
                                    player.inventory.clear()
                                }

                                var t = Timer()
                                var i = 30
                                Bukkit.broadcastMessage("Noch 30 Sekunden!")
                                t.schedule(timerTask {
                                    Bukkit.broadcastMessage(i.toString())
                                    i--
                                    if (i <= 0) {
                                        for (player in players) {
                                            player.gameMode = GameMode.SURVIVAL
                                            player.health = player.maxHealth
                                            player.inventory.clear()
                                        }
                                        Bukkit.broadcastMessage("Mögen die Spiele beginnen!")
                                        t.cancel()
                                    }
                                }, 1000, 1000)
                            }
                        }
                    }
                }
            }
        }
        return true
    }


    fun runTimer(): Runnable {
        var r: Runnable = Runnable {
//            Bukkit.broadcastMessage("Tick")
        }
        return r
    }

    fun setDay(): Runnable {
        var r: Runnable = Runnable {
            val worlds = Bukkit.getWorlds()
            for (world in worlds) {
                if (world.time > 13000) {
                    world.time = 0
                }
            }
        }
        return r
    }
}