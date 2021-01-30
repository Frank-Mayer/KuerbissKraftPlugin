package main

import org.bukkit.Bukkit

class PlayTimeTimer {
    init {
        var manager = Bukkit.getScoreboardManager()
        var board = manager.newScoreboard
        var team = board.registerNewTeam("teamname")
    }
}