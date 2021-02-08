package main

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.logging.Level

object Logger {
    fun log(txt: String) {
        Bukkit.getLogger().log(Level.INFO, txt)
        if (Settings.oPsGetLog) {
            for (player in Bukkit.getOnlinePlayers()) {
                if (player.isOp) {
                    player.sendMessage(txt)
                }
            }
        }
    }

    fun error(txt: String) {
        Bukkit.getLogger().log(Level.SEVERE, txt)
        val msg = "${ChatColor.RED}ERROR: $txt"
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.isOp) {
                player.sendMessage(msg)
            }
        }
    }
}
