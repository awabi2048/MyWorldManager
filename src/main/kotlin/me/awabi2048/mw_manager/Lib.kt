package me.awabi2048.mw_manager

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.*
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

            if (!MyWorldManager.registeredMyWorld.contains(myWorld)) return null
            return myWorld

        } else if (specifier.startsWith("wuuid:")) {
            val worldUUID = specifier.substringAfter("wuuid:")
            val myWorld = MyWorldManager.registeredMyWorld.find { it.uuid == worldUUID }

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

    fun locationToString(location: Location): String {
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ

        return "($x, $y, $z)"
    }

    fun stringToBlockLocation(world: World, string: String): Location {
        val coordinate = string.replace(" ", "").replace("(", "").replace(")", "").split(",").map { it.toInt() }
        return Location(world, coordinate[0] + 0.5, coordinate[1].toDouble(), coordinate[2] + 0.5)
    }

    fun getItemID(item: ItemStack?): String? {
        return item?.itemMeta?.persistentDataContainer?.get(
            NamespacedKey(instance, "item_id"),
            PersistentDataType.STRING
        )
    }

    fun formatDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)", Locale.JAPANESE)
        return localDate.format(formatter).toString()
    }

    fun stringContainsBlacklisted(string: String): Boolean {
        return Config.stringBlacklist.any {string.contains(it)}
    }

    fun checkIfAlphaNumeric(string: String): Boolean {
        return string.matches("^[a-zA-z0-9]*$".toRegex())
    }

    fun resolveComponent(component: Component): String {
        return PlainTextComponentSerializer.plainText().serialize(component)
    }
}
