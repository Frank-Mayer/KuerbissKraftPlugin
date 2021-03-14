package main

object Settings {
    const val oPsGetLog: Boolean = false
    const val storePath = "plugins/KuerbissKraft/"

    /**
     * Maximum play time per day in seconds
     */
    const val maxPlayTime: Int = 1800

    var open = true

    var quitNotAllowed = false

    var pause = false

    var lobbyMode = false
}
