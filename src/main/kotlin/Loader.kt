package main

import org.bukkit.BanList
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityCreatePortalEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.*
import kotlin.concurrent.timerTask

class Loader : JavaPlugin(), Listener, CommandExecutor, KoinComponent {
    private lateinit var playerDataManager: PlayerDataManager
    private lateinit var cmdInterpreter: CmdInterpreter

    override fun onEnable() {
        registerModules()
        playerDataManager = inject<PlayerDataManager>().value
        cmdInterpreter = inject<CmdInterpreter>().value
        playerDataManager.loadData()
        Bukkit.getPluginManager().registerEvents(this, this)
        val tick = Timer()
        tick.schedule(timerTask {
            onTick()
        }, 1000, 4000)
    }

    @EventHandler
    fun joinEvent(event: PlayerJoinEvent) {
        if (Lib.playerCheckIn(event.player, playerDataManager)) {
            playerDataManager.addPlayer(event.player)
        }
    }

    @EventHandler
    fun quitEvent(event: PlayerQuitEvent) {
        playerDataManager.removePlayer(event.player)
        if (Settings.quitNotAllowed) {
            playerDataManager.strikePlayer(event.player, "Du hast den Server während der Startphase verlassen")
        }
    }

    @EventHandler
    fun playerDeathEvent(event: PlayerDeathEvent) {
        if (event.entity is Player) {
            playerDataManager.excludePlayer(event.entity, event.deathMessage)
        }
    }

    @EventHandler
    fun onResourcePackStatus(event: PlayerResourcePackStatusEvent) {
        if (event.status == PlayerResourcePackStatusEvent.Status.DECLINED) {
            event.player.kickPlayer("Bitte nutze das Resource Pack des Servers!")
        }
    }

    @EventHandler
    fun onEntityCreatePortal(event: EntityCreatePortalEvent) {
        Bukkit.broadcastMessage("${ChatColor.LIGHT_PURPLE}${event.entity.name} hat ein Portal erstellt")
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        event.player.kickPlayer("Durch deinen Tod bist du aus dem Spiel ausgeschlossen")
    }

    @EventHandler
    fun onPlayerAchievementAwarded(event: PlayerAchievementAwardedEvent) {
        Bukkit.broadcastMessage("Ein Spieler hat diesen Erfolg erzielt: ${event.achievement.name}")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name == "varo") {
            return cmdInterpreter.command(sender, args)
        }
        return false
    }

    private fun onTick() {
        playerDataManager.storeData()
    }

    private fun registerModules() {
        val plugin = this

        val koinModules = module {
            single<Plugin> { plugin }
            single { PlayerDataManager() }
            single { CmdInterpreter(get(), get()) }
        }

        startKoin {
            modules(koinModules)
        }
    }
}
