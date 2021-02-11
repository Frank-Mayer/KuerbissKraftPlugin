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
    private lateinit var entryProtector: EntryProtector

    override fun onEnable() {
        registerModules()
        playerDataManager = inject<PlayerDataManager>().value
        entityDataManager = inject<EntityDataManager>().value
        cmdInterpreter = inject<CmdInterpreter>().value
        badLanguageChecker = inject<BadLanguageChecker>().value
        entryProtector = inject<EntryProtector>().value
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
            entryProtector.protect(event.player.location)
        }
    }

    @EventHandler
    fun playerQuit(event: PlayerQuitEvent) {
        playerDataManager.removePlayer(event.player)
        if (Settings.quitNotAllowed) {
            playerDataManager.strikePlayer(event.player, "Du hast den Server während der Startphase verlassen")
        }
    }

    @EventHandler
    fun playerDeath(event: PlayerDeathEvent) {
        if (event.entity is Player) {
            event.keepInventory = true
            event.keepLevel = true
            if (event.deathMessage != null) {
                playerDataManager.excludePlayer(event.entity, event.deathMessage!!)
            } else {
                playerDataManager.excludePlayer(event.entity, "${event.entity.name} ist gestorben")
            }
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
    fun onPortal(event: PlayerPortalEvent) {
        Bukkit.broadcastMessage("${ChatColor.LIGHT_PURPLE}Portaled")
        if (event.to != null) {
            entryProtector.protect(event.to!!)
        }
        else {
            Logger.log("Could not protect ${event.player.name}, target location is null")
        }
    }

    @EventHandler
    fun onPlayerAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val key = event.advancement.key.key.replace("/root", "")
        if ("recipe" !in key) {
            Bukkit.broadcastMessage(
                "${ChatColor.AQUA}Ein Spieler hat den Erfolg ${ChatColor.YELLOW}${key.split('/').last().replace('_', ' ')}${ChatColor.AQUA} erziehlt"
            )
        }
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
        else if (event.block.type == Material.NETHER_PORTAL) {
            event.isCancelled = true
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
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock != null && (event.clickedBlock!!.type == Material.CHEST || event.clickedBlock!!.type == Material.TRAPPED_CHEST)) {
            if (!entityDataManager.ownChest(
                    event.clickedBlock!!.location,
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
            single { EntryProtector() }
        }

        startKoin {
            modules(koinModules)
        }
    }
}
