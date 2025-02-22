package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.config.DataFiles
import me.awabi2048.mw_manager.ui.TemplateSelectUI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class TemplateWorld(val worldName: String) {
    val world: World?
        get() {
            return Bukkit.getWorld(worldName)
        }

    val originLocation: Location
        get() {
            val coordinate =
                Lib.stringToBlockLocation(DataFiles.templateSetting.getString("$worldName.origin_location")!!)!!
            return Location(world, coordinate[0] + 0.5, coordinate[1].toDouble(), coordinate[2] + 0.5)
        }

    fun preview(player: Player) {
        println(originLocation)

        val returnLocation = player.location
        player.teleport(
            originLocation.apply {
                add(0.0, 5.0, 0.0)
                pitch = 30f
            }
        )

        // 終了後に元の位置に
        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                player.teleport(returnLocation)
            },
            10 * 20L
        )

        // メニュー再度開く
        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                TemplateSelectUI(player).open()
            },
            10 * 20L + 10L
        )

        // ぐるっと一周
        object : BukkitRunnable() {
            override fun run() {
                val location = player.location
                location.yaw += 2f
                player.teleport(location)

                if (player.world != world) cancel()
            }

        }.runTaskTimer(instance, 0, 1)
    }
}
