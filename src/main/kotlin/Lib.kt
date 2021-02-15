package main

import org.bukkit.*
import org.bukkit.entity.Player

object Lib {
    fun getPlayerIdentifier(player: Player): String {
        return player.name
    }

    fun getPlayerIdentifier(player: OfflinePlayer): String {
        return if (player.name != null) {
            player.name!!
        } else {
            player.uniqueId.toString()
        }
    }

    fun playerCheckIn(player: Player, dataManager: PlayerDataManager): Boolean {
        if (!Settings.open) {
            if (!player.isOp) {
                player.kickPlayer("Der Server ist vorübergehend nur für Admins zugänglich")
            }
            return false
        }
        val data = dataManager.getPlayerData(getPlayerIdentifier(player))
        if (data != null) {
            if (data.lastLogout > 1) {
                val dif = (dataManager.today - data.lastLogout) - 1
                when {
                    dif == 1L -> {
                        dataManager.strikePlayer(player, "Du warst einen Tag nicht online")
                    }
                    dif == 2L -> {
                        dataManager.strikePlayer(player, "Du warst zwei Tage nicht online", 2)
                    }
                    dif >= 3L -> {
                        dataManager.strikePlayer(player, "Du warst viel zu lange nicht online", 3)
                    }
                }
            }
            if (data.lastLogout == dataManager.today && data.dayPlayTime >= Settings.maxPlayTime - 2) {
                if (player.isOp) {
                    player.gameMode = GameMode.SPECTATOR
                } else {
                    player.kickPlayer("Deine Tageszeit ist aufgebraucht")
                }
                return false
            } else if (!data.alive) {
                if (player.isOp) {
                    player.gameMode = GameMode.SPECTATOR
                } else {
                    player.kickPlayer("Durch deinen Tod bist du aus dem Spiel ausgeschlossen")
                }
                return false
            }
//            player.setResourcePack("https://kuerbisskraft.web.app/textures/${data.textures}.zip")
            return true
        }
        player.kickPlayer("Keine Spielerdaten verfügbar, bitte wende Dich an den Veranstalter")
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

    fun isPortalNearby(location: Location): Boolean {
        if (location.world != null) {
            val radius = 8
            for (x in location.blockX - radius..location.blockX + radius) {
                for (y in location.blockY - radius..location.blockY + radius) {
                    for (z in location.blockZ - radius..location.blockZ + radius) {
                        if (location.world!!.getBlockAt(
                                Location(
                                    location.world,
                                    x.toDouble(),
                                    y.toDouble(),
                                    z.toDouble()
                                )
                            ).type == Material.NETHER_PORTAL
                        ) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
}
