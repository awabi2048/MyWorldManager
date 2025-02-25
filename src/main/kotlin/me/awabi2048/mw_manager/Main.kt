package me.awabi2048.mw_manager

import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MVWorldManager
import me.awabi2048.mw_manager.command.*
import me.awabi2048.mw_manager.config.DataFiles
import me.awabi2048.mw_manager.listener.EventListener
import me.awabi2048.mw_manager.listener.VoteListener
import me.awabi2048.mw_manager.listener.WorldCreationSessionListener
import me.awabi2048.mw_manager.listener.WorldPortalListener
import me.awabi2048.mw_manager.my_world.CreationData
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.ui.PlayerWorldSettingState
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        val prefix = "§aMWManager §7»"

        lateinit var instance: JavaPlugin
        lateinit var mvWorldManager: MVWorldManager

        lateinit var creationDataSet: MutableSet<CreationData>
        var worldSettingState: MutableMap<Player, PlayerWorldSettingState> = mutableMapOf()

        var invitationCodeMap = mutableMapOf<String, String>()
        var recruitmentCodeMap = mutableMapOf<String, String>()
    }

    override fun onEnable() {
        instance = this

        DataFiles.loadAll()
        DataFiles.copy()

        mvWorldManager = (server.pluginManager.getPlugin("Multiverse-Core") as MultiverseCore).mvWorldManager

        creationDataSet = mutableSetOf()

        // command
        CommandManager.setExecutor()

        saveDefaultConfig()
        saveResource("world_data.yml", false)
        saveResource("template_setting.yml", false)

        server.pluginManager.registerEvents(EventListener, instance)
        server.pluginManager.registerEvents(WorldCreationSessionListener, instance)
        server.pluginManager.registerEvents(WorldPortalListener, instance)
        server.pluginManager.registerEvents(VoteListener, instance)

        // プレビュー用ワールドの準備
        MyWorldManager.reloadPreviewWorld()

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
