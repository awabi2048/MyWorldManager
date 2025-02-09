package me.awabi2048.mw_manager

import me.awabi2048.mw_manager.Main.Companion.instance
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*

object Lib {
    object YamlUtil {
        fun load(filePath: String): YamlConfiguration {
            val settingDataFile = File(instance.dataFolder.toString() + File.separator + filePath.replace("/", File.separator))
            return YamlConfiguration.loadConfiguration(settingDataFile)
        }

        fun save(filePath: String, yamlSection: FileConfiguration): Boolean {
            try {
                val settingDataFile =
                    File(instance.dataFolder.toString() + File.separator + filePath.replace("/", File.separator))
                yamlSection.save(settingDataFile)

                return true
            } catch (e: Exception) {
                return false
            }
        }
    }

    fun getPlayerFromSpecifier(specifier: String): Player? {
        val player = when (specifier.split(":")[0]) {
            "player" -> Bukkit.getPlayer(specifier.split(":")[1])
            "uuid" -> Bukkit.getPlayer(UUID.fromString(specifier.split(":")[1]))
            else -> null
        }

        return player
    }
}