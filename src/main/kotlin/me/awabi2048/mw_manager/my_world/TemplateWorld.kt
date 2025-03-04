package me.awabi2048.mw_manager.my_world

import com.onarandombox.MultiverseCore.api.MultiverseWorld
import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.ui.children.TemplateSelectUI
import org.bukkit.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class TemplateWorld(val worldId: String) {
    private val dataSection: ConfigurationSection?
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
            return mvWorldManager.mvWorlds.find { it.cbWorld == cbWorld }
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

    val icon: Material?
        get() {
            return Material.valueOf(dataSection?.getString("icon")?: return null)
        }

    fun preview(player: Player) {
        // プレイヤーが帰ってこられるよう
        val returnLocation = player.location
        val returnGamemode = player.gameMode

        // プレイヤーに通知
        player.sendMessage("§b「${name}」 §7をプレビュー中...")
        player.sendMessage("§7${description}")

        // プレビューの準備
        originLocation.chunk.load() // チャンクをロードしないとTPできない

        val previewLocation = originLocation.apply {
            add(0.0, 7.0, 0.0)
        }

        val previewEntity =
            cbWorld!!.spawnEntity(previewLocation, EntityType.ITEM_DISPLAY) as ItemDisplay
        previewEntity.addScoreboardTag("mwm.template_preview")

        val previewTimeSec = 12
        val delay = 8L

        player.teleport(originLocation) // spectateでディメンションを跨ぐと、観察状態が強制的に解除される（バグ？）

        // Multiverseの野郎のおかげで、同tickで処理すると上書きされる
        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
//                println("PLAYER START PREVIEW PROCESS")
                player.gameMode = GameMode.SPECTATOR
                player.spectatorTarget = previewEntity

//                println("start spectate: ${player.spectatorTarget}, preview: $previewEntity")
            }, delay
        )

        // スケジュール: 終了後に原状復帰
        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
//                println("PLAYER END PREVIEW PROCESS")
                // ここの解除で発火しないように
                previewEntity.removeScoreboardTag("mwm.template_preview")

                // スペクテイター解除
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
//                println("PREVIEW PROCESS")
                val location = previewEntity.location.apply {
                    yaw += 1.5f
                }

                previewEntity.teleport(location)
//                println("player spectator target: ${player.spectatorTarget}")

                // 何かしらでワールドを抜けたら (終了時も含む)
                if (player.world != cbWorld) {
                    cancel()
                }

                // プレイヤーがログアウトしたら
                if (!player.isOnline) {
                    previewEntity.remove()
                    cancel()
                    creationDataSet.removeIf { it.player == player }
                }
            }
        }.runTaskTimer(instance, delay + 1, 0L)
    }
}
