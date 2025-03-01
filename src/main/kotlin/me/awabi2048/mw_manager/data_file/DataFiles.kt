package me.awabi2048.mw_manager.data_file

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.instance
import org.bukkit.configuration.file.FileConfiguration
import java.io.File

object DataFiles {
    lateinit var config: FileConfiguration
    lateinit var playerData: FileConfiguration
    lateinit var worldData: FileConfiguration
    lateinit var templateSetting: FileConfiguration
    lateinit var portalData: FileConfiguration
    lateinit var macroSetting: FileConfiguration

    val filePath = listOf(
        "config.yml",
        "player_data.yml",
        "world_data.yml",
        "template_setting.yml",
        "portal_data.yml",
        "macro_setting.yml",
    )

    fun loadAll() {
        config = Lib.YamlUtil.load("config.yml")
        playerData = Lib.YamlUtil.load("player_data.yml")
        worldData = Lib.YamlUtil.load("world_data.yml")
        templateSetting = Lib.YamlUtil.load("template_setting.yml")
        portalData = Lib.YamlUtil.load("portal_data.yml")
        macroSetting = Lib.YamlUtil.load("macro_setting.yml")
    }

    /**
     * データファイルを上書き保存します。
     */
    fun save() {
        Lib.YamlUtil.save("player_data.yml", playerData)
        Lib.YamlUtil.save("world_data.yml", worldData)
        Lib.YamlUtil.save("portal_data.yml", portalData)
    }

    fun copy() {
        filePath.forEach {
            if (!File(instance.dataFolder.path + File.separator + it).exists()) {
                instance.saveResource(it, false)
                instance.logger.info("Copied \"$it\" to the data folder.")
            }
        }
    }
}
