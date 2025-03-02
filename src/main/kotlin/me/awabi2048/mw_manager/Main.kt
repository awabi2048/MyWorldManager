package me.awabi2048.mw_manager

import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MVWorldManager
import me.awabi2048.mw_manager.command.CommandManager
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.listener.*
import me.awabi2048.mw_manager.my_world.CreationData
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.portal.WorldPortal
import me.awabi2048.mw_manager.ui.AbstractUI
import me.awabi2048.mw_manager.ui.ConfirmationTracker
import me.awabi2048.mw_manager.ui.PlayerWorldSettingState
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        val prefix = "§aMWManager §7»"

        lateinit var instance: JavaPlugin
        lateinit var mvWorldManager: MVWorldManager

        lateinit var creationDataSet: MutableSet<CreationData>
        var worldSettingState: MutableMap<Player, PlayerWorldSettingState> = mutableMapOf()

        var confirmationTracker: MutableSet<ConfirmationTracker> = mutableSetOf()

        var invitationCodeMap = mutableMapOf<String, String>()
        var recruitmentCodeMap = mutableMapOf<String, String>()

        var playerUIState: MutableMap<Player, AbstractUI> = mutableMapOf()
    }

    override fun onEnable() {
        instance = this

        DataFiles.copy()
        DataFiles.loadAll()

        mvWorldManager = (server.pluginManager.getPlugin("Multiverse-Core") as MultiverseCore).mvWorldManager

        creationDataSet = mutableSetOf()

        // command
        CommandManager.setExecutor()

        // EventListenerバイオーム
        server.pluginManager.registerEvents(EventListener, instance)
        server.pluginManager.registerEvents(WorldCreationSessionListener, instance)
        server.pluginManager.registerEvents(WorldPortalListener, instance)
        server.pluginManager.registerEvents(VoteListener, instance)
        server.pluginManager.registerEvents(WorldSettingListener, instance)
        server.pluginManager.registerEvents(UIListener, instance)

        //
        MyWorldManager.loadTemplateWorlds()

        // ポータルの判定・演出
        Bukkit.getScheduler().runTaskTimer(
            instance,
            Runnable {
                DataFiles.portalData.getKeys(false).forEach {
                    val portal = WorldPortal(it)

                    if (!portal.isAvailable) return@forEach // 無効なポータル
                    if (!portal.location.isChunkLoaded) return@forEach // ロードされていない
                    if (portal.location.getNearbyPlayers(10.0).isEmpty()) return@forEach // 10ブロック以内にプレイヤーがいない

                    portal.tickingProcess()
                }
            },
            100L, // ロード処理諸々があるため念の為
            5L
        )
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
