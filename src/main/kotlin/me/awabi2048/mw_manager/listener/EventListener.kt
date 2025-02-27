package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.MyWorldManager
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerLoginEvent

object EventListener : Listener {
    private fun updateTask(world: World) {
        // データファイルのアップデート
        MyWorldManager.updateData()

        // ワールドの情報とファイル上のデータの同期
        if (world.name.startsWith("my_world.")) {
            val myWorld = MyWorldManager.registeredMyWorld.find {it.vanillaWorld == world}?: return
            myWorld.sync()
        }
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        updateTask(event.player.world)

        // テンプレートワールドの中にいたら追い出す
        if (MyWorldManager.registeredTemplateWorld.any {it.cbWorld == event.player.world}) {
            val escapeLocation = Config.escapeLocation?: return
            event.player.teleport(escapeLocation)
            event.player.playSound(event.player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 2.0f)
        }
    }

    @EventHandler
    fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
        updateTask(event.player.world)
    }
}
