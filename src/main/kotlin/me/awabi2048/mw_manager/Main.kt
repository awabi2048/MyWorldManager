package me.awabi2048.mw_manager

import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MVWorldManager
import me.awabi2048.mw_manager.command.RegisterWorldCommand
import me.awabi2048.mw_manager.command.UpdateWorld
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        val prefix = "§aPWManager §7»"

        lateinit var registeredWorldData: FileConfiguration
        lateinit var configData: FileConfiguration

        lateinit var instance: Main
        lateinit var mvWorldManager: MVWorldManager
    }

    override fun onEnable() {
        registeredWorldData = Lib.YamlUtil.load("registered_worlds.yml")
        configData = config

        instance = this
        mvWorldManager = (server.pluginManager.getPlugin("Multiverse-Core") as MultiverseCore).mvWorldManager

        // command
        getCommand(configData.getString("register_world_command", "register_world")!!)?.setExecutor(RegisterWorldCommand)
        getCommand(configData.getString("update_world_command", "update_world")!!)?.setExecutor(UpdateWorld)

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
