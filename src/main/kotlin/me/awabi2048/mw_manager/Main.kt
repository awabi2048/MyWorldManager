package me.awabi2048.mw_manager

import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MVWorldManager
import me.awabi2048.mw_manager.command.CommandManager
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.listener.*
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.my_world.world_create.CreationData
import me.awabi2048.mw_manager.ui.abstract.AbstractUI
import me.awabi2048.mw_manager.ui.state_manager.ConfirmationTracker
import me.awabi2048.mw_manager.ui.state_manager.PlayerWorldSettingState
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        const val PREFIX = "§aMWManager §7»"

        lateinit var instance: JavaPlugin
        lateinit var mvWorldManager: MVWorldManager

        lateinit var creationDataSet: MutableSet<CreationData>
        var worldSettingState: MutableMap<Player, PlayerWorldSettingState> = mutableMapOf()

        var confirmationTracker: MutableSet<ConfirmationTracker> = mutableSetOf()

        var invitationCodeMap = mutableMapOf<String, String>()
        var recruitmentCodeMap = mutableMapOf<String, String>()

        var playerUIState: MutableMap<Player, AbstractUI> = mutableMapOf()

        var playersInPortalCooldown: Set<Player> = mutableSetOf()
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
        server.pluginManager.registerEvents(WorldCreationSessionListener, instance)
        server.pluginManager.registerEvents(WorldPortalListener, instance)
        server.pluginManager.registerEvents(VoteListener, instance)
        server.pluginManager.registerEvents(WorldSettingListener, instance)
        server.pluginManager.registerEvents(UIListener, instance)
        server.pluginManager.registerEvents(ChatInputListener, instance)
        server.pluginManager.registerEvents(MiscListener, instance)

        //
        MyWorldManager.loadTemplateWorlds()
        MyWorldManager.updateData()

        // ポータルの判定・演出
        Bukkit.getScheduler().runTaskTimer(
            instance,
            Runnable {
                MyWorldManager.registeredPortal.forEach {
                    it.tickingProcess()
                }
            },
            100L, // ロード処理諸々があるため念の為
            5L
        )
    }

    override fun onDisable() {
        // キュー内のワールドデータを削除

    }
}
