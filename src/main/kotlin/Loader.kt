package main

import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.PortalCreateEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.*
import kotlin.concurrent.timerTask


@KoinApiExtension
class Loader : JavaPlugin(), Listener, CommandExecutor, KoinComponent {
    private lateinit var playerDataManager: PlayerDataManager
    private lateinit var cmdInterpreter: CmdInterpreter
    private lateinit var entityDataManager: EntityDataManager
    private lateinit var badLanguageChecker: BadLanguageChecker
    private lateinit var entryProtector: EntryProtector
    private lateinit var translator: Translator
    private lateinit var customRecipes: CustomRecipes

    override fun onEnable() {
        registerModules()
        playerDataManager = inject<PlayerDataManager>().value
        entityDataManager = inject<EntityDataManager>().value
        cmdInterpreter = inject<CmdInterpreter>().value
        badLanguageChecker = inject<BadLanguageChecker>().value
        entryProtector = inject<EntryProtector>().value
        translator = inject<Translator>().value
        customRecipes = inject<CustomRecipes>().value
        playerDataManager.loadData()
        entityDataManager.loadData()
        Bukkit.getPluginManager().registerEvents(this, this)
        val tick = Timer()
        tick.schedule(timerTask {
            onTick()
        }, 5000, 10000)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (Settings.pause) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (Settings.pause) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (Lib.playerCheckIn(event.player, playerDataManager)) {
            playerDataManager.addPlayer(event.player)
            event.player.sendMessage("${ChatColor.AQUA}Nutze die /kurbiss <option> commands um aktuelle Informationen zu erhalten\nstrikes: Zeigt wie viele Strikes du hast\ntime: zeigt deine aktuelle Online-Zeit an")
            entryProtector.login(event.player)
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
        event.keepInventory = true
        event.keepLevel = true
        if (event.deathMessage != null) {
            playerDataManager.excludePlayer(event.entity, event.deathMessage!!)
        } else {
            playerDataManager.excludePlayer(event.entity, "${event.entity.name} ist gestorben")
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
        if (event.reason == PortalCreateEvent.CreateReason.FIRE && Settings.open) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        for (block in event.blockList()) {
            if (block.type == Material.NETHER_PORTAL || block.type == Material.CHEST || block.type == Material.TRAPPED_CHEST) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun onPlayerAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val name = translator.getAdvancementName(event.advancement.key.key)
        if (name != null) {
            Bukkit.broadcastMessage(
                "${ChatColor.AQUA}Ein Spieler hat den Erfolg ${ChatColor.YELLOW}[$name]${ChatColor.AQUA} erzielt"
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
        if (Settings.pause || (Settings.open && (event.block.type == Material.NETHER_PORTAL || Lib.isPortalNearby(event.block.location)))) {
            event.player.sendMessage("${ChatColor.RED}Du kannst hier nichts abbauen, das dient zum Schutz des Netherportals")
            event.isCancelled = true
            return
        }

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
        if (Settings.pause || (Settings.open && Lib.isPortalNearby(event.block.location))) {
            event.player.sendMessage("${ChatColor.RED}Du kannst hier nichts setzen, das dient zum Schutz des Netherportals")
            event.isCancelled = true
            return
        }

        if (event.block.type == Material.CHEST || event.block.type == Material.TRAPPED_CHEST) {
            entityDataManager.addChest(
                event.block.location,
                playerDataManager.getPlayerTeam(Lib.getPlayerIdentifier(event.player))
            )
        }
    }

    @EventHandler
    fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent) {
        if (Settings.open && Lib.isPortalNearby(event.block.location)) {
            event.player.sendMessage("${ChatColor.RED}Du kannst hier nichts setzen, das dient zum Schutz des Netherportals")
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock != null) {
            if (event.clickedBlock!!.type == Material.CHEST || event.clickedBlock!!.type == Material.TRAPPED_CHEST) {
                if (!entityDataManager.ownChest(
                        event.clickedBlock!!.location,
                        playerDataManager.getPlayerTeam(Lib.getPlayerIdentifier(event.player))
                    )
                ) {
                    event.player.closeInventory()
                    playerDataManager.strikePlayer(event.player, "Du hast eine fremde Kiste geöffnet")
                    event.isCancelled = true
                }
            } else if (
                (event.clickedBlock!!.location.world != null && event.clickedBlock!!.location.world!!.environment != World.Environment.NETHER) &&
                (event.clickedBlock!!.type == Material.BLACK_BED || event.clickedBlock!!.type == Material.BLUE_BED || event.clickedBlock!!.type == Material.BROWN_BED || event.clickedBlock!!.type == Material.CYAN_BED || event.clickedBlock!!.type == Material.GRAY_BED || event.clickedBlock!!.type == Material.GREEN_BED || event.clickedBlock!!.type == Material.LIGHT_BLUE_BED || event.clickedBlock!!.type == Material.LIGHT_GRAY_BED || event.clickedBlock!!.type == Material.LIME_BED || event.clickedBlock!!.type == Material.MAGENTA_BED || event.clickedBlock!!.type == Material.ORANGE_BED || event.clickedBlock!!.type == Material.PINK_BED || event.clickedBlock!!.type == Material.PURPLE_BED || event.clickedBlock!!.type == Material.RED_BED || event.clickedBlock!!.type == Material.WHITE_BED || event.clickedBlock!!.type == Material.YELLOW_BED)
            ) {
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

    @EventHandler
    fun onPlayerRecipeDiscover(event: PlayerRecipeDiscoverEvent) {
        event.isCancelled = true
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
            single { Translator() }
            single { CustomRecipes(get()) }
        }

        startKoin {
            modules(koinModules)
        }
    }
}
