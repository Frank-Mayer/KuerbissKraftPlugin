package main

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import main.data.PlayerData
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

class PlayerDataManager : KoinComponent {
    private val playersTracer = HashMap<String, PlayerTrace>()
    private val playersData = HashMap<String, PlayerData>()
    private val scoreboard = Bukkit.getScoreboardManager()?.mainScoreboard
    private val plugin = inject<Plugin>().value
    private val storeDir = "${Settings.storePath}Players.json"
    private var loaded = false
    val today = (SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().time)).toLong()
    private var lastHash: Int = -1

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
            } else {
                Logger.error("No player data found for $id")
            }
        } catch (e: Exception) {
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
            Bukkit.getScheduler().callSyncMethod(plugin) {
                player.kickPlayer("Du wurdest aus dem Spiel ausgeschlossen!\n${reason}")
                for (p in Bukkit.getOnlinePlayers()) {
                    p.playSound(p.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 0.8F)
                }
            }
        }
        checkGameEnd()
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
    fun strikePlayer(player: Player, reason: String, amount: Int = 1) {
        val data = playersData[Lib.getPlayerIdentifier(player)]
        if (data != null) {
            data.strikes += amount
            player.sendMessage("${ChatColor.RED}Strike ${data.strikes}: $reason")
            if (data.strikes >= 3) {
                excludePlayer(player, "Zu viele Strikes")
                return
            } else {
                Bukkit.broadcastMessage(
                    "${ChatColor.AQUA}Strike ${data.strikes} für ${ChatColor.YELLOW}${player.name}${ChatColor.AQUA}: ${ChatColor.YELLOW}${
                        Lib.locationToDesplay(
                            player.location
                        )
                    }"
                )
                Bukkit.getScheduler().callSyncMethod(plugin) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 200, 1))
                }
            }
        }
    }

    /**
     * Load players data from disc
     */
    fun loadData() {
        val gson = Gson()
        Logger.log("Importing data from Disc")
        // Read string
        val fr = FileReader(storeDir)
        val json = fr.readText()
        fr.close()

        // Parse json
        val type: Type = object : TypeToken<List<PlayerData>>() {}.type
        val impArr: List<PlayerData> = gson.fromJson(json, type)
        for (el in impArr) {
            playersData[el.id] = el
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
        } else {
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

    /**
     * Check if there is only one team left
     */
    fun checkGameEnd() {
        val aliveTeams = setOf<String>()
        for (player in playersData) {
            if (player.value.alive && (player.value.strikes + (today - player.value.lastLogout - 1)) < 3) {
                aliveTeams.plus(player.value.teamName)
            }
        }
        if (aliveTeams.count() == 1) {
            Bukkit.broadcastMessage("Team ${aliveTeams.any()} hat KürbissKraft gewonnen")
        }
    }
}
