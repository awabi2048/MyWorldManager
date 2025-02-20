package me.awabi2048.mw_manager

import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MVWorldManager
import me.awabi2048.mw_manager.command.MWMCommand
import me.awabi2048.mw_manager.listener.EventListener
import me.awabi2048.mw_manager.my_world.TemporalCreationData
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        val prefix = "§aMWManager §7»"

        lateinit var registeredWorldData: FileConfiguration
        lateinit var configData: FileConfiguration

        lateinit var instance: JavaPlugin
        lateinit var mvWorldManager: MVWorldManager

        lateinit var creationDataSet: MutableSet<TemporalCreationData>
    }

    override fun onEnable() {
        instance = this

        registeredWorldData = Lib.YamlUtil.load("world_data.yml")
        configData = config
        mvWorldManager = (server.pluginManager.getPlugin("Multiverse-Core") as MultiverseCore).mvWorldManager

        creationDataSet = mutableSetOf()

        // command
//        getCommand("mwregister")?.setExecutor(RegisterWorldCommand)
//        getCommand("mwupdate")?.setExecutor(UpdateWorld)
        getCommand("myworldmanager")?.setExecutor(MWMCommand)
        getCommand("mwmanager")?.setExecutor(MWMCommand)
        getCommand("mwm")?.setExecutor(MWMCommand)
        getCommand("invite")?.setExecutor(MWMCommand)

        saveDefaultConfig()
        saveResource("world_data.yml", false)

        server.pluginManager.registerEvents(EventListener, instance)

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
