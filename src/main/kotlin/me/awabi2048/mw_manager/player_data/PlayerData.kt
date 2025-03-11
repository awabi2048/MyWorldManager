package me.awabi2048.mw_manager.player_data

import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

data class PlayerData(private val player: OfflinePlayer) {
    private val uuid = player.uniqueId.toString()

    init {
        if (!DataFiles.playerData.contains(uuid)) {
            val dataSection = mapOf(
                "world_point" to 0,
                "unlocked_warp_slot" to Config.defaultUnlockedWarpSlot,
                "unlocked_world_slot" to Config.defaultUnlockedWorldSlot,
            )

            DataFiles.playerData.createSection(uuid, dataSection)
            DataFiles.save()
        }
    }

    private val section: ConfigurationSection
        get() {
            return DataFiles.playerData.getConfigurationSection(uuid)!!
        }

    var worldPoint: Int
        get() {
            return section.getInt("world_point")
        }
        set(value) {
            DataFiles.playerData.set("$uuid.world_point", value.coerceAtLeast(0))
            DataFiles.save()
        }

    var unlockedWarpSlot: Int
        get() {
            return section.getInt("unlocked_warp_slot")
        }
        set(value) {
            DataFiles.playerData.set("$uuid.unlocked_warp_slot", value.coerceAtLeast(0))
            DataFiles.save()
        }

    var unlockedWorldSlot: Int
        get() {
            return section.getInt("unlocked_world_slot")
        }
        set(value) {
            DataFiles.playerData.set("$uuid.unlocked_world_slot", value.coerceAtLeast(1))
            DataFiles.save()
        }

    var warpShortcuts: List<String>
        get() {
            return section.getStringList("warp_shortcuts")
        }
        set(value) {
            val uuidList = value.filter { MyWorld(it).isRegistered }

            DataFiles.playerData.set("$uuid.warp_shortcuts", uuidList)
            DataFiles.save()
        }

    val createdWorlds: Set<MyWorld>
        get() {
            return MyWorldManager.registeredMyWorld.filter {it.owner?.uniqueId == player.uniqueId}.toSet()
        }
}
