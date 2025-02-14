package me.awabi2048.mw_manager

import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MVWorldManager
import me.awabi2048.mw_manager.command.RegisterWorldCommand
import me.awabi2048.mw_manager.command.UpdateWorld
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        val prefix = "§aMWManager §7»"

        lateinit var registeredWorldData: FileConfiguration
        lateinit var configData: FileConfiguration

        lateinit var instance: JavaPlugin
        lateinit var mvWorldManager: MVWorldManager
    }

    override fun onEnable() {
        instance = this

        registeredWorldData = Lib.YamlUtil.load("world_data.yml")
        configData = config
        mvWorldManager = (server.pluginManager.getPlugin("Multiverse-Core") as MultiverseCore).mvWorldManager

        // command
//        getCommand("mwregister")?.setExecutor(RegisterWorldCommand)
//        getCommand("mwupdate")?.setExecutor(UpdateWorld)
        getCommand("myworldmanager")?.setExecutor(UpdateWorld)
        getCommand("mwmanager")?.setExecutor(UpdateWorld)
        getCommand("mwm")?.setExecutor(UpdateWorld)

        saveDefaultConfig()
        saveResource("world_data.yml", false)

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
