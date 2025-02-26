package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.Main.Companion.instance
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

data class CreationData(
    val player: Player,
    var worldName: String?,
    var sourceWorldName: String?,
    var creationLevel: CreationLevel,
) {
    fun register() {
        if (worldName != null && sourceWorldName != null) {
            val uuid = UUID.randomUUID().toString()
            val myWorld = MyWorld(uuid)

            player.sendMessage("§eワールドを作成しています...")
            player.playSound(player, Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f)
            player.closeInventory()

            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    myWorld.initiate(sourceWorldName!!, player, worldName!!)
                },
                10L
            )

            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    myWorld.warpPlayer(player)
                },
                60L
            )

        } else return
    }
}
