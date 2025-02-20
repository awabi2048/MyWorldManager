package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.config.DataFiles
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.time.LocalDateTime

class TemplateWorld(val worldName: String) {
    val world: World?
        get() {
            return Bukkit.getWorld(worldName)
        }

    val origin: Location
        get() {
            val coordinate = Lib.stringToBlockLocation(DataFiles.templateSetting.getString("$worldName.origin")!!)!!
            return Location(world, coordinate[0] + 0.5, coordinate[1].toDouble(), coordinate[2] + 0.5)
        }

    fun preview(player: Player) {
        val originalLocation = player.location
        player.teleport(origin)
        val endTime = LocalDateTime.now().plusSeconds(3)

        object: BukkitRunnable() {
            override fun run() {
                val location = player.location
                location.yaw += 6
                player.teleport(location)

                if (LocalDateTime.now() == endTime) cancel()
            }

        }.runTaskTimer(instance, 20, 1)
    }
}
