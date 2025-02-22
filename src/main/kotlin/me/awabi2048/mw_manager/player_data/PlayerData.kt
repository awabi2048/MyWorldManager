package me.awabi2048.mw_manager.player_data

import me.awabi2048.mw_manager.config.DataFiles
import org.bukkit.entity.Player

data class PlayerData(val player: Player) {
    fun initialize() {
        DataFiles.playerData
    }
}