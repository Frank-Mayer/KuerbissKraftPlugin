package main

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object Lib {
    fun getPlayerIdentifier(player: Player): String {
        return player.name
    }

    fun getPlayerIdentifier(player: OfflinePlayer): String {
        return player.name
    }

    fun playerCheckIn(player: Player, dataManager: PlayerDataManager): Boolean {
        if (!Settings.open && !player.isOp) {
            player.kickPlayer("Der Server ist vorübergehend nur für Admins zugänglich")
            return false
        }
        val data = dataManager.getPlayerData(getPlayerIdentifier(player))
        if (data != null) {
            if (data.lastLogout == dataManager.today && data.dayPlayTime >= Settings.maxPlayTime - 2) {
                player.kickPlayer("Deine Tageszeit ist aufgebraucht")
                return false
            }
            player.setResourcePack("https://kuerbisskraft.web.app/textures/${data.textures}.zip")
            return true
        }
        player.kickPlayer("Keine Spielerdaten verfügbar")
        return false
    }

    fun getAllPlayers(): List<OfflinePlayer> {
        return Bukkit.getOnlinePlayers().plus(Bukkit.getOfflinePlayers())
    }

    fun getPlayerByName(name: String): Player? {
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.name == name) {
                return player
            }
        }
        return null
    }

    fun locationToDesplay(location: Location): String {
        return "${location.blockX}/${location.blockY}/${location.blockZ}"
    }
}
