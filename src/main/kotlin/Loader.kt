package main

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.world.PortalCreateEvent
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
    private lateinit var entityDataManager: EntityDataManager
    private lateinit var badLanguageChecker: BadLanguageChecker

    override fun onEnable() {
        registerModules()
        playerDataManager = inject<PlayerDataManager>().value
        entityDataManager = inject<EntityDataManager>().value
        cmdInterpreter = inject<CmdInterpreter>().value
        badLanguageChecker = inject<BadLanguageChecker>().value
        playerDataManager.loadData()
        entityDataManager.loadData()
        Bukkit.getPluginManager().registerEvents(this, this)
        val tick = Timer()
        tick.schedule(timerTask {
            onTick()
        }, 5000, 10000)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (Lib.playerCheckIn(event.player, playerDataManager)) {
            playerDataManager.addPlayer(event.player)
            event.player.sendMessage("${ChatColor.AQUA}Nutze die /kurbiss <option> commands um aktuelle Informationen zu erhalten\nstrikes: Zeigt wie viele Strikes du hast\ntime: zeigt deine aktuelle Online-Zeit an")
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
    fun onPortalCreate(event: PortalCreateEvent) {
        if (Settings.open) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerAchievementAwarded(event: PlayerAchievementAwardedEvent) {
        Bukkit.broadcastMessage(
            "${ChatColor.AQUA}Ein Spieler hat den Erfolg ${ChatColor.YELLOW}${
                event.achievement.name.replace(
                    '_',
                    ' '
                ).toLowerCase()
            }${ChatColor.AQUA} erziehlt"
        )
    }

    @EventHandler
    fun onBlockDamage(event: BlockDamageEvent) {
        if (event.block.type == Material.CHEST || event.block.type == Material.TRAPPED_CHEST) {
            if (!entityDataManager.ownChest(
                    event.block.location,
                    playerDataManager.getPlayerTeam(Lib.getPlayerIdentifier(event.player))
                )
            ) {
                event.player.sendMessage("${ChatColor.RED}Das ist nicht deine Kiste!")
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.block.type == Material.CHEST || event.block.type == Material.TRAPPED_CHEST) {
            if (!entityDataManager.removeChest(
                    event.block.location,
                    playerDataManager.getPlayerTeam(Lib.getPlayerIdentifier(event.player))
                )
            ) {
                playerDataManager.strikePlayer(event.player, "Du hast eine fremde Kiste zerstört")
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.block.type == Material.CHEST || event.block.type == Material.TRAPPED_CHEST) {
            entityDataManager.addChest(
                event.block.location,
                playerDataManager.getPlayerTeam(Lib.getPlayerIdentifier(event.player))
            )
        }
    }

    @EventHandler
    fun onInventoryOpen(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK && (event.clickedBlock.type == Material.CHEST || event.clickedBlock.type == Material.TRAPPED_CHEST)) {
            if (!entityDataManager.ownChest(
                    event.clickedBlock.location,
                    playerDataManager.getPlayerTeam(Lib.getPlayerIdentifier(event.player))
                )
            ) {
                event.player.closeInventory()
                playerDataManager.strikePlayer(event.player, "Du hast eine fremde Kiste geöffnet")
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        if (badLanguageChecker.filter(event.message)) {
            event.message = ""
            playerDataManager.strikePlayer(event.player, "Deine Chat Nachricht ist möglicherweise beleidigend")
            event.isCancelled = true
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name == "kürbiss" || command.name == "kurbiss" || command.name == "kuerbiss" || command.name == "kürbis" || command.name == "kurbis" || command.name == "kuerbis") {
            return cmdInterpreter.command(sender, args)
        }
        return false
    }

    private fun onTick() {
        playerDataManager.storeData()
        entityDataManager.storeData()
    }

    private fun registerModules() {
        val plugin = this

        val koinModules = module {
            single<Plugin> { plugin }
            single { PlayerDataManager() }
            single { CmdInterpreter(get(), get()) }
            single { EntityDataManager(get()) }
            single { BadLanguageChecker() }
        }

        startKoin {
            modules(koinModules)
        }
    }
}
