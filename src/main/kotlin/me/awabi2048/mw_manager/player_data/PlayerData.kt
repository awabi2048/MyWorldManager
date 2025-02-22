package me.awabi2048.mw_manager.player_data

import me.awabi2048.mw_manager.config.DataFiles
import org.bukkit.entity.Player

data class PlayerData(val player: Player) {
    private val uuid = player.uniqueId.toString()

    val worldPoint: Int
        get() {
            return DataFiles.playerData.getInt("$uuid.world_point")
        }

    val unlockedWarpSlot: Int
        get() {
            return DataFiles.playerData.getInt("$uuid.unlocked_warp_slot")
        }

    val unlockedWorldSlot: Int
        get() {
            return DataFiles.playerData.getInt("$uuid.unlocked_world_slot")
        }

    val warpShortcuts: List<String>
        get() {
            return DataFiles.playerData.getStringList("$uuid.warp_shortcuts")
        }

    fun initialize() {
        val dataSection = mapOf(
            "world_point" to 0,
            "unlocked_warp_slot" to DataFiles.config.getInt("default_unlocked_warp_slot"),
            "unlocked_world_slot" to DataFiles.config.getInt("default_unlocked_world_slot"),
        )

        DataFiles.playerData.createSection(uuid, dataSection)
        DataFiles.save()
    }
}
