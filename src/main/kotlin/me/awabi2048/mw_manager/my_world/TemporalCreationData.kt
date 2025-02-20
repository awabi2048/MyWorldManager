package me.awabi2048.mw_manager.my_world

import org.bukkit.entity.Player
import java.util.*

data class TemporalCreationData(val player: Player, var worldName: String?, var sourceWorldName: String?, var creationLevel: CreationLevel) {
    fun register() {
        if (worldName != null && sourceWorldName != null) {
            val uuid = UUID.randomUUID().toString()
            val myWorld = MyWorld(uuid)

            myWorld.initiate(worldName!!, player, worldName)
        } else return
    }
}
