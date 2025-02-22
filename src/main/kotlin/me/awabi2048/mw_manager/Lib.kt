package me.awabi2048.mw_manager

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.util.*

object Lib {
    object YamlUtil {
        fun load(filePath: String): YamlConfiguration {
            val settingDataFile =
                File(instance.dataFolder.toString() + File.separator + filePath.replace("/", File.separator))
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

    fun convertPlayerSpecifier(specifier: String): Player? {
        val player = when (specifier.split(":")[0]) {
            "player" -> Bukkit.getPlayer(specifier.split(":")[1])
            "puuid" -> Bukkit.getPlayer(UUID.fromString(specifier.split(":")[1]))
            else -> null
        }

        return player
    }

    fun translateWorldSpecifier(specifier: String): MyWorld? {
        if (specifier.startsWith("world:")) {
            val worldName = specifier.substringAfter("world:")
            val myWorld = MyWorld(worldName)

            if (!MyWorldManager.registeredWorld.contains(myWorld)) return null
            return myWorld

        } else if (specifier.startsWith("wuuid:")) {
            val worldUUID = specifier.substringAfter("wuuid:")
            val myWorld = MyWorldManager.registeredWorld.find { it.uuid == worldUUID }

            return myWorld
        } else return null
    }

    fun getVirtualItem(material: Material): ItemStack {
        val item = ItemStack(material)
        item.setItemMeta(
            item.itemMeta.apply {
                isHideTooltip = true
            })
        return item
    }

    fun blockLocationToString(x: Int, y: Int, z: Int): String {
        return "($x, $y, $z)"
    }

    fun stringToBlockLocation(string: String): List<Int>? {
        return try {
            string.filterNot { it == ' ' }.split(",").map { it.toInt() }
        } catch (e: Exception) {
            null
        }
    }

    fun getItemID(item: ItemStack?): String? {
        return item?.itemMeta?.persistentDataContainer?.get(
            NamespacedKey(instance, "item_id"),
            PersistentDataType.STRING
        )
    }
}
