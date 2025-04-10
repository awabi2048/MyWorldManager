package me.awabi2048.mw_manager

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.custom_item.CustomItem
import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
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

    fun translatePlayerSpecifier(specifier: String): OfflinePlayer? {
        val player = when (specifier.split(":")[0]) {
            "player" -> Bukkit.getOfflinePlayer(specifier.split(":")[1])
            "uuid" -> Bukkit.getOfflinePlayer(UUID.fromString(specifier.split(":")[1]))
            else -> null
        }

        return player
    }

    fun translateWorldSpecifier(specifier: String): MyWorld? {
        if (specifier.startsWith("uuid:")) {
            val worldUUID = specifier.substringAfter("uuid:")
            val myWorld = MyWorldManager.registeredMyWorlds.find { it.uuid == worldUUID }

            return myWorld
        } else {
            try {
                val ownerName = specifier.substringBefore(":")
                val worldName = specifier.substringAfter(":")

                val myWorld = MyWorldManager.registeredMyWorlds.find {it.owner?.name == ownerName && it.name == worldName}
                return myWorld

            } catch (e: Exception) {
                return null
            }
        }
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

    fun getCustomItem(item: ItemStack?): CustomItem? {
        return CustomItem.valueOf(item?.itemMeta?.persistentDataContainer?.get(
            NamespacedKey(instance, "item_id"),
            PersistentDataType.STRING
        )?.uppercase()?: return null)
    }

    fun formatDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)", Locale.JAPANESE)
        return localDate.format(formatter).toString()
    }

    fun checkIfContainsBlacklisted(string: String): Boolean {
        return Config.stringBlacklist.any { string.contains(it) }
    }

    fun checkWorldNameAvailable(name: String, player: Player): Boolean {
        // ブラックリスト判定
        if (checkIfContainsBlacklisted(name)) {
            player.sendMessage("§c使用できない文字列が含まれています。再度入力してください。(${Config.cancelFlag}でキャンセル)")
            return false
        }

        // 文字種判定
        if (!name.matches("^[a-zA-z0-9]*$".toRegex())) {
            player.sendMessage("§cワールド名には半角英数字のみ使用可能です。再度入力してください。(${Config.cancelFlag}でキャンセル)")
            return false
        }

        // 文字長
        if (name.length !in Config.availableWorldNameLength) {
            player.sendMessage("§cワールド名は§n${Config.availableWorldNameLength.min()}文字以上${Config.availableWorldNameLength.max()}文字以下§cである必要があります。再度入力してください。(${Config.cancelFlag}でキャンセル)")
            return false
        }

        // 重複: それぞれプレイヤーにつき同じ名前のワールドはひとつのみ所有できる
        if (MyWorldManager.registeredMyWorlds.filter {it.owner == player}.any {it.name == name}) {
            player.sendMessage("§c既に同じ名前のワールドを作成しています。再度入力してください。(${Config.cancelFlag}でキャンセル)")
            return false
        }

        // 上記いずれにも引っ掛からなければ通ってヨシ
        return true
    }

    fun escapePlayers(players: Set<Player>) {
        players.forEach {
            it.teleport(Config.escapeLocation!!)
            it.playSound(it, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 2.0f)
        }
    }
}
