package main

import main.data.PlayerData
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.Plugin
import org.koin.core.context.GlobalContext.get
import java.util.*
import kotlin.concurrent.timerTask

class PlayerTrace(
    player: org.bukkit.entity.Player,
    private val playerData: PlayerData
) {
    private val controlledPlayer: org.bukkit.entity.Player = player
    private val playerDataManager: PlayerDataManager = get().get()
    private val plugin: Plugin = get().get()

    init {
        Logger.log("Tracing player ${controlledPlayer.name}")
        playerDataManager.addPlayerToTeam(controlledPlayer.name, playerData.teamName)
        if (playerData.lastLogout == playerDataManager.today) {
            if (playerData.dayPlayTime >= Settings.maxPlayTime) {
                timeEnd()
            } else {
                playerData.dayPlayTime = 0
                playerData.lastLogout = playerDataManager.today;
                startTimer()
            }
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        val t = Timer()
        t.schedule(timerTask {
            if (controlledPlayer.isOnline) {
                playerData.dayPlayTime++
                playerData.id = Lib.getPlayerIdentifier(controlledPlayer)
                playerData.lastLogout = playerDataManager.today
                playerDataManager.updatePlayerData(Lib.getPlayerIdentifier(controlledPlayer), playerData)
                val timeLeft = (Settings.maxPlayTime - playerData.dayPlayTime).toInt()
                if (playerData.dayPlayTime >= Settings.maxPlayTime) {
                    timeEnd()
                    t.cancel()
                } else if (timeLeft == 60 || timeLeft == 30 || timeLeft == 15 || timeLeft <= 10) {
                    Bukkit.broadcastMessage(
                        "${ChatColor.YELLOW}${controlledPlayer.name}${ChatColor.AQUA} wird in ${ChatColor.YELLOW}$timeLeft${ChatColor.AQUA} Sekunden gekickt"
                    )
                }
            } else {
                t.cancel()
            }
        }, 1000, 1000)
    }

    /**
     * The daily playtime has expired
     */
    private fun timeEnd() {
        playerDataManager.storeData()
        val delay = Timer()
        delay.schedule(timerTask {
            Bukkit.getScheduler().callSyncMethod(plugin) {
                controlledPlayer.kickPlayer("Deine Tageszeit ist aufgebraucht")
            }
        }, 1000)
    }
}
