package me.awabi2048.mw_manager.my_world

import com.onarandombox.MultiverseCore.MVWorld
import com.onarandombox.MultiverseCore.api.MultiverseWorld
import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.ui.TemplateSelectUI
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.roundToInt
import kotlin.math.sin

class TemplateWorld(val worldId: String) {
    val dataSection: ConfigurationSection?
        get() {
            return DataFiles.templateSetting.getConfigurationSection(worldId)
        }

//    val world: World?
//        get() {
//            return Bukkit.getWorld(worldId)
//        }

    val cbWorld: World?
        get() {
            return Bukkit.getWorld(worldId)
        }

    val mvWorld: MultiverseWorld?
        get() {
            return mvWorldManager.mvWorlds.find {it.cbWorld == cbWorld}
        }

    val originLocation: Location
        get() {
            return Lib.stringToBlockLocation(
                cbWorld!!,
                DataFiles.templateSetting.getString("$worldId.origin_location")!!
            )
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
        // プレイヤーが帰ってこられるよう
        val returnLocation = player.location
        val returnGamemode = player.gameMode

        // プレイヤーに通知

        // プレビューの準備
        val previewEntity = cbWorld!!.spawnEntity(originLocation.add(0.0, 7.0, 0.0), EntityType.ARMOR_STAND) as ArmorStand
        previewEntity.isMarker = true
        previewEntity.isInvisible = true
        previewEntity.addScoreboardTag("mwm.template_preview")

        previewEntity.location.chunk.load() // チャンクをロードしないとTPできない

        player.gameMode = GameMode.SPECTATOR
        player.spectatorTarget = previewEntity

        val previewTimeSec = 18

        // スケジュール: 終了後に原状復帰
        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                // ここの解除で発火しないように
                previewEntity.removeScoreboardTag("mwm.template_preview")

                // スペクテイター解除
                player.spectatorTarget = null
                player.gameMode = returnGamemode
                previewEntity.remove()

                player.teleport(returnLocation)

                // メニュー再度ひらく
                TemplateSelectUI(player).open(false)
            },
            previewTimeSec * 20L
        )

        // ぐるっと一周
        object : BukkitRunnable() {
            override fun run() {
                val location = previewEntity.location.apply {
                    yaw += 1.0f
                    pitch = (sin(Math.toRadians(yaw.toDouble() / 2)) * 10).roundToInt().toFloat() // ピッチをいい感じにしたいけど、ラグでがたがたになる
                }

                previewEntity.teleport(location)

                // 何かしらでワールドを抜けたら (終了時も含む)
                if (player.world != cbWorld) {
                    cancel()
                }

                // プレイヤーがログアウトしたら
                if (!player.isOnline) {
                    previewEntity.remove()
                    cancel()
                    creationDataSet.removeIf{it.player == player}
                }
            }

        }.runTaskTimer(instance, 0, 1)
    }
}
