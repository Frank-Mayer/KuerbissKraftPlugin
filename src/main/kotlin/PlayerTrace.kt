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
    data: PlayerData
) {
    private val controlledPlayer: org.bukkit.entity.Player = player
    private var todayPlaytime: Long = 0L
    private var strikes: Long = 0
    private val playerData = data
    private var teamName = playerData.TeamName
    private val playerDataManager: PlayerDataManager = get().get()
    private val plugin: Plugin = get().get()

    init {
        if (playerDataManager.today == playerData.LastLogout) {
            todayPlaytime = playerData.DayPlaytime
        }
        if (todayPlaytime >= Settings.maxPlayTime) {
            timeEnd()
        }
        playerDataManager.addPlayerToTeam(controlledPlayer.name, teamName)
        startTimer()
    }

    private fun startTimer() {
        val t = Timer()
        t.schedule(timerTask {
            if (controlledPlayer.isOnline) {
                todayPlaytime++
                playerData.Id = Lib.getPlayerIdentifier(controlledPlayer)
                playerData.Strikes = strikes
                playerData.DayPlaytime = todayPlaytime
                playerData.LastLogout = playerDataManager.today
                playerDataManager.updatePlayerData(Lib.getPlayerIdentifier(controlledPlayer), playerData)
                val timeLeft = (Settings.maxPlayTime - todayPlaytime).toInt()
                if (todayPlaytime >= Settings.maxPlayTime) {
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
