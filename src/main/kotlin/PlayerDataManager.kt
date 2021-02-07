package main

import com.google.gson.Gson
import main.data.PlayerData
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.koin.core.component.KoinComponent
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
    private val storeDir = "${Settings.storePath}Players.json"
    private var loaded = false
    val today = (SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().time)).toLong()
    private var lastHash:Int = -1

    init {
        val path = File(Settings.storePath)
        if (!path.exists()) {
            File(Settings.storePath).mkdirs()
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
    fun getPlayerData(playerId: String): PlayerData? {
        return playersData[playerId]
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
//        Bukkit.getBanList(BanList.Type.NAME).addBan(player.name, reason, null, null)
        getPlayerData(Lib.getPlayerIdentifier(player))?.alive = false
        Bukkit.broadcastMessage("${ChatColor.YELLOW}${player.name}${ChatColor.AQUA} ist aus dem Spiel ausgeschieden")
        if (player.isOnline) {
            player.kickPlayer(reason)
            for (p in Bukkit.getOnlinePlayers()) {
                p.playSound(p.location, Sound.ENTITY_LIGHTNING_THUNDER, 1.0F, 0.8F)
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
                data.strikes = 0
                data.lastLogout = -1
                data.dayPlayTime = 0
                data.alive = true
            }
        } else {
            for (p in Lib.getAllPlayers()) {
                resetPlayerData(Lib.getPlayerIdentifier(p))
            }
        }
    }

    fun getPlayerTeam(playerId: String): String {
        val data = playersData[playerId]
        if (data != null) {
            return data.teamName
        }
        return ""
    }

    /**
     * Give a player a Strike
     */
    fun strikePlayer(player: Player, reason: String) {
        val data = playersData[Lib.getPlayerIdentifier(player)]
        if (data != null) {
            data.strikes++
            player.sendMessage("${ChatColor.RED}Strike ${data.strikes}: $reason")
            if (data.strikes >= 3) {
                excludePlayer(player, "Zu viele Strikes")
                return
            }
            else {
                Bukkit.broadcastMessage(
                    "${ChatColor.AQUA}Strike ${data.strikes} für ${ChatColor.YELLOW}${player.name}${ChatColor.AQUA}: ${ChatColor.YELLOW}${Lib.locationToDesplay(player.location)}"
                )
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
            val playerId = player["id"] as String
            // Player data existent?
            if (playersData.containsKey(playerId)) {
                val p = playersData[playerId]
                if (p != null) {
                    p.id = playerId
                    p.teamName = player["teamName"] as String
                    p.strikes = player["strikes"] as Long
                    p.lastLogout = player["lastLogout"] as Long
                    p.dayPlayTime = player["dayPlayTime"] as Long
                    p.textures = player["textures"] as String
                    p.alive = player["alive"] as Boolean
                }
            } else {
                val p = PlayerData()
                p.id = playerId
                p.teamName = player["teamName"] as String
                p.strikes = player["strikes"] as Long
                p.lastLogout = player["lastLogout"] as Long
                p.dayPlayTime = player["dayPlayTime"] as Long
                p.textures = player["textures"] as String
                p.alive = player["alive"] as Boolean
                playersData[playerId] = p
            }
        }
        Logger.log("Data imported")
        loaded = true
        lastHash = json.hashCode()
    }

    /**
     * Save players data to disc
     */
    fun storeData() {
        if (!loaded) {
            loadData()
        }
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
        val hash = json.hashCode()
        if (hash != lastHash) {
            val fw = FileWriter(storeDir)
            fw.write(json)
            fw.close()
            lastHash = hash
            Logger.log("Player data saved")
        }
        else {
            Logger.log("No changes in Player data")
        }
    }

    fun addPlayerToTeam(player: String, team: String) {
        if (scoreboard != null) {
            Logger.log("adding $player to $team")
            var t = scoreboard.getTeam(team)
            if (t == null) {
                t = scoreboard.registerNewTeam(team)
                t.prefix = "[$team] "
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

    fun loginProtection(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 3))
        player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 5))
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 200, 5))
    }
}
