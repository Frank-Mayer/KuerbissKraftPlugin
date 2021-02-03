package main

import org.bukkit.Bukkit
import org.bukkit.entity.Player

object Lib {
    fun getPlayerIdentifier(player: Player): String {
        return player.name
    }

    fun playerCheckIn(player: Player, dataManager: PlayerDataManager): Boolean {
        if (Settings.onlyOp) {
            if (player.isOp) {
                player.allowFlight = true
            }
            else {
                player.allowFlight = false
                player.kickPlayer("Der Server ist vorübergehend nur für Admins zugänglich")
                return false
            }
        }
        val data = dataManager.getPlayerData(getPlayerIdentifier(player))
        if (data != null && data.LastLogout == dataManager.today && data.DayPlaytime >= Settings.maxPlayTime - 2) {
            player.kickPlayer("Deine Tageszeit ist aufgebraucht")
            return false
        }
//        player.setResourcePack("https://frank-mayer.tk/cdn/vanillaxbr-classic.zip")
        return true
    }

    fun getAllPlayers(): List<Player> {
        return Bukkit.getOnlinePlayers().plus(Bukkit.getOfflinePlayers()) as List<Player>
    }

    fun getPlayerByName(name: String): Player? {
        for (player in getAllPlayers()) {
            if (player.name == name) {
                return player
            }
        }
        return null
    }
}
