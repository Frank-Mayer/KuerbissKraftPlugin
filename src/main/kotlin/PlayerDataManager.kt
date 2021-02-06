package main

import com.google.gson.Gson
import main.data.PlayerData
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Team
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

class PlayerDataManager : KoinComponent {
    private val playersTracer = HashMap<String, PlayerTrace>()
    private val playersData = HashMap<String, PlayerData>()
    private val scoreboard = Bukkit.getScoreboardManager()?.mainScoreboard
    private val storePath = "plugins/KuerbissKraft/"
    private val storeDir = "${storePath}Players.json"
    private var loaded = false
    val today = (SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().time)).toLong()

    init {
        val path = File(storePath)
        if (!path.exists()) {
            File(storePath).mkdirs()
        }

        val file = File(storeDir)
        if (!file.exists()) {
            file.writeText("[]")
        }
    }

    /**
     * Update the data of a player that has to be stored
     */
    fun updatePlayerData(id: String, newData: PlayerData) {
        playersData[id] = newData
    }

    /**
     * Find data with player id
     */
    fun getPlayerData(id: String): PlayerData? {
        return playersData[id]
    }

    /**
     * Add player tracing
     */
    fun addPlayer(player: Player) {
        Logger.log("Adding Player")
        try {
            if (!loaded) {
                loadData()
            }
            val id = Lib.getPlayerIdentifier(player)
            Logger.log("initializing player trace $id")
            if (playersData.containsKey(id)) {
                playersTracer[id] = PlayerTrace(player, playersData[id]!!)
            }
            else {
                Logger.error("No player data found for $id")
            }
        }
        catch(e: Exception) {
            Logger.error(e.localizedMessage)
        }
    }

    /**
     * Remove player from being traced by server
     */
    fun removePlayer(player: Player) {
        playersTracer.remove(Lib.getPlayerIdentifier(player))
        Logger.log("Stopped tracing Player ${Lib.getPlayerIdentifier(player)}")
    }


    /**
     * Exclude a player from the game
     */
    fun excludePlayer(player: Player, reason: String) {
        Bukkit.getBanList(BanList.Type.NAME).addBan(player.name, reason, null, null)
        if (player.isOnline) {
            player.kickPlayer(reason)
            for (p in Bukkit.getOnlinePlayers()) {
                p.playSound(p.location, Sound.ENTITY_LIGHTNING_THUNDER, 1.0F, 0.9F)
            }
        }
    }

    /**
     * Reset data of a player or all players
     */
    fun resetPlayerData(playerId: String?) {
        if (playerId != null) {
            val data = playersData[playerId]
            if (data != null) {
                data.Strikes = 0
                data.LastLogout = -1
                data.DayPlaytime = 0
            }
        } else {
            for (p in Lib.getAllPlayers()) {
                resetPlayerData(Lib.getPlayerIdentifier(p))
            }
        }
    }

    /**
     * Give a player a Strike
     */
    fun strikePlayer(player: Player, reason: String) {
        val data = playersData[Lib.getPlayerIdentifier(player)]
        if (data != null) {
            data.Strikes++
            player.sendMessage("Strike ${data.Strikes}: $reason")
            if (data.Strikes >= 3) {
                excludePlayer(player, "Zu viele Strikes")
                return
            }
            else {
                Bukkit.broadcastMessage("${ChatColor.RED}Strike ${data.Strikes} für ${player.name}: ${player.location.x} / ${player.location.y} / ${player.location.z}")
            }
        }
    }

    /**
     * Load players data from disc
     */
    fun loadData() {
        Logger.log("Importing data from Disc")
        // Read string
        val fr = FileReader(storeDir)
        val json = fr.readText()
        fr.close()
        // Parse Json
        val parser = JSONParser()
        val players = parser.parse(json) as JSONArray
        // Loop Json-Array
        for (playerDta in players) {
            val player = playerDta as JSONObject
            val playerId = player["Id"] as String
            // Player data existent?
            if (playersData.containsKey(playerId)) {
                val p = playersData[playerId]
                if (p != null) {
                    p.Id = playerId
                    p.TeamName = player["TeamName"] as String
                    p.Strikes = player["Strikes"] as Long
                    p.LastLogout = player["LastLogout"] as Long
                    p.DayPlaytime = player["DayPlaytime"] as Long
                }
            } else {
                val p = PlayerData()
                p.Id = playerId
                p.TeamName = player["TeamName"] as String
                p.Strikes = player["Strikes"] as Long
                p.LastLogout = player["LastLogout"] as Long
                p.DayPlaytime = player["DayPlaytime"] as Long
                playersData[playerId] = p
            }
        }
        Logger.log("Data imported")
        loaded = true
    }

    /**
     * Save players data to disc
     */
    fun storeData() {
        if (!loaded) {
            loadData()
        }
        Logger.log("${ChatColor.GREEN}Storing data")
        val gson = Gson()
        val jsonSB = StringBuilder()
        jsonSB.append("[")
        var notFirst = false
        for (playerDat in playersData) {
            if (notFirst) {
                jsonSB.append(",")
            }
            notFirst = true
            jsonSB.append(gson.toJson(playerDat.value))
        }
        jsonSB.append("]")
        val json = jsonSB.toString()
        Logger.log(json)
        val fw = FileWriter(storeDir)
        fw.write(json)
        fw.close()
        Logger.log("Match data saved")
    }

    fun addPlayerToTeam(player: String, team: String) {
        if (scoreboard != null) {
            Logger.log("adding $player to $team")
            var t = scoreboard.getTeam(team)
            if (t == null) {
                t = scoreboard.registerNewTeam(team)
                t.prefix = "${ChatColor.AQUA}[$team] "
                t.addEntry(player)
            } else if (!t.hasEntry(player)) {
                t.addEntry(player)
            }
            t.setAllowFriendlyFire(false)
            t.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS)
            Logger.log("added $player to $team")
        } else {
            Logger.error("scoreboard is null")
        }
    }
}
