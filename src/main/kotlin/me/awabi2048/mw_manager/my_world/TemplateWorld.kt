package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.ui.TemplateSelectUI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class TemplateWorld(val worldId: String) {
    val dataSection: ConfigurationSection?
        get() {
            return DataFiles.templateSetting.getConfigurationSection(worldId)
        }

//    val world: World?
//        get() {
//            return Bukkit.getWorld(worldId)
//        }

    val previewWorld: World?
        get() {
            return Bukkit.getWorld("preview.$worldId")
        }

    val originLocation: Location
        get() {
            return Lib.stringToBlockLocation(previewWorld!!, DataFiles.templateSetting.getString("$worldId.origin_location")!!)
        }

    val name: String?
        get() {
            return dataSection?.getString("name")
        }

    val description: String?
        get() {
            return dataSection?.getString("description")
        }

    fun preview(player: Player) {
        val returnLocation = player.location
        player.teleport(
            originLocation.apply {
                add(0.0, 5.0, 0.0)
                pitch = 20f
            }
        )

        val previewTimeSec = 10

        // 終了後に元の位置に
        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                player.teleport(returnLocation)
            },
            previewTimeSec * 20L
        )

        // メニュー再度開く (TP直後だとロードの時間があるから10t遅延して)
        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                TemplateSelectUI(player).open(false)
            },
            previewTimeSec * 20L + 10L
        )

        // ぐるっと一周
        object : BukkitRunnable() {
            override fun run() {
                val location = player.location
                location.yaw += 2f
                player.teleport(location)

                if (player.world != previewWorld) cancel()
            }

        }.runTaskTimer(instance, 0, 1)
    }
}
