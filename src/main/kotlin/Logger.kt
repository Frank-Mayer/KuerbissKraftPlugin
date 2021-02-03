package main

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.logging.Level

object Logger {
    fun log(txt: String) {
        Bukkit.getLogger().log(Level.ALL, txt)
        if (Settings.oPsGetLog) {
            for (player in Bukkit.getOnlinePlayers()) {
                if (player.isOp) {
                    player.sendMessage(txt)
                }
            }
        }
    }

    fun error(txt: String) {
        val msg = "${ChatColor.RED}ERROR: $txt"
        Bukkit.getLogger().log(Level.WARNING, msg)
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.isOp) {
                player.sendMessage(msg)
            }
        }
    }
}
