package me.awabi2048.mw_manager.my_world.world_create

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.my_world.MyWorld
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

data class CreationData(
    val player: Player,
    var worldName: String?,
    var templateId: String?,
    var creationStage: CreationStage,
    val uuid: UUID,
    val escapeLocation: Location
) {
    fun register() {
        if (worldName != null && templateId != null) {
            val uuid = UUID.randomUUID().toString()
            val myWorld = MyWorld(uuid)

            player.sendMessage("§eワールドを作成しています...")
            player.playSound(player, Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f)
            player.closeInventory()

            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    myWorld.initiate(templateId!!, player, worldName!!)
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
