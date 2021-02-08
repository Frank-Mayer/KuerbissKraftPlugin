package main

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.Bukkit
import org.bukkit.Location
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type

class EntityDataManager(private val playerDataManager: PlayerDataManager) {
    private val storeDir = "${Settings.storePath}Entities.json"
    private var loaded = false
    private val chests = hashMapOf<String, String>()
    private var lastHash: Int = -1

    init {
        val path = File(Settings.storePath)
        if (!path.exists()) {
            File(Settings.storePath).mkdirs()
        }

        val file = File(storeDir)
        if (!file.exists()) {
            file.writeText("{}")
        }
    }

    fun loadData() {
        val gson = Gson()
        val fr = FileReader(storeDir)
        val json = fr.readText()
        fr.close()
        val type: Type = object : TypeToken<Map<String, String>>() {}.type
        val impMap: Map<String, String> = gson.fromJson(json, type)
        for (el in impMap) {
            chests[el.key] = el.value
        }
        loaded = true
        lastHash = json.hashCode()
    }

    fun storeData() {
        if (!loaded) {
            loadData()
        }
        val gson = Gson()
        val json = gson.toJson(chests)
        val hash = json.hashCode()
        if (hash != lastHash) {
            val fw = FileWriter(storeDir)
            fw.write(json)
            fw.close()
            lastHash = hash
            Logger.log("Entity data saved")
        } else {
            Logger.log("No changes in Entity data")
        }
    }

    fun addChest(location: Location, team: String) {
        chests[Lib.locationToDesplay(location)] = team
    }

    fun removeChest(location: Location, team: String): Boolean {
        if (ownChest(location, team)) {
            chests.remove(Lib.locationToDesplay(location))
            return true
        }
        return false
    }

    fun ownChest(location: Location, team: String): Boolean {
        val key = Lib.locationToDesplay(location)
        val value = chests[key]
        if (value != null) {
            if (value == team) {
                return true
            } else {
                for (player in Bukkit.getOnlinePlayers()) {
                    if (playerDataManager.getPlayerTeam(Lib.getPlayerIdentifier(player)) == team) {
                        return false
                    }
                }
            }
        }
        return true
    }
}
